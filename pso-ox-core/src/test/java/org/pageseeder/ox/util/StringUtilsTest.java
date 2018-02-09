/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Carlos Cabral
 * @since 05 Feb. 2018
 */
public class StringUtilsTest {

  
  @Test
  public void testIsCommaSeparateFileList() {
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("c:\\path\\file.java,/root/file2.html,c:/path/file 3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("c:\\path\\file_1.java,/root/file-2.html,c:/path/file 3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("file.java,file2.html,file3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList(" file_1.java, file-2.html, file 3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("c:\\path\\file.java,/root/file2.html"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("file.java,file2.html"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java,file2.html,"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList(",file.java,file2.html"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java,"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList(",file.java"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java"));    
  }
}
