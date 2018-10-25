/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.utils;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.util.FileSizeFormat;

/**
 * @author Ciber Cai
 * @since 19 Jul 2016
 */
public class FileSizeFormatTest {

  @Test
  public void format() {
    FileSizeFormat fmt = new FileSizeFormat();
    Assert.assertEquals("1Byte", fmt.format(1, FileSizeFormat.Unit.BYTE));
    Assert.assertEquals("1KB", fmt.format(1024, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("1KB", fmt.format(1025, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("1.5KB", fmt.format(1500, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("2KB", fmt.format(2049, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("1MB", fmt.format(1024 * 1024, FileSizeFormat.Unit.MEGA_BYTE));

    Assert.assertEquals("1Byte", fmt.format(1));
    Assert.assertEquals("1KB", fmt.format(1024));
    Assert.assertEquals("1KB", fmt.format(1025));
    Assert.assertEquals("1.5KB", fmt.format(1500));
    Assert.assertEquals("2KB", fmt.format(2049));
    Assert.assertEquals("1MB", fmt.format(1024 * 1024));
  }

}
