/* Copyright (c) 2018 Allette Systems (Australia) Pty Ltd. */
package org.pageseeder.ox.util;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walk from the base directory and add to the list all files that match the pattern.
 * 
 * If you would like to know more about glob pattern, please have a look at
 * http://docs.oracle.com/javase/javatutorials/tutorial/essential/io/fileOps.html#glob
 */
public class FilesFinderVisitor extends SimpleFileVisitor<Path> {
  
  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(FilesFinderVisitor.class);
  
  /** The files found. */
  private final List<File> filesFound = new ArrayList<>();
  
  /** The matcher. */
  private final PathMatcher matcher;
  
  /** The base dir. */
  private final String _baseDir;

  /**
   * Instantiates a new files finder visitor.
   *
   * @param pattern the pattern
   * @param baseDir the base dir
   */
  public FilesFinderVisitor(String pattern, Path baseDir) {
    if (StringUtils.isBlank(pattern)) throw new IllegalArgumentException("The pattern for the file finder cannot be blank.");
    Objects.requireNonNull(baseDir, "The base directory for the file finder is invalid.");
    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    this._baseDir = normalizePath(baseDir) + File.separatorChar;
  }

  /**
   * Selects the files by comparing the glob pattern against the path (From the base directory).
   *
   * @param path the path
   */
  private void select(Path path) {
    //Get the path from the base directory
    Path cleaned = cleanPath(path);
    if (path != null && this.matcher.matches(cleaned)) {
      LOGGER.debug("File Find: " + path.toString());
      this.filesFound.add(path.toFile());
    }
  }
  
  /**
   * Normalize path.
   *
   * @param path the path
   * @return the string
   */
  private String normalizePath (Path path) {
    return path.toString().replace('/', File.separatorChar).replace('\\', File.separatorChar);
  }
  
  /**
   * Clean path.
   *
   * @param path the path
   * @return the path
   */
  private Path cleanPath (Path path) {
    //Remove the base directory form the file path
    String newPathString = normalizePath(path).replace(this._baseDir, "");
    return Paths.get(newPathString);
  }
  
  /**
   * Gets the files found.
   *
   * @return the files found
   */
  public List<File> getFilesFound() {
    return filesFound;
  }

  /* (non-Javadoc)
   * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
   */
  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
    select(file);
    return CONTINUE;
  }  
  
  /* (non-Javadoc)
   * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
   */
  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    // Only files are selected 
    // select(dir);
    return CONTINUE;
  }

  /* (non-Javadoc)
   * @see java.nio.file.SimpleFileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
   */
  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) {
    LOGGER.error("Failed to visit {}, following error occurred {}.", file.toString(), exc.getMessage());
    return CONTINUE;
  }
}