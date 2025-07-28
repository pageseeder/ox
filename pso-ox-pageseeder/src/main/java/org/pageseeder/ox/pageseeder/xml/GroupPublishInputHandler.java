/*
 * Copyright 2022 Allette Systems (Australia)
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

import org.pageseeder.ox.pageseeder.model.GroupPublish;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GroupPublishInputHandler extends DefaultHandler {

  List<GroupPublish> publishes;

  public GroupPublishInputHandler() {
    this.publishes = new ArrayList<>();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (localName.equals("publish")) {
      this.publishes.add(new GroupPublish(
          attributes.getValue("project"),
          attributes.getValue("group"),
          attributes.getValue("member"),
          attributes.getValue("target"),
          attributes.getValue("type"),
          attributes.getValue("log-level"),
          mapParametersFromAttributes(attributes)
      ));
    }
  }

  private static HashMap<String, String> mapParametersFromAttributes(Attributes attributes) {
    HashMap<String, String> parameters = new HashMap<>();
    List<String> attributesToExclude = Arrays.asList("project", "group", "target", "type","log-level", "member");
    for (int i = 0; i < attributes.getLength(); i++) {
      String aname = attributes.getLocalName(i) == null ? attributes.getQName(i) : attributes.getLocalName(i);
      if(!attributesToExclude.contains(aname)) {
        parameters.put(aname, attributes.getValue(i));
      }
    }
    return parameters;
  }

  public List<GroupPublish> getPublishes() {
    return this.publishes;
  }

}
