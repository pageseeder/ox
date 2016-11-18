/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
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
