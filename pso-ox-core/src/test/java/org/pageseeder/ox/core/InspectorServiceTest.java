/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ciber Cai
 * @since 16 June 2016
 */
public class InspectorServiceTest {

  @Test
  public void test_inspector_by_type() {
    InspectorService service = InspectorService.getInstance();
    service.reload();
    Assert.assertNotNull(service);
    Assert.assertNotNull(service.getInspectors("something_unknown"));
    Assert.assertNotNull(service.getInspectors("application/zip"));
    Assert.assertNotNull(service.getInspectors("application/vnd.pageseeder.psml+xml"));
    Assert.assertNotNull(service.getInspectors("text/html"));

    Assert.assertTrue(service.getInspectors("application/zip").size() > 0);
    Assert.assertTrue(service.getInspectors("application/vnd.pageseeder.psml+xml").size() > 0);
    Assert.assertTrue(service.getInspectors("text/html").size() > 0);

  }

}
