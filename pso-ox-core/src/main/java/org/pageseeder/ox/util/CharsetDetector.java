/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * This class can be used to detect the Charset for a file.
 *
 * @author Christophe Lauret
 * @since  4 December 2014
 */
public final class CharsetDetector {

  /**
   * Maximum size for file in bytes (16Mb).
   */
  private static final int MAX_FILE_SIZE = 0xffffff;

  /**
   * Maximum size of the byte order mark.
   */
  private static final int MAX_BOM_SIZE = 4;

  /**
   * The charsets used by the decode method in order.
   */
  private static final Charset[] DECODE_CHARSETS = new Charset[] { Charset.forName("US-ASCII"), Charset.forName("UTF8"), // must come before ISO-8859-1
  Charset.forName("ISO-8859-1")
  };

  /**
   * An enumeration of byte order marks supported by this class.
   *
   * @see <a href="http://www.unicode.org/versions/Unicode5.0.0/ch02.pdf">Unicode 5.0</a>
   * @see <a href="http://tools.ietf.org/html/rfc3629#section-6">RFC 3629: UTF8 - Section 6: Byte order mark (BOM)</a>
   * @see <a href="http://en.wikipedia.org/wiki/Byte-order_mark">Wikipedia: Byte Order Mark</a>
   *
   * @author Christophe Lauret
   * @since  1 September 2009
   */
  public enum ByteOrderMark {

    /**
     * The byte-order mark for UTF-8 (Eight-bit UCS Transformation Format).
     *
     * xEF xBB xBF
     */
    UTF8(new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf }, "UTF-8"),

    /**
     * The byte-order mark for UTF-16LE (Sixteen-bit UCS Transformation Format, little-endian byte order).
     *
     * 0xFF 0xFE
     */
    UTF16LE(new byte[] { (byte) 0xff, (byte) 0xFE }, "UTF-16LE"),

    /**
     * The byte-order mark for UTF-16LE (Sixteen-bit UCS Transformation Format, big-endian byte order).
     *
     * 0xFE 0xFF
     */
    UTF16BE(new byte[] { (byte) 0xFE, (byte) 0xFF }, "UTF-16BE");

    /**
     * The byte-order mark.
     */
    private final byte[] _bom;

    /**
     * The corresponding character set.
     */
    private final Charset _cs;

    /**
     * Creates a new byte-order mark.
     *
     * @param bom The byte order mark as a byte array.
     * @param cs The name of the corresponding character set.
     */
    private ByteOrderMark(byte[] bom, String cs) {
      this._bom = bom;
      this._cs = Charset.forName(cs);
    }

    /**
     * Indicates whether the specified byte-order mark matches on the constants.
     *
     * @param bom The BOM array.
     *
     * @return <code>true</code> if the
     */
    public boolean matches(byte[] bom) {
      if (bom.length < this._bom.length) return false;
      for (int i = 0; i < this._bom.length; i++) {
        if (this._bom[i] != bom[i]) return false;
      }
      return true;
    }

    /**
     * The charset corresponding to the BOM.
     *
     * @return The charset corresponding to the BOM.
     */
    public Charset charset() {
      return this._cs;
    }
  }

  /**
   * This is a utility class - no need for a constructor
   */
  private CharsetDetector() {}

  /**
   * Returns the encoding of the specified file based on the byte-order mark to see if it matches a charset defined in
   * this class.
   *
   * Returns <code>null</code> if there is no byte order mark.
   *
   * @param f The file to check the encoding for.
   *
   * @return The corresponding charset.
   *
   * @throws IOException If thrown while attempting to read the file.
   */
  public static Charset getFromBOM(File f) throws IOException {
    byte[] bom = new byte[MAX_BOM_SIZE];
    // Check if there is a byte-order mark
    FileInputStream in = null;
    try {
      in = new FileInputStream(f);
      int read = in.read(bom);
      if (read < 2) return null;
      in.close();
      for (ByteOrderMark b : ByteOrderMark.values()) {
        if (b.matches(bom)) return b.charset();
      }
    } finally {
      if (in != null) in.close();
    }
    // Return null if no BOM was present
    return null;
  }

  /**
   * Returns the encoding of the specified file by examining its content.
   *
   * This method will do the following: - Check if there are any non-ASCII characters, if not return US-ASCII - Attempts
   * to read as a Unicode UTF-8 or UTF-16 - Default to the machine default
   *
   * @param f The file to check the encoding for.
   *
   * @return The corresponding charset.
   *
   * @throws IOException If thrown while attempting to read the file.
   */
  public static Charset getFromContent(File f) throws IOException {
    Charset cs = Charset.forName("US-ASCII");
    // Load into a byte buffer
    FileInputStream fis = null;
    FileChannel fc = null;
    try {
      fis = new FileInputStream(f);
      fc = fis.getChannel();
      int upto = fc.size() > MAX_FILE_SIZE ? MAX_FILE_SIZE : (int) fc.size();
      MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, upto);

      // Attempt successive decoding
      for (Charset charset : DECODE_CHARSETS) {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        buffer.rewind();
        int size = Math.round(upto * decoder.maxCharsPerByte());
        CharBuffer out = CharBuffer.allocate(size);
        CoderResult result = decoder.decode(buffer, out, true);
        // no error reported
        if (!result.isError()) {
          // If it is ISO 8859-1, Let's check whether we're dealing with Windows encoding
          if (charset.equals(Charset.forName("ISO-8859-1"))) {
            buffer.rewind();
            while (buffer.hasRemaining()) {
              byte b = buffer.get();
              if (b >= (byte) 0x80 && b < (byte) 0xA0) {
                if (Charset.isSupported("Cp1252")) {
                  fc.close();
                  return Charset.forName("Cp1252");
                }
              }
            }
          }
          fc.close();
          return charset;
        }
      }
    } finally {
      if (fc != null) fc.close();
      if (fis != null) fis.close();
    }

    return cs;
  }

  /**
   * Returns the encoding of the specified file by examining its content.
   *
   * This method will do the following: - Check if there are any non-ASCII characters, if not return US-ASCII - Attempts
   * to read as a Unicode UTF-8 or UTF-16 - Default to the machine default
   *
   * @param f The file to check the encoding for.
   *
   * @return The corresponding charset.
   *
   * @throws IOException If thrown while attempting to read the file.
   */
  public static CharBuffer decode(File f) throws IOException {
    // Load the file in a byte buffer
    int upto = f.length() > MAX_FILE_SIZE ? MAX_FILE_SIZE : (int) f.length();
    ByteBuffer buffer = getFileContent(f, upto);

    // Attempt successive decoding
    for (Charset charset : DECODE_CHARSETS) {
      CharsetDecoder decoder = charset.newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPORT);
      decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
      int size = Math.round(upto * decoder.averageCharsPerByte());
      CharBuffer out = CharBuffer.allocate(size);
      CoderResult result = decoder.decode(buffer, out, true);
      // no error reported
      if (!result.isError()) {
        out.limit(out.position());
        out.rewind();
        return out;
      }
      buffer.rewind();
    }
    // no luck!
    return null;
  }

  /**
   * Returns the content of the files as a byte array.
   *
   * @param f    The file to load.
   * @param upto The number of bytes to read.
   *
   * @return The corresponding byte array.
   *
   * @throws IOException If the file is not found.
   */
  private static ByteBuffer getFileContent(File f, int upto) throws IOException {
    FileChannel fc = null;
    ByteBuffer buffer = null;
    try (FileInputStream in = new FileInputStream(f)) {
      fc = in.getChannel();
      buffer = ByteBuffer.allocate(upto);
      fc.read(buffer);
      fc.close();
      // reset the buffer so that it is ready to use
      buffer.rewind();
    } catch (IOException ex) {
      if (fc != null) fc.close();
    }
    return buffer;
  }

}
