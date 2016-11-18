/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.inspector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class ZipInspectorTest {

  private ZipInspector inspector = null;

  @Before
  public void init() {
    this.inspector = new ZipInspector();
  }

  @Test
  public void test_object() {
    Assert.assertNotNull(this.inspector);
    Assert.assertEquals("ox-zip-inspector", this.inspector.getName());
    Assert.assertTrue(this.inspector.supportsMediaType("application/zip"));
    Assert.assertFalse(this.inspector.supportsMediaType("application/xml"));
  }

}
