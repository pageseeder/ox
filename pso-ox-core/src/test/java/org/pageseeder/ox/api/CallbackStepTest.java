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
package org.pageseeder.ox.api;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Method;

/**
 * The type Callback step test.
 *
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class CallbackStepTest {

  /**
   * Test interface.
   */
  @Test
  public void test_interface() {
    CallbackStep step = new CallbackStep() {

      @Override
      public void process(Model model, PackageData data, Result result, StepInfo info) {
        // do nothing.
      }
    };

    Assert.assertNotNull(step);

    Method[] methods = Whitebox.getMethods(CallbackStep.class, "process");
    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.length);
  }

}
