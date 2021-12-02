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
import java.util.Map.Entry;

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
