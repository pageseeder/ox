/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.util;

import org.junit.Assert;
import org.junit.Test;

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
