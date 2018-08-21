/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.utils;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.util.PeriodicCleaner;

/**
 * @author Ciber Cai
 * @since 22 Jun 2016
 */
public class PeriodicCleanerTest {

  private final File file = new File("test/folder");

  @Before
  public void init() throws Exception {
    this.file.mkdirs();

    File subFolder1 = new File(this.file, "1");
    subFolder1.mkdirs();
    subFolder1.setLastModified(0);
    File subFolder1File1 = new File(subFolder1, "1.txt");
    subFolder1File1.createNewFile();
    subFolder1File1.setLastModified(0);
    File subFolder1File2 = new File(subFolder1, "2.txt");
    subFolder1File2.createNewFile();
    subFolder1File2.setLastModified(0);

    File subFolder2 = new File(this.file, "2");
    subFolder2.mkdirs();
    subFolder2.setLastModified(0);
    File subFolder2File1 = new File(subFolder2, "1.txt");
    subFolder2File1.createNewFile();
    subFolder2File1.setLastModified(0);

    File subFolder2File2 = new File(subFolder2, "2.txt");
    subFolder2File2.createNewFile();
    subFolder2File2.setLastModified(0);

    File subFolder11 = new File(subFolder1, "1.1");
    subFolder11.mkdirs();
    subFolder11.setLastModified(0);

    File subFolder11File1 = new File(subFolder11, "1.txt");
    subFolder11File1.createNewFile();
    subFolder11File1.setLastModified(0);

    this.file.setLastModified(0);
  }

  @Test
  public void test_clean() throws Exception {
    PeriodicCleaner.clean(this.file);
    Assert.assertFalse(this.file.exists());
  }
}
