/*
 * Copyright 2026 Allette Systems (Australia)
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

import org.pageseeder.ox.pageseeder.model.PublishRequest;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SAX handler for parsing XML files containing publish request configurations
 * into {@link PublishRequest} instances.
 *
 * @author Carlos Cabral
 * @since 1 October
 */
public class PublishRequestInputHandler extends DefaultHandler {

  private final List<PublishRequest> publishes;

  public PublishRequestInputHandler() {
    this.publishes = new ArrayList<>();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    String elementName = (localName == null || localName.isEmpty()) ? qName : localName;

    if ("publish".equalsIgnoreCase(elementName)) {
      PublishRequest request = new PublishRequest.Builder()
          .project(attributes.getValue("project"))
          .group(attributes.getValue("group"))
          .member(attributes.getValue("member"))
          .target(attributes.getValue("target"))
          .type(attributes.getValue("type"))
          .logLevel(attributes.getValue("log-level"))
          .uriId(attributes.getValue("uriid"))
          .documentType(attributes.getValue("documenttype"))
          .parameters(mapParametersFromAttributes(attributes))
          .build();

      this.publishes.add(request);
    }
  }

  /**
   * Extracts custom script parameters from attributes while excluding standard
   * request fields.
   *
   * @param attributes the SAX attributes
   * @return a map of key-value parameters
   */
  private static Map<String, String> mapParametersFromAttributes(Attributes attributes) {
    Map<String, String> parameters = new HashMap<>();
    List<String> attributesToExclude = Arrays.asList(
        "project",
        "group",
        "target",
        "type",
        "log-level",
        "member",
        "uriid",
        "documenttype"
    );

    for (int i = 0; i < attributes.getLength(); i++) {
      String aname = attributes.getLocalName(i) == null || attributes.getLocalName(i).isEmpty()
          ? attributes.getQName(i)
          : attributes.getLocalName(i);

      if (!attributesToExclude.contains(aname.toLowerCase())) {
        parameters.put(aname, attributes.getValue(i));
      }
    }
    return parameters;
  }

  /**
   * Gets the list of parsed publish requests.
   *
   * @return the list of {@link PublishRequest} instances
   */
  public List<PublishRequest> getPublishes() {
    return this.publishes;
  }
}