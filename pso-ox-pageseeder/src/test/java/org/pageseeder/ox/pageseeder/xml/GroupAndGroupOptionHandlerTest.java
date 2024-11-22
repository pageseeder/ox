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

import net.pageseeder.app.simple.pageseeder.model.GroupOptions;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSNotification;
import org.pageseeder.bridge.model.PSRole;
import org.pageseeder.ox.pageseeder.model.GroupAndGroupOptionWrapper;
import org.pageseeder.ox.util.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ccabral
 * @since 22 November 2024
 */
public class GroupAndGroupOptionHandlerTest {

  /**
   * Test handler.
   */
  @Test
  public void testHandler(){
    final File inputXML = new File("src/test/resources/org/pageseeder/ox/pageseeder/xml/group/groups.xml");
    final List<GroupAndGroupOptionWrapper> groupsList = parse(inputXML);
    Assert.assertNotNull(groupsList);
    Assert.assertEquals(7, groupsList.size());
    validateWrapper(groupsList.get(0), 0);
    validateWrapper(groupsList.get(1), 1);
    validateWrapper(groupsList.get(2), 2);
    validateWrapper(groupsList.get(3), 3);
    validateWrapper(groupsList.get(4), 4);
  }

  private void validateWrapper(GroupAndGroupOptionWrapper wrapper, int position) {
    Assert.assertNotNull(wrapper);
    switch (position) {
      case 0:
        validateWrapper(wrapper, 3726L, "domestic", "domestic", "domestic",
            null, null, "domestic", PSRole.reviewer, PSNotification.immediate,
            new HashMap<>(), "member", "public", false, false, null,
            "none", "normal", null, "domestic");
        break;
      case 1:
        validateWrapper(wrapper, 3727L, "domestic-editorial", "editorial", "domestic",
            null, null, "domestic", PSRole.reviewer, PSNotification.immediate,
            new HashMap<>(), "member", "public", false, false, null,
            "none", "normal", null, "domestic-editorial");
        break;
      case 2:
        validateWrapper(wrapper, 4L, "natspec", "National Building Specification", "natspec",
          null, null, "natspec", PSRole.reviewer, PSNotification.essential,
          new HashMap<>(), "member", "public", false, false, null,
          "none", "normal", null, "natspec");
        break;
      case 3:
        validateWrapper(wrapper, 3725L, "natspec-editorial", "editorial", "natspec",
            "editorial", null, "natspec", PSRole.reviewer, PSNotification.immediate,
            new HashMap<>(), "member", "public", false, false, "editorial",
            "none", "normal", null, "natspec-editorial");
        break;
      case 4:
        validateWrapper(wrapper, 3722L, "spec", "Specbuilder website NATSPEC related contents", "spec",
            null, null, "spec", PSRole.reviewer, PSNotification.immediate,
            new HashMap<>(), "member", "public", false, false, null,
            "none", "normal", null, "spec");
        break;

    }
  }

  private void validateWrapper(GroupAndGroupOptionWrapper wrapper, Long id, String name, String description,
                               String owner, String title, String detailstype, String template, PSRole defaultRole,
                               PSNotification defaultNotify, Map<String, String> properties, String access,
                               String commenting, boolean common, boolean editurls, String message, String moderation,
                               String registration, String relatedurl, String visibility) {

    PSGroup group = wrapper.getGroup();
    GroupOptions options = wrapper.getGroupOptions();
    Assert.assertEquals(id, group.getId());
    Assert.assertEquals(name, group.getName());
    Assert.assertEquals(description, group.getDescription());
    Assert.assertEquals(owner, group.getOwner());
    Assert.assertEquals(title, group.getTitle());
    Assert.assertEquals(detailstype, group.getDetailsType());
    Assert.assertEquals(template, group.getTemplate());
    Assert.assertEquals(defaultRole, group.getDefaultRole());
    Assert.assertEquals(defaultNotify, group.getDefaultNotification());
    Assert.assertEquals(access, options.getAccess());
    Assert.assertEquals(commenting, options.getCommenting());
    Assert.assertEquals(common, options.isCommon());
    Assert.assertEquals(editurls, options.isEditurls());
    Assert.assertEquals(message, options.getMessage());
    Assert.assertEquals(moderation, options.getModeration());
    Assert.assertEquals(registration, options.getRegistration());
    Assert.assertEquals(relatedurl, options.getRelatedurl());
    Assert.assertEquals(visibility, options.getVisibility());
    if (properties != null) {
      Assert.assertEquals(properties, options.getProperties());
    } else {
      Assert.assertNull(options.getProperties());
    }


//    //It is required id or name
//    Long id, String name, String description, String owner, String title,
//    String detailstype, String template, PSRole defaultRole, PSNotification defaultNotify,
//    Map<String, String> properties, String access, String commenting, boolean common,
//    boolean editurls, String message, String moderation, String registration, String relatedurl,
//    String visibility
  }

  private List<GroupAndGroupOptionWrapper> parse(File workbookXml) {

    final GroupAndGroupOptionHandler handler = new GroupAndGroupOptionHandler();

    try (FileInputStream fiStream  = new FileInputStream(workbookXml)) {
      XMLUtils.parseXML(fiStream, handler);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return handler.list();
  }
}
