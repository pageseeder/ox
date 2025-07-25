/*
 * Copyright 2024 Allette Systems (Australia)
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
package org.pageseeder.ox.pageseeder.xml;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.pageseeder.model.GroupResourcesSync;
import org.pageseeder.ox.util.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author ccabral
 * @since 26 November 2024
 */
public class GroupResourcesSyncHandlerTest {

  /**
   * Test handler.
   */
  @Test
  public void testHandler(){
    final File inputXML = new File("src/test/resources/org/pageseeder/ox/pageseeder/xml/group/projects-resources.xml");
    final List<GroupResourcesSync> groupResourcesList = parse(inputXML);
    Assert.assertNotNull(groupResourcesList);
    Assert.assertEquals(3, groupResourcesList.size());
    validateWrapper(groupResourcesList.get(0), 0);
    validateWrapper(groupResourcesList.get(1), 1);
    validateWrapper(groupResourcesList.get(2), 2);
  }

  private void validateWrapper(GroupResourcesSync groupResourcesList, int position) {
    Assert.assertNotNull(groupResourcesList);
    switch (position) {
      case 0:
        validateGroupResources(groupResourcesList, "group01", "staging", "test",
            "group01", "new");
        break;
      case 1:
        validateGroupResources(groupResourcesList, "group02", "staging", "test",
            "group03", "staging");
        break;
      case 2:
        validateGroupResources(groupResourcesList, "group04", null, null,
            "group05", null);
        break;
    }
  }

  private void validateGroupResources(GroupResourcesSync groupResources, String fromName, String fromPSConfgiName,
                               String fromPerspective, String toName, String toPSConfigName) {
    Assert.assertEquals(fromName, groupResources.getFromProjectName());
    Assert.assertEquals(fromPSConfgiName, groupResources.getFromPSConfigName());
    Assert.assertEquals(fromPerspective, groupResources.getFromPerspective());
    Assert.assertEquals(toName, groupResources.getToProjectName());
    Assert.assertEquals(toPSConfigName, groupResources.getToPSConfigName());
  }

  private List<GroupResourcesSync> parse(File workbookXml) {

    final GroupResourcesSyncHandler handler = new GroupResourcesSyncHandler();

    try (FileInputStream fiStream  = new FileInputStream(workbookXml)) {
      XMLUtils.parseXML(fiStream, handler);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return handler.list();
  }
}
