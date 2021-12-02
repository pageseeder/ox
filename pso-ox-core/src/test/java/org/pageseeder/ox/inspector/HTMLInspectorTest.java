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
package org.pageseeder.ox.inspector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;

import java.io.File;

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
