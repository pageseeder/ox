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
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.core.PackageData;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
  public void write() {
    try {
      String fileContent = "First Line";
      File target = new File(OXConfig.getOXTempFolder(), "file-write.txt");
      FileUtils.write(fileContent, target);
      Assert.assertEquals(fileContent, FileUtils.read(target));
    } catch (IOException ex) {
      Assert.fail();
    }
  }

  @Test
  public void getMIMEType() {
    File sampleFile = new File("src/test/resources/models/m1/sample.xml");
    Assert.assertEquals("Get MIME type not working", "application/xml", FileUtils.getMimeType(sampleFile));
    sampleFile = new File("src/test/resources/models/m1/sample.html");
    Assert.assertEquals("Get MIME type not working", "text/html", FileUtils.getMimeType(sampleFile));
    sampleFile = new File("src/test/resources/models/m1/Sample.docx");
    Assert.assertEquals("Get MIME type not working", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", FileUtils.getMimeType(sampleFile));
    sampleFile = new File("src/test/resources/models/m1/sample.zip");
    Assert.assertEquals("Get MIME type not working", "application/zip", FileUtils.getMimeType(sampleFile));
    sampleFile = new File("src/test/resources/models/m1/sample.psml");
    Assert.assertEquals("Get MIME type not working", "application/vnd.pageseeder.psml+xml", FileUtils.getMimeType(sampleFile));
  }


  @Test
  public void getExtension() {
    File sampleFile = new File("src/test/resources/models/m1/sample.xml");
    Assert.assertEquals("Get Extension not working", "xml", FileUtils.getFileExtension(sampleFile));
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
      Assert.assertEquals("xml/config.xml",fileFound);
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
      String fileFound = FileUtils.getFileByExtension(data, "docx", ".dotx",".docx");
      Assert.assertEquals("docx/file.docx",fileFound);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void getDOCXFileByExtensionUppercase() {
    File sampleFile = new File("src/test/resources/models/m1/Sample.docx");
    File toCopy = new File("src/test/resources/org/pageseeder/ox/util/fileutils/file.docx");
    PackageData data = PackageData.newPackageData("m1", sampleFile);
    try {
      File dataFolder = new File(data.directory(), "docx");
      dataFolder.mkdirs();
      FileUtils.copy(toCopy, new File(dataFolder,  "file.DOCX"));
      String fileFound = FileUtils.getFileByExtension(data, "docx", ".dotx",".docx");
      Assert.assertEquals("docx/file.DOCX",fileFound);

//      //Extension parameter uppercase
//      fileFound = FileUtils.getFileByExtension(data, "DOCX");
//      Assert.assertEquals("docx/file.docx",fileFound);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void getDOCXFileByExtensionParameterUppercase() {
    File sampleFile = new File("src/test/resources/models/m1/Sample.docx");
    File toCopy = new File("src/test/resources/org/pageseeder/ox/util/fileutils/file.docx");
    PackageData data = PackageData.newPackageData("m1", sampleFile);
    try {
      File dataFolder = new File(data.directory(), "docx");
      dataFolder.mkdirs();
      FileUtils.copy(toCopy, new File(dataFolder,  "file.docx"));
      String fileFound = FileUtils.getFileByExtension(data, "docx", ".DOCX");
      Assert.assertEquals("docx/file.docx",fileFound);
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
      Assert.assertEquals("psml/file.psml",fileFound);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
     }
  }

  @Test
  public void findFileByExtensions() {
    File root = new File("src/test/resources/org/pageseeder/ox/util/filefinder");
    List<String> extensionsAllowed = new ArrayList<>();
    extensionsAllowed.add("java");
    extensionsAllowed.add("html");
    FileFilter filter = FileUtils.filter(extensionsAllowed, true);
    List<File> files = FileUtils.findFiles(root, filter);
    Assert.assertEquals(files.size(), 12);
    Assert.assertTrue(hasThisFile(files, "c.java"));
    Assert.assertTrue(hasThisFile(files, "a.java"));
    Assert.assertTrue(hasThisFile(files, "b.java"));
    Assert.assertTrue(hasThisFile(files, "c.html"));
    Assert.assertTrue(hasThisFile(files, "a.html"));
    Assert.assertTrue(hasThisFile(files, "b.html"));
  }

  @Test
  public void testGetNameWithoutExtension() {
    String filename = "fileV1.docx";
    Assert.assertEquals("fileV1", FileUtils.getNameWithoutExtension(filename));
    File file = new File (filename);
    Assert.assertEquals("fileV1", FileUtils.getNameWithoutExtension(file));

  }

  @Test
  public void findFileByExtensionsNoDirectory() {
    File root = new File("src/test/resources/org/pageseeder/ox/util/filefinder");
    List<String> extensionsAllowed = new ArrayList<>();
    extensionsAllowed.add("java");
    extensionsAllowed.add("html");
    FileFilter filter = FileUtils.filter(extensionsAllowed, false);
    List<File> files = FileUtils.findFiles(root, filter);
    Assert.assertEquals(files.size(), 4);
    Assert.assertTrue(hasThisFile(files, "c.java"));
    Assert.assertTrue(hasThisFile(files, "c.html"));
  }

  private boolean hasThisFile(List<File> files, String filename) {
    return files.stream().filter(file -> file.getName().equals(filename)).collect(Collectors.toList()).size() > 0;
  }
}
