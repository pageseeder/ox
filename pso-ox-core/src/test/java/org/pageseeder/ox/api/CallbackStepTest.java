/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.api;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.core.PackageData;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Method;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class CallbackStepTest {

  @Test
  public void test_interface() {
    CallbackStep step = new CallbackStep() {

      @Override
      public void process(PackageData data, Result result, StepInfo info) {
        // do nothing.
      }
    };

    Assert.assertNotNull(step);

    Method[] methods = Whitebox.getMethods(CallbackStep.class, "process");
    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.length);
  }

}
