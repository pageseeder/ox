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
package org.pageseeder.ox.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PackageDataTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
  }

  @Test
  public void test_not_null_for_packageData() {
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), null);
    Assert.assertNotNull(data);
  }

  @Test
  public void test_get_File_for_packageData() throws IOException {
    File sampleFile = new File("src/test/resources/models/m1/sample.zip");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);

    File file = data.getOriginal();
    Assert.assertEquals(file.getName(), sampleFile.getName());
    File expectedFile = data.getFile("sample.zip");
    Assert.assertTrue(expectedFile.exists());
  }

  @Test
  public void test_properties() {
    File sampleFile = new File("src/test/resources/models/m1/sample.zip");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);

    Assert.assertNotNull(data.getProperties());

    // not exist
    Assert.assertNull(data.getProperty("not-exists"));
    Assert.assertEquals("default", data.getProperty("not-exists", "default"));

    // set
    data.setProperty("not-exists", "new");
    Assert.assertNotNull(data.getProperty("not-exists"));
    Assert.assertEquals("new", data.getProperty("not-exists", "default"));

  }

  @Test
  public void test_unpack() throws Exception {
    File sampleFile = new File("src/test/resources/models/m1/sample.docx");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);
    Assert.assertFalse(data.isUnpacked());

    // unpack the docx
    if (!data.isUnpacked()) {
      data.unpack();
    }
    Assert.assertTrue(data.isUnpacked());
  }

  @Test
  public void test_get_packageDate() {
    File sampleFile = new File("src/test/resources/models/m1/sample.docx");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);
    String id = data.id();
    Assert.assertNotNull(id);
    PackageData loadedData = PackageData.getPackageData(id);

    Assert.assertEquals(data.id(), loadedData.id());
    Assert.assertEquals(data.getOriginal(), loadedData.getOriginal());
  }

  @Test
  public void test_getPath() {
    File sampleFile = new File("src/test/resources/models/m1/sample.docx");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);
    String id = data.id();
    Assert.assertNotNull(id);

    File inPackageDateFile = new File(data.directory(), "sample.docx");
    Assert.assertThat(data.getPath(inPackageDateFile), org.hamcrest.core.Is.is("/sample.docx"));

  }

  @Test
  public void test_getFile() {
    File sampleFile = new File("src/test/resources/models/m1/sample.xml");
    File toCopy1 = new File("src/test/resources/org/pageseeder/ox/util/filefinder/c.html");
    File toCopy2 = new File("src/test/resources/org/pageseeder/ox/util/filefinder/folder1/a.html");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);
    try {
      File destinationFolder = new File(data.directory(),  "filefinder");
      destinationFolder.mkdir();
      FileUtils.copy(toCopy1, new File(destinationFolder, "c.html"));
      FileUtils.copy(toCopy2, new File(destinationFolder, "folder1/a.html"));
      File test = data.getFile("sample.xml");
      Assert.assertNotNull(test);
      Assert.assertTrue(test.exists());
      test = data.getFile("filefinder/c.html");
      Assert.assertNotNull(test);
      Assert.assertTrue(test.exists());
      test = data.getFile("filefinder/folder1/a.html");
      Assert.assertNotNull(test);
      Assert.assertTrue(test.exists());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void test_getFileByUsingGlobPattern() {
    File sampleFile = new File("src/test/resources/models/m1/sample.xml");
    File toCopy1 = new File("src/test/resources/org/pageseeder/ox/util/filefinder/c.html");
    File toCopy2 = new File("src/test/resources/org/pageseeder/ox/util/filefinder/folder1/a.html");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);
    try {
      File destinationFolder = new File(data.directory(),  "filefinder");
      destinationFolder.mkdir();
      FileUtils.copy(toCopy1, new File(destinationFolder, "c.html"));
      FileUtils.copy(toCopy2, new File(destinationFolder, "folder1/a.html"));
      File test = data.getFile("*.xml");
      Assert.assertNotNull(test);
      Assert.assertEquals("sample.xml", test.getName());
      Assert.assertTrue(test.exists());
      test = data.getFile("*.html");
      Assert.assertNull(test);
      test = data.getFile("**/*.html");
      Assert.assertNotNull(test);
      Assert.assertEquals("c.html", test.getName());
      Assert.assertTrue(test.exists());
      test = data.getFile("filefinder/folder1/**.html");
      Assert.assertNotNull(test);
      Assert.assertEquals("a.html", test.getName());
      Assert.assertTrue(test.exists());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void test_getFilesByUsingGlobPattern() {
    File sampleFile = new File("src/test/resources/models/m1/sample.xml");
    File toCopy1 = new File("src/test/resources/org/pageseeder/ox/util/filefinder/c.html");
    File toCopy2 = new File("src/test/resources/org/pageseeder/ox/util/filefinder/folder1/a.html");
    PackageData data = PackageData.newPackageData(String.valueOf(System.nanoTime()), sampleFile);
    try {
      File destinationFolder = new File(data.directory(),  "filefinder");
      destinationFolder.mkdir();
      FileUtils.copy(toCopy1, new File(destinationFolder, "c.html"));
      FileUtils.copy(toCopy2, new File(destinationFolder, "folder1/a.html"));
      List<File> files = data.getFiles("**.html");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==2);
      files = data.getFiles("**/*.html");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==2);
      files = data.getFiles("**.*ml");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==3);
      files = data.getFiles("**.{xml,html}");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==3);
      files = data.getFiles("**/*.*ml");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==2);
      files = data.getFiles("**/folder1/*.*ml");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==1);
      files = data.getFiles("**/[a].*ml");
      Assert.assertNotNull(files);
      Assert.assertTrue(files.size()==1);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
