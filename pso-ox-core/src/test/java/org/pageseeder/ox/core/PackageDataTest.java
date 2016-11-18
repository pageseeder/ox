/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.core;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;

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

}
