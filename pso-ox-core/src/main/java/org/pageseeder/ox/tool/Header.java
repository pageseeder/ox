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
import java.util.Objects;

/**
 * Header parameters will be used as table headers
 *
 * @author vku
 * @since 02 March 2021
 */
public class Header implements XMLWritable {

  private final String _text;
  private final String _value;

  /**
   * Instantiates a new Header object
   *
   * @param text  the value that is displayed in the front end
   * @param value the value used to match the information with the attribute name in the element info
   */
  public Header(String text, String value) {
    Objects.requireNonNull(text);
    Objects.requireNonNull(value);
    this._text = text;
    this._value = value;
  }

  /**
   * Gets text.
   *
   * @return the text
   */
  public String getText() {
    return _text;
  }

  /**
   * Gets value.
   *
   * @return the value
   */
  public String getValue() {
    return _value;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("header");
    xml.attribute("text", this._text);
    xml.attribute("value", this._value);
    xml.closeElement();
  }
}
