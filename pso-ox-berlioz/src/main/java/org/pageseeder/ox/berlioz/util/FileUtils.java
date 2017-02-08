/*
 * Copyright (c) 2015 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import javax.activation.MimetypesFileTypeMap;

/**
 * The Class FileUtils.
 *
 * @author Carlos Cabral
 * @since 13 April 2016
 */
public class FileUtils {

  /** The Constant MIME_APPLICATION. */
  public static final String MIME_APPLICATION = "application/octet-stream";

  /** The Constant EXTENSION_JAVA. */
  public static final String EXTENSION_JAVA = "java";

  /** The Constant DIRECTORY_SEPARATOR. */
  public static final String DIRECTORY_SEPARATOR = "/";

  /** The Constant DIRECTORY_SEPARATOR_WINDOWS. */
  public static final String DIRECTORY_SEPARATOR_WINDOWS = "\\";

  /** The Constant ENCODE_UTF8. */
  public static final String ENCODE_UTF8 = "utf-8";


	/**
	 * Before create the it checks if the directories exist, if not it creates
	 * the directory.
	 *
	 * @param file the file
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
  public static boolean createFile(File file) throws IOException{
  	int lastSeparatorPosition = file.getPath().lastIndexOf(File.separator);
		if(lastSeparatorPosition > 0){
			//may be the parent folder(if there is) doesn't exist
			createDirectories(file.getPath().substring(0, lastSeparatorPosition));
		}
		return file.createNewFile();
  }

  /**
   * Create the file and the directories if necessary.
   *
   * @param pathAndName the path and name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static File createFile(String pathAndName) throws IOException{
      File file = new File(pathAndName);
      //Check if it is possible to create this file, if yes it creates.
      //Otherwise no and throw an exception.
      createFile(file);
      return file;
  }

  /**
   * Creates the directories.
   *
   * @param path the path
   * @return true, if successful
   */
  public static boolean createDirectories(String path){
    File directory = new File(path);
    return createDirectories(directory);
  }

  /**
   * Creates the directories.
   *
   * @param directory the directory
   * @return true, if successful
   */
  public static boolean createDirectories(File directory){
  	return directory.mkdirs();
  }

  /**
   * Gets the mime.
   *
   * @param file the file
   * @return the mime
   */
  public static String getMime(File file){
      return  new MimetypesFileTypeMap().getContentType(file);
  }

  /**
   * Gets the extension.
   *
   * @param file the file
   * @return the extension
   */
  public static String getExtension(File file){
      String fileName = file.getName();
      if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
          return fileName.substring(fileName.lastIndexOf(".")+1);
      else return "";
  }

  /**
   * @param source the source file or directory.
   * @param target the target file or directory.
   * @throws IOException when IO error occur.
   */
  public static void copy(final File source, final File target) throws IOException {
    if (source == null) throw new NullPointerException("source directory is null.");
    if (target == null) throw new NullPointerException("target directory is null.");

    if (source.isFile()) {
      Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    } else if (source.isDirectory()) {
      Files.walkFileTree(source.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
          Files.createDirectories(target.toPath().resolve(source.toPath().relativize(dir)));
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          Files.copy(file, target.toPath().resolve(source.toPath().relativize(file)), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  /**
   * Copies the file using NIO.
   *
   * @param from          File to copy
   * @param to          Target file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void copy(FileInputStream from, FileOutputStream to) throws IOException {

    FileChannel source = null;
    FileChannel destination = null;
    try {
      source = from.getChannel();
      destination = to.getChannel();
      destination.transferFrom(source, 0, source.size());
    } finally {
      smothlyClose(source);
      smothlyClose(destination);
    }
  }

  /**
   * Copy.
   *
   * @param from the from
   * @param to the to
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void copy(InputStream from, OutputStream to) throws IOException {
    try {
      int read = 0;
      byte[] bytes = new byte[1024];

      while ((read = from.read(bytes)) != -1) {
        to.write(bytes, 0, read);
      }
    } finally {
      smothlyClose(from);
      smothlyClose(to);
    }
  }

  /**
   * Smothly close.
   *
   * @param io the io
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void smothlyClose(Closeable io) throws IOException {
    if (io != null) {
      io.close();
    }
  }
  /**
   *
   * @param file
   * @return
   */
  public static String fileNameNoExtension (File file) {
    String nameWithExtension = file.getName();
    int dotPosition = nameWithExtension.lastIndexOf('.');
    String nameNoExtension = nameWithExtension;
    if (dotPosition >= 0) {
      nameNoExtension = nameWithExtension.substring(0, dotPosition);
    }
    return nameNoExtension;
  }
}
