/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.inspector;

import java.io.File;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;

/**
 * @author Ciber Cai
 * @since 19 Jul 2016
 */
public class FileInspectorTest {

  private FileInspector inspector = null;

  @Before
  public void init() {
    this.inspector = new FileInspector();
  }

  @Test
  public void inspector() {
    Assert.assertNotNull(this.inspector);
    Assert.assertTrue(this.inspector instanceof PackageInspector);
    Assert.assertEquals("ox-file-inspector", this.inspector.getName());
    Assert.assertTrue(this.inspector.supportsMediaType("anythingt"));
  }

  @Test
  public void test_inspect() {
    File file = new File("src/test/resources/models/m1/sample.html");
    PackageData data = PackageData.newPackageData("test", file);

    Assert.assertEquals(file.getName(), data.getOriginal().getName());

    this.inspector.inspect(data);
    for (Entry<String, String> entry : data.getProperties().entrySet()) {
      System.out.println(entry.getKey() + " " + entry.getValue());
    }
  }

  @Test
  public void test_inspect_folder() {
    File file = new File("src/test/resources");
    PackageData data = PackageData.newPackageData("test", file);

    Assert.assertEquals(file.getName(), data.getOriginal().getName());

    this.inspector.inspect(data);
    for (Entry<String, String> entry : data.getProperties().entrySet()) {
      System.out.println(entry.getKey() + " " + entry.getValue());
    }

    Assert.assertNull(data.getProperty("html.paragraphs"));
    Assert.assertNull(data.getProperty("html.headings"));
  }

}
