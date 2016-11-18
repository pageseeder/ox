/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.inspector;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class HTMLInspectorTest {

  private HTMLInspector inspector = null;

  @Before
  public void init() {
    this.inspector = new HTMLInspector();
  }

  @Test
  public void test_object() {
    Assert.assertNotNull(this.inspector);
    Assert.assertTrue(this.inspector instanceof PackageInspector);
    Assert.assertEquals("ox-html-inspector", this.inspector.getName());
    Assert.assertFalse(this.inspector.supportsMediaType("application/html"));
    Assert.assertTrue(this.inspector.supportsMediaType("text/html"));
  }

  @Test
  public void test_inspect() {
    File file = new File("src/test/resources/models/m1/sample.html");
    PackageData data = PackageData.newPackageData("test", file);
    Assert.assertEquals(file.getName(), data.getOriginal().getName());

    Assert.assertNull(data.getProperty("html.paragraphs"));
    Assert.assertNull(data.getProperty("html.headings"));
    Assert.assertNull(data.getProperty("html.tables"));
    Assert.assertNull(data.getProperty("html.images"));
    Assert.assertNull(data.getProperty("html.lists"));
    this.inspector.inspect(data);
    Assert.assertEquals("38", data.getProperty("html.paragraphs"));
    Assert.assertEquals("28", data.getProperty("html.headings"));
    Assert.assertEquals("0", data.getProperty("html.tables"));
    Assert.assertEquals("2", data.getProperty("html.images"));
    Assert.assertEquals("23", data.getProperty("html.lists"));

  }

}
