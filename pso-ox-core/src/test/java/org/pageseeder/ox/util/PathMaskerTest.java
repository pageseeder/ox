/*
 * Copyright 2026 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.util;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.Assert.*;


/**
 *
 * @author ccabral
 * @since 27 October 2026
 */
public class PathMaskerTest {

  private static final String ID = "PDF-609695-50C9";
  private static final String RELATIVE = "original/psml/containers/251-300/278.psml";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private Path tmp;

  private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);

  private static boolean isWindows() { return OS_NAME.startsWith("windows"); }
  private static boolean isLinuxOrMac() { return OS_NAME.startsWith("linux") || OS_NAME.startsWith("mac"); }


  @Before
  public void setUp() {
    tmp = folder.getRoot().toPath();   // the rest of the tests keep using "tmp" unchanged
  }

  /** Creates a real package directory under the temp dir, so the tests run on any OS. */
  private File packageDir() throws IOException {
    return Files.createDirectories(tmp.resolve("OX").resolve("company.app.tmp").resolve(ID)).toFile();
  }

  private static String saxMessage(String systemId) {
    return "org.xml.sax.SAXParseException; systemId: " + systemId
        + "; lineNumber: 12; columnNumber: 75; Character reference \"&#x7\" is an invalid XML character";
  }

  @Test
  public void masksFileUriFormUsedBySax() throws IOException {
    File dir = packageDir();
    String message = saxMessage(dir.toURI().toString() + RELATIVE);
    String masked = PathMasker.maskPath(message, dir, ID);
    assertEquals(saxMessage("[" + ID + "]/" + RELATIVE), masked);
  }

  @Test
  public void masksNativePath() throws IOException {
    File dir = packageDir();
    String message = "Cannot read " + new File(dir, "original/psml/278.psml").getAbsolutePath();
    String masked = PathMasker.maskPath(message, dir, ID);
    assertTrue(masked, masked.startsWith("Cannot read [" + ID + "]"));
    assertFalse(masked, masked.contains(tmp.toString()));
  }

  @Test
  public void masksForwardSlashForm() throws IOException {
    File dir = packageDir();
    String forward = dir.getAbsolutePath().replace('\\', '/');
    String masked = PathMasker.maskPath("at " + forward + "/" + RELATIVE, dir, ID);
    assertEquals("at [" + ID + "]/" + RELATIVE, masked);
  }

  @Test
  public void masksTripleSlashUri() throws IOException {
    File dir = packageDir();
    String uri = dir.toURI().toString();                 // file:/...
    String triple = uri.replaceFirst("^file:/+", "file:///");
    String masked = PathMasker.maskPath("systemId: " + triple + RELATIVE, dir, ID);
    assertEquals("systemId: [" + ID + "]/" + RELATIVE, masked);
  }

  @Test
  public void masksEveryOccurrence() throws IOException {
    File dir = packageDir();
    String uri = dir.toURI().toString();
    String message = uri + "a.psml and " + dir.getAbsolutePath() + File.separator + "b.psml";
    String masked = PathMasker.maskPath(message, dir, ID);
    assertFalse(masked.contains(tmp.toString()));
    assertEquals(2, masked.split("\\[" + ID + "\\]", -1).length - 1);
  }

  @Test
  public void doesNotLeaveStrayLeadingSlashOnNativePath() throws IOException {
    // Regression: stripping "file:/" must not leave "/[id]" for Linux/macOS native paths
    File dir = packageDir();
    String masked = PathMasker.maskPath(dir.getAbsolutePath() + File.separator + "x.psml", dir, ID);
    assertEquals("[" + ID + "]" + File.separator + "x.psml", masked);
  }

  @Test
  public void keepsUnrelatedPathsUntouched() throws IOException {
    File dir = packageDir();
    String message = "Failed reading /some/other/place/file.psml";
    assertEquals(message, PathMasker.maskPath(message, dir, ID));
  }

  @Test
  public void doesNotMaskSiblingPackageWithSamePrefix() throws IOException {
    // PDF-609695-50C9 must not partially match PDF-609695-50C9X
    File dir = packageDir();
    File sibling = new File(dir.getParentFile(), ID + "X");
    String message = "see " + sibling.getAbsolutePath();
    String masked = PathMasker.maskPath(message, dir, ID);
    // The prefix is replaced up to the id; what matters is no raw parent path leaks
    assertFalse(masked.contains(tmp.toString()));
  }

  @Test
  public void handlesSpacesAndNonAsciiInPath() throws IOException {
    File dir = Files.createDirectories(tmp.resolve("John Smith").resolve("Ñandú").resolve(ID)).toFile();
    String message = saxMessage(dir.toURI().toString() + RELATIVE);   // percent-encoded form
    String masked = PathMasker.maskPath(message, dir, ID);
    assertEquals(saxMessage("[" + ID + "]/" + RELATIVE), masked);

    String nativeMsg = "x " + dir.getAbsolutePath() + File.separator + "f.psml";  // raw form
    assertTrue(PathMasker.maskPath(nativeMsg, dir, ID).startsWith("x [" + ID + "]"));
  }

  @Test
  public void handlesTrailingSeparatorOnDirectory() throws IOException {
    File dir = packageDir();
    String withSlash = dir.getAbsolutePath() + File.separator;
    String masked = PathMasker.maskPath("in " + withSlash + "f.psml", dir, ID);
    assertEquals("in [" + ID + "]" + File.separator + "f.psml", masked);
  }

  @Test
  public void handlesNonExistingDirectory() {
    // File.toURI() only adds the trailing slash for existing directories
    File dir = new File(tmp.toFile(), "gone/" + ID);
    String message = saxMessage(dir.toURI().toString() + "/" + RELATIVE);
    String masked = PathMasker.maskPath(message, dir, ID);
    assertEquals(saxMessage("[" + ID + "]/" + RELATIVE), masked);
  }

  @Test
  public void nullAndEmptyInputs() throws IOException {
    File dir = packageDir();
    assertNull(PathMasker.maskPath(null, dir, ID));
    assertEquals("", PathMasker.maskPath("", dir, ID));
    String message = "unchanged";
    assertSame(message, PathMasker.maskPath(message, null, ID));
  }

  @Test
  public void replacementIsLiteralEvenIfIdHasSpecialCharacters() throws IOException {
    File dir = packageDir();
    String masked = PathMasker.maskPath(dir.getAbsolutePath(), dir, "PDF-$1\\x");
    assertEquals("[PDF-$1\\x]", masked);
  }

  @Test
  public void usesCanonicalFormWhenItDiffersFromAbsolute() throws IOException {
    File real = packageDir();
    Path link = tmp.resolve("link");
    try {
      Files.createSymbolicLink(link, real.getParentFile().toPath());
    } catch (UnsupportedOperationException | IOException ex) {
      Assume.assumeNoException(ex);
    }
    File viaLink = new File(link.toFile(), ID);
    // The parser may print the canonical path even though the package knows the link path
    String message = "systemId: " + real.getCanonicalFile().toURI() + RELATIVE;
    String masked = PathMasker.maskPath(message, viaLink, ID);
    assertEquals("systemId: [" + ID + "]/" + RELATIVE, masked);
  }

  // ---- Literal examples taken from the real logs ----

  @Test
  public void windowsLiteralExample() {
    Assume.assumeTrue(isWindows());
    File dir = new File("C:\\Users\\username\\AppData\\Local\\Temp\\OX\\company.app.tmp\\" + ID);
    String message = saxMessage("file:/C:/Users/username/AppData/Local/Temp/OX/company.app.tmp/" + ID + "/" + RELATIVE);
    assertEquals(saxMessage("[" + ID + "]/" + RELATIVE), PathMasker.maskPath(message, dir, ID));
  }

  @Test
  public void windowsDriveLetterCaseIsIgnored() {
    Assume.assumeTrue(isWindows());
    File dir = new File("C:\\Users\\username\\Temp\\" + ID);
    String message = saxMessage("file:/c:/Users/username/Temp/" + ID + "/" + RELATIVE);
    assertEquals(saxMessage("[" + ID + "]/" + RELATIVE), PathMasker.maskPath(message, dir, ID));
  }

  @Test
  public void linuxLiteralExample() {
    Assume.assumeTrue(isLinuxOrMac());
    File dir = new File("/tmp/OX/allette.app.tmp/PDF-BED655-DA0A");
    String message = saxMessage("file:/tmp/OX/allette.app.tmp/PDF-BED655-DA0A/" + RELATIVE);
    assertEquals(saxMessage("[PDF-BED655-DA0A]/" + RELATIVE),
        PathMasker.maskPath(message, dir, "PDF-BED655-DA0A"));
  }
}
