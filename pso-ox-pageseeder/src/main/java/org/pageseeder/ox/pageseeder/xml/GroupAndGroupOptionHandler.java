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
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.xml.BasicHandler;
import org.pageseeder.bridge.xml.MissingAttributeException;
import org.pageseeder.bridge.xml.PSHandlers;
import org.pageseeder.ox.pageseeder.model.GroupAndGroupOptionWrapper;
import org.xml.sax.Attributes;

/**
 * Handler for PageSeeder groups and projects.
 *
 * @author Carlos Cabral
 * @since 21 November 2024
 */
public final class GroupAndGroupOptionHandler extends BasicHandler<GroupAndGroupOptionWrapper> {

  @Override
  public void startElement(String element, Attributes atts) {
    if ("group".equals(element) || "project".equals(element)) {
      PSGroup group = makeGroup(atts);
      GroupOptions groupOptions = makeGroupOption(atts);
      super.add(new GroupAndGroupOptionWrapper(group, groupOptions));
    } else if ("message".equals(element)) {
      super.newBuffer();
    }
  }

  @Override
  public void endElement(String element) {
    if ("message".equals(element)) {
      super.get().getGroupOptions().setMessage(super.buffer(true));
    }
  }

  /**
   * Make group ps group.
   *
   * @param atts the atts
   * @return the ps group
   */
  public PSGroup makeGroup(Attributes atts) {
    //It is required id or name
    Long id = getOptionalLong(atts, "id");
    String name = getOptionalString(atts, "name");
    if (id == null & name == null) {
      throw new MissingAttributeException("The group/project requires an id or name.");
    }

    PSGroup g = new PSGroup();
    g.setId(id);
    g.setName(name);

    // Core attributes
    g.setDescription(getOptionalString(atts, "description"));
    g.setTitle(getOptionalString(atts, "title"));
    g.setOwner(getOptionalString(atts, "owner"));

    // Extended attributes
    g.setDetailsType(getOptionalString(atts, "detailstype"));
    g.setTemplate(getOptionalString(atts, "template"));
    g.setDefaultRole(PSHandlers.role(atts.getValue("defaultrole")));
    g.setDefaultNotification(PSHandlers.notification(atts.getValue("defaultnotify")));

    return g;
  }

  /**
   * Make group option group options.
   *
   * @param atts the atts
   * @return the group options
   */
  public GroupOptions makeGroupOption(Attributes atts) {
    GroupOptions opts = new GroupOptions();

    for (int i = 0; i < atts.getLength(); i++) {
      String name = atts.getQName(i);
      if (name == null) {
        name = atts.getLocalName(i);
      }
      String value = atts.getValue(i);

      if (name.startsWith("defaultproperty.") || name.startsWith("property.")) {
        opts.setProperty(name, value);
      } else {
        switch (name) {
          case "access":
            opts.setAccess(value);
            break;
          case "commenting":
            opts.setCommenting(value);
            break;
          case "common":
            //Deprecated 5.99
            opts.setCommon("true".equalsIgnoreCase(value));
            break;
          case "editurls":
            opts.setEditurls("true".equalsIgnoreCase(value));
            break;
          case "message":
            opts.setMessage(value);
            break;
          case "moderation":
            opts.setModeration(value);
            break;
          case "registration":
            opts.setRegistration(value);
            break;
          case "relatedurl":
          opts.setRelatedurl(value);
            break;
          case "visibility":
            opts.setVisibility(value);
            break;
          case "addmember":
            opts.setAddCreatorAsMember("false".equalsIgnoreCase(value));
            break;
        }
      }
    }
    return opts;
  }

}
