/* Copyright (c) 2018 Allette Systems (Australia) Pty Ltd. */
package org.pageseeder.ox.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * If you would like to know more about glob pattern, please have a look at
 * http://docs.oracle.com/javase/javatutorials/tutorial/essential/io/fileOps.html#glob
 *
 *
 *    By extension
 *
 *    *.java Matches all files with java extension into base directory.
 *    **.java Matches all files with java extension into base directory and sub directories.
 *    **&#47;*.java Matches all files with java extension into sub directories.
 *
 *    By multiple extension
 *
 *    *.{html,java}  Matches all files that has extension as html or java into base directory. if you want the
 *    sub directories.
 *
 *    By folder
 *
 *    *folder1&#47;** Matches all files into sub directory folder1
 *
 *    By single character
 *
 *    ?.java Matches all files that has any single character as name and extension as java into base directory.
 *    [abc].java Matches all files that has a or b or c as name and extension as java into base directory.
 *    [!a].java Matches all files that has any single character different of 'a' as name and extension as java into base directory.
 *
 *
 *    &#47; It is the slash
 *
 */

public class FilesFinder {

  /** The logger. */
  private final Logger LOGGER = LoggerFactory.getLogger(FilesFinder.class);

  /** The visitor. */
  private final FilesFinderVisitor _visitor;

  /** The base dir. */
  private final Path _baseDir;

  /**
   * Instantiates a new files finder.
   *
   * @param pattern the pattern
   * @param baseDir the base dir
   */
  public FilesFinder(String pattern, File baseDir) {
    super();
    if (baseDir != null && baseDir.isDirectory()) {
      this._baseDir = baseDir.toPath();
      this._visitor = new FilesFinderVisitor(pattern, this._baseDir);
    } else {
      LOGGER.debug("Base Directory is {} ", baseDir == null? "null" : "directory");
      this._baseDir = null;
      this._visitor = null;
    }

  }

  /**
   * Run.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void run() throws IOException {
    Files.walkFileTree(this._baseDir, this._visitor);
  }

  /**
   * Gets the files.
   *
   * @return the files
   */
  public List<File> getFiles() {
    List<File> files = null;
    try {
      if (this._visitor != null) {
        run();
        files = this._visitor.getFilesFound();
      }
    } catch (IOException ex) {
      LOGGER.error("Following error occurred: {}.", ex.getMessage());
    }
    return files;
  }
}
