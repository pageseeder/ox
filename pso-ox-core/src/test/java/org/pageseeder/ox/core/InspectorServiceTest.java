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
