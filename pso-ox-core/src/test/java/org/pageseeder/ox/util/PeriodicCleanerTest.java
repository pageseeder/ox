/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.util.PeriodicCleaner;

/**
 * @author Ciber Cai
 * @since 22 Jun 2016
 */
public class PeriodicCleanerTest {


  @Test
  public void test_clean() throws Exception {
    File file = Files.createTempDirectory(OXConfig.getOXTempUploadFolder().toPath(), "clean-test").toFile();
    file.mkdirs();
    File subFolder1 = new File(file, "1");
    subFolder1.mkdirs();
    File subFolder1File1 = new File(subFolder1, "1.txt");
    subFolder1File1.createNewFile();
    subFolder1File1.setLastModified(0);
    File subFolder1File2 = new File(subFolder1, "2.txt");
    subFolder1File2.createNewFile();
    subFolder1File2.setLastModified(0);

    File subFolder2 = new File(file, "2");
    subFolder2.mkdirs();
    File subFolder2File1 = new File(subFolder2, "1.txt");
    subFolder2File1.createNewFile();
    subFolder2File1.setLastModified(0);

    File subFolder2File2 = new File(subFolder2, "2.txt");
    subFolder2File2.createNewFile();
    subFolder2File2.setLastModified(0);

    File subFolder11 = new File(subFolder1, "1.1");
    subFolder11.mkdirs();

    File subFolder11File1 = new File(subFolder11, "1.txt");
    subFolder11File1.createNewFile();
    subFolder11File1.setLastModified(0);

    file.setLastModified(0);
    subFolder1.setLastModified(0);
    subFolder2.setLastModified(0);
    subFolder11.setLastModified(0);
    PeriodicCleaner.clean(file);
    Assert.assertFalse(file.exists());
  }
}
