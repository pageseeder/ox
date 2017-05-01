/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.core.PackageData;


/**
 * @author Adriano Akaishi  
 * @since 01/05/2017
 */
public class FileUtilsTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
  }
  
  @Test
  public void getXMLFileByExtension() {
    File sampleFile = new File("src/test/resources/models/m1/sample.xml");
    File toCopy = new File("src/test/resources/org/pageseeder/ox/util/fileutils/config.xml");
    PackageData data = PackageData.newPackageData("m1", sampleFile);
    try {
      File dataFolder = new File(data.directory(), "xml");
      dataFolder.mkdirs();
      FileUtils.copy(toCopy, new File(dataFolder,  "config.xml"));
      String fileFound = FileUtils.getFileByExtension(data, "xml", ".xml");
      Assert.assertEquals("folder/config.xml",fileFound);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void getDOCXFileByExtension() {
    File sampleFile = new File("src/test/resources/models/m1/Sample.docx");
    File toCopy = new File("src/test/resources/org/pageseeder/ox/util/fileutils/file.docx");
    PackageData data = PackageData.newPackageData("m1", sampleFile);
    try {
      File dataFolder = new File(data.directory(), "docx");
      dataFolder.mkdirs();
      FileUtils.copy(toCopy, new File(dataFolder,  "file.docx"));
      String fileFound = FileUtils.getFileByExtension(data, "dotx", ".dotx",".docx");
      Assert.assertEquals("folder/file.docx",fileFound);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void getPSMLFileByExtension() {
    File sampleFile = new File("src/test/resources/models/m1/sample.psml");
    File toCopy = new File("src/test/resources/org/pageseeder/ox/util/fileutils/file.psml");
    PackageData data = PackageData.newPackageData("m1", sampleFile);
    try {
      File dataFolder = new File(data.directory(), "psml");
      dataFolder.mkdirs();
      FileUtils.copy(toCopy, new File(dataFolder,  "file.psml"));
      String fileFound = FileUtils.getFileByExtension(data, "psml", ".psml");
      Assert.assertEquals("folder/file.psml",fileFound);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
     }
  }


}
