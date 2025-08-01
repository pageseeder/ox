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
package org.pageseeder.ox.tool;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Represent some extra properties for the result.
 *
 * @author vku
 * @since 2 March 2021
 */
public class ExtraResultProperties implements XMLWritable {

  /**
   * It is a map of properties used for steps
   * It must be valid.
   */
  private Map<String, String> properties;

  /**
   * Instantiates a new Extra result properties.
   *
   * @param properties the map of properties
   */
  public ExtraResultProperties(Map<String, String> properties) {
    this.properties = properties;
  }


  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("properties");
    if (properties != null) {
      for (Map.Entry<String, String> entry : properties.entrySet() ) {
        xml.openElement("property");
        xml.attribute("name", entry.getKey());
        xml.attribute("value", entry.getValue());
        xml.closeElement();
      }
    }
    xml.closeElement();//parameter
  }
}
