/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.utils;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.util.GlobPatternUtils;

/**
 * @author Carlos Cabral
 * @since 05 Feb. 2018
 */
public class GlobPatternUtilsTest {

  @Test
  public void findByExtension() {
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("*.java"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("**/*.java"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("**.java"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("*.{html,java}"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("**/test*.{java,html}"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("**test*.{java,html}"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("*folder1/**"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("*folder1/*test*"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("?.java"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("[abc].java"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("[a-c].java"));
    Assert.assertTrue(GlobPatternUtils.isGlobPattern("[!a].java"));
        

    Assert.assertFalse(GlobPatternUtils.isGlobPattern("/folder/test.java"));
    Assert.assertFalse(GlobPatternUtils.isGlobPattern("test-01.java"));
    Assert.assertFalse(GlobPatternUtils.isGlobPattern("test.java"));
    Assert.assertFalse(GlobPatternUtils.isGlobPattern(".java"));
  }
}
