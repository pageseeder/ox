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

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author vku
 * @since 02 March 2021
 */
public class HeaderTest {

  @Test
  public void testAll_Valid() {
    String text = "Header Text";
    String value = "header-value";

    Header header = new Header(text, value);
    assertEquals("Header Text", header.getText());
    assertEquals("header-value", header.getValue());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      header.toXML(writer);
      assertEquals("<header text=\"Header Text\" value=\"header-value\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test (expected = NullPointerException.class)
  public void testNullText_Exception() {
    String value = "header-value";

    Header header = new Header(null, value);
  }

  @Test (expected = NullPointerException.class)
  public void testNullValue_Exception() {
    String text = "Header Text";

    Header header = new Header(text, null);
  }

  @Test (expected = NullPointerException.class)
  public void testAllNull_Exception(){
    Header header = new Header(null, null);
  }
}
