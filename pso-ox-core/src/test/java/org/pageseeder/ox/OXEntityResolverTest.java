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
package org.pageseeder.ox;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class OXEntityResolverTest {

  private OXEntityResolver resolver = null;

  @Before
  public void init() {
    this.resolver = OXEntityResolver.getInstance();
  }

  @Test
  public void test_object() {
    Assert.assertNotNull(this.resolver);
  }

  @Test
  public void test_public_id() throws Exception {
    Assert.assertNull(Whitebox.invokeMethod(OXEntityResolver.class, "toFileName", "random"));
    Assert.assertNotNull(Whitebox.invokeMethod(OXEntityResolver.class, "toFileName", "-//PageSeeder//DTD::OX model 1.0//EN"));
    Assert.assertEquals("model-1.0.dtd", Whitebox.invokeMethod(OXEntityResolver.class, "toFileName", "-//PageSeeder//DTD::OX model 1.0//EN"));

    Assert.assertNotNull(Whitebox.invokeMethod(OXEntityResolver.class, "toFileName", "-//OX//DTD:: model 1.0//EN"));
    Assert.assertEquals("model-1.0.dtd", Whitebox.invokeMethod(OXEntityResolver.class, "toFileName", "-//OX//DTD::model 1.0//EN"));

  }

}
