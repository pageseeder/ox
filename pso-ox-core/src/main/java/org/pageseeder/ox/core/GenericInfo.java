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

import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class GenericInfo.
 *
 * @author Carlos Cabral
 * @since 06 Jul. 2018
 */
public class GenericInfo implements XMLWritable, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -8725001735606291241L;

  /**  XML Element Name. */
  private final String _name;

  /** Elements Attributes. */
  private final Map<String, String> _attributes = new HashMap<>();

  /** The text. */
  private final StringBuilder text = new StringBuilder();

  /**
   * Instantiates a new generic info.
   *
   * @param name the name
   */
  public GenericInfo(String name) {
    super();
    this._name = name;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this._name;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  public Map<String, String> getAttributes() {
    return this._attributes;
  }

  /**
   * Adds the attributes.
   *
   * @param name  the name
   * @param value the value
   */
  public void addAttributes (String name, String value) {
    if (StringUtils.isBlank(name)) throw new IllegalArgumentException("The attribute cannot have empty name.");
    if (this._attributes.containsKey(name)) {
      //Check the uniqueness of the step
      throw new IllegalArgumentException("The attribute " + name + " already exist in this element " + this.getName());
    }
    this._attributes.put(name, value==null ? "" : value);
  }

  /**
   * Adds the text.
   *
   * @param text the text
   */
  public void addText (String text) {
    if (text != null) this.text.append(text);
  }

  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement(this.getName());
    for (Entry<String, String> attribute : this._attributes.entrySet()) {
      xml.attribute(attribute.getKey(), attribute.getValue());
    }
    xml.writeText(this.text.toString());
    xml.closeElement();
  }
}
