/*
 * Copyright (c) 2021 Allette systems pty. ltd.
 */
package org.pageseeder.ox.xml.utils;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The FilesComparator.
 *
 * @author Carlos Cabral
 * @since 29 March 2021
 */
public class FilesComparator {

  /** The expected results base directory or file. */
  private final File _expectedResultsBaseDirectoryOrFile;

  /** The result base directory or file. */
  private final File _resultBaseDirectoryOrFile;

  /** The files to ignore. */
  private final List<File> _filesToIgnore;

  /**
   * Has the logic to compare file or directory.
   *
   * @param expectedResultsBaseDirectoryOrFile the expected results base directory or file
   * @param resultBaseDirectoryOrFile the results base directory or file
   * @param filesToIgnore the files to ignore (the path should be based on the expected result base directory or file)
   */
  public FilesComparator(File expectedResultsBaseDirectoryOrFile, File resultBaseDirectoryOrFile, List<File> filesToIgnore) {
    super();
    this._expectedResultsBaseDirectoryOrFile = expectedResultsBaseDirectoryOrFile;
    this._resultBaseDirectoryOrFile = resultBaseDirectoryOrFile;
    this._filesToIgnore = filesToIgnore;
  }

  /**
   * Compare.
   */
  public void compare() {
    if (this._expectedResultsBaseDirectoryOrFile.isFile()) {
      compareFile(this._expectedResultsBaseDirectoryOrFile);
    } else {
      compareDirectory(this._expectedResultsBaseDirectoryOrFile);
    }
  }

  /**
   * Compare directory.
   *
   * @param expected the expected
   */
  public void compareDirectory(File expected) {
    if (!shouldIgnore(expected)) {
      File equivalentResult = getEquivalentFileResult(expected);
      Assert.assertTrue("Equivalent result directory does not exist: " + equivalentResult.getAbsolutePath(), equivalentResult.exists());

      for(File nextExpected:expected.listFiles()) {
        if (nextExpected.isDirectory()) {
          compareDirectory(nextExpected);
        } else {
          compareFile(nextExpected);
        }
      }
    }
  }

  /**
   * Compare file.
   *
   * @param expected the expected
   */
  public void compareFile(File expected) {
    if (!shouldIgnore(expected)) {
      File equivalentResult = getEquivalentFileResult(expected);
      Assert.assertTrue("Equivalent result file does not exist: " + equivalentResult.getAbsolutePath(), equivalentResult.exists());
      String filename = equivalentResult.getName();

      if (filename.endsWith("xml") || filename.endsWith("psml") || filename.endsWith("html") || filename.endsWith("xhtml") || filename.endsWith("htm")) {
        compareXMLFile(expected, equivalentResult);
      } else {
        compareGenericFile(expected, equivalentResult);
      }
    }
  }

  /**
   * Compare XML file.
   *
   * @param expected the expected
   * @param target the target
   */
  private void compareXMLFile(File expected, File target) {
    XMLComparator.compareXMLFile(expected, target);
  }

  /**
   * Compare generic file.
   *
   * @param expected the expected
   * @param target the target
   */
  private void compareGenericFile(File expected, File target) {
    long expectedSize = expected.length();
    long targetSize = target.length();
    System.out.println("Expected: " + expected.getAbsolutePath());
    System.out.println("Target: " + target.getAbsolutePath());
    Assert.assertEquals("The size are differents expected " + expectedSize + " target " + targetSize, expectedSize, targetSize);
  }

  /**
   * Should ignore.
   *
   * @param candidate the candidate
   * @return true, if successful
   */
  private boolean shouldIgnore(File candidate) {
    boolean shouldIgnore = false;
    if (this._filesToIgnore != null) {
      for (File file:this._filesToIgnore) {
        if (candidate.getAbsolutePath().equals(file.getAbsolutePath())) {
          shouldIgnore = true;
          break;
        }
      }
    }
    return shouldIgnore;
  }

  /**
   * Compare file.
   *
   * @param expected the expected
   * @throws IOException
   */
  public File getEquivalentFileResult(File expected) {
    String expectedFileName = expected.getAbsolutePath().replace(this._expectedResultsBaseDirectoryOrFile.getAbsolutePath(), "");
    File equivalentResult = null;
    if (expectedFileName.equals("")) {
      if (expected.isFile()) {
        equivalentResult = new File(this._resultBaseDirectoryOrFile, this._expectedResultsBaseDirectoryOrFile.getName());
      } else {
        equivalentResult = this._resultBaseDirectoryOrFile;
      }
    } else {
      equivalentResult = new File(this._resultBaseDirectoryOrFile, expectedFileName);
    }
    return equivalentResult;
  }
}
