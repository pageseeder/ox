/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.OXException;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class OXExceptionTest {

  @Test
  public void test_Object() {
    Assert.assertNotNull(new OXException());
  }

}
