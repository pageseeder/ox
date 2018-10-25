/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.util.ZipUtils;

/**
 * @author Carlos Cabral
 * @since 05 Feb. 2018
 */
public class TestZipUtils {

  public final static File _BASE_DIR = new File("src/test/resources/org/pageseeder/ox/util/filefinder");
  
  @Test
  public void testZipFilesTo() {
    try {
      File zip = new File(Files.createTempDirectory("TestZipFiles").toFile(), "test.zip");
      File file1 = new File(_BASE_DIR, "c.java");
      File file2 = new File(_BASE_DIR, "test.java");
      ZipUtils.zipFilesTo(zip, file1, file2);
      System.out.println(zip.getAbsolutePath());
    } catch (IOException ex) {
      Assert.fail("Failed: " + ex.getMessage());
    }
  }
}
