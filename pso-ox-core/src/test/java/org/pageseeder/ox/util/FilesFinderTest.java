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

import java.io.File;
import java.util.List;

/**
 * @author Carlos Cabral
 * @since 05 Feb. 2018
 */
public class FilesFinderTest {

  public final static File _BASE_DIR = new File("src/test/resources/org/pageseeder/ox/util/filefinder");

/**
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
 */

  @Test
  public void findByExtension() {
    exec("*.java", "c.java","test.java");//2
    exec("**/*.java", "folder1/a.java","folder1/test01.java","folder2/b.java","folder2/test02.java");//4
    exec("**.java", "c.java","folder1/a.java","folder1/test01.java","folder2/b.java","folder2/test02.java","test.java");//6
  }

  @Test
  public void findByMultipleExtension() {
    exec("*.{html,java}", "c.html", "c.java","test.html","test.java");//2
    exec("**/test*.{java,html}", "folder1/test01.html","folder1/test01.java","folder2/test02.html","folder2/test02.java");//4
    exec("**test*.{java,html}", "folder1/test01.html","folder1/test01.java","folder2/test02.html","folder2/test02.java","test.html","test.java");//6
  }

  @Test
  public void findByDirectory() {
    exec("*folder1/**", "folder1/a.html","folder1/a.java","folder1/a.txt","folder1/test01.html","folder1/test01.java","folder1/test01.txt");
    exec("*folder1/*test*", "folder1/test01.html","folder1/test01.java","folder1/test01.txt");
    exec("folder1/**", "folder1/a.html","folder1/a.java","folder1/a.txt","folder1/test01.html","folder1/test01.java","folder1/test01.txt");
    exec("folder1/*test*", "folder1/test01.html","folder1/test01.java","folder1/test01.txt");
  }

  @Test
  public void findBySingleCharactter() {
    exec("?.java", "c.java");
    exec("[abc].java", "c.java");
    exec("[a-c].java", "c.java");
    exec("[!a].java", "c.java");
    exec("[!c].java", new String[]{});
  }

  private void exec(String pattern, String ... expectedResults) {
    FilesFinder finder = new FilesFinder(pattern, _BASE_DIR);
    List<File> files = finder.getFiles();
    Assert.assertTrue(files.size() == expectedResults.length);
    for (int i = 0; i < expectedResults.length; i++) {
      File file = files.get(i);
      Assert.assertTrue(normalizePath(file.getAbsolutePath()).endsWith(normalizePath(expectedResults[i])));
    }
  }


  /**
   * Normalize path.
   *
   * @param path the path
   * @return the string
   */
  private String normalizePath (String path) {
    return path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
  }
}
