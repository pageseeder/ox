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
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Carlos Cabral
 * @since 05 Feb. 2018
 */
public class ZipUtilsTest {

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
