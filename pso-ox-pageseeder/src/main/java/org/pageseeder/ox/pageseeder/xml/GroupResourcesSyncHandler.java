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

import org.pageseeder.bridge.xml.BasicHandler;
import org.pageseeder.ox.pageseeder.model.GroupResourcesSync;
import org.xml.sax.Attributes;

/**
 * Handler for Group resources sync.
 *
 * @author Carlos Cabral
 * @since 26 November 2024
 */
public final class GroupResourcesSyncHandler extends BasicHandler<GroupResourcesSync> {

  @Override
  public void startElement(String element, Attributes atts) {
    if ("project".equals(element)) {
      GroupResourcesSync groupResourcesSync = makeGroupResourcesSync(atts);
      super.add(groupResourcesSync);
    }
  }

  /**
   * Make GroupResourcesSync
   *
   * @param atts the atts
   * @return the GroupResourcesSync
   */
  public GroupResourcesSync makeGroupResourcesSync(Attributes atts) {
    //It is required id or name
    String fromName = getString(atts, "from-name");
    String fromPSConfigName = getOptionalString(atts, "from-ps-config-name");
    String fromPerspective = getOptionalString(atts, "from-perspective");
    String toName = getString(atts, "to-name");
    String toPSConfigName = getOptionalString(atts, "to-ps-config-name");
    return new GroupResourcesSync(fromName, fromPSConfigName, fromPerspective, toName, toPSConfigName);
  }
}
