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
import org.pageseeder.ox.core.PackageData;
import org.powermock.reflect.Whitebox;

import java.io.File;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class PSMLInspectorTest {

  private PSMLInspector inspector = null;

  @Before
  public void init() {
    this.inspector = new PSMLInspector();
  }

  @Test
  public void test_object() {
    Assert.assertNotNull(this.inspector);
    Assert.assertEquals("ox-psml-inspector", this.inspector.getName());
    Assert.assertTrue(this.inspector.supportsMediaType("application/vnd.pageseeder.psml+xml"));
    Assert.assertFalse(this.inspector.supportsMediaType("application/xml"));
  }

  @Test
  public void test_inspect() {
    File file = new File("src/test/resources/models/m1/sample.psml");
    PackageData data = PackageData.newPackageData("test", file);
    Assert.assertEquals(file.getName(), data.getOriginal().getName());

    this.inspector.inspect(data);
    Assert.assertEquals("US-ASCII", data.getProperty("psml.charset"));
    Assert.assertEquals("true", data.getProperty("psml.wellformedness"));
    Assert.assertNotNull(data.getProperty("psml.schemaversion"));
    Assert.assertNotNull(data.getProperty("psml.type"));

  }

  @Test
  public void test_method() throws Exception {
    File file = new File("src/test/resources/models/m1/sample.psml");
    File notExistile = new File("src/test/resources/models/m1/sample-random.psml");
    Assert.assertEquals(Boolean.TRUE, Whitebox.invokeMethod(PSMLInspector.class, "isWellformedness", file));
    Assert.assertEquals(Boolean.FALSE, Whitebox.invokeMethod(PSMLInspector.class, "isWellformedness", notExistile));
  }

}
