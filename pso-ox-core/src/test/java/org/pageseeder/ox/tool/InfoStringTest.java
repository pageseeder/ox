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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author vku
 * @since 02 March 2021
 */
public class InfoStringTest {

  @Test
  public void testAll_Valid() {
    String value = "test";

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute Source.");

    Info infoString = new InfoString("Info String", extraAttr, value);
    assertEquals(InfoType.string, infoString.getType());
    assertEquals("Info String", infoString.getName());
    assertEquals("test", infoString.getValue());
    assertEquals(extraAttr, infoString.getExtraAttributes());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoString.toXML(writer);
      assertEquals("<info name=\"Info String\" value=\"test\" type=\"string\" source=\"Attribute Source.\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testWithoutAttributes_Valid() {
    String value = "test";

    InfoString infoString = new InfoString("Info String", value);
    assertEquals(InfoType.string, infoString.getType());
    assertEquals("Info String", infoString.getName());
    assertEquals("test", infoString.getValue());
    assertEquals(null, infoString.getExtraAttributes());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoString.toXML(writer);
      assertEquals("<info name=\"Info String\" value=\"test\" type=\"string\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testNullAttributes_Valid() {
    String value = "test";

    InfoString infoString = new InfoString("Info String", null, value);
    assertEquals(InfoType.string, infoString.getType());
    assertEquals("Info String", infoString.getName());
    assertEquals("test", infoString.getValue());
    assertEquals(null, infoString.getExtraAttributes());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoString.toXML(writer);
      assertEquals("<info name=\"Info String\" value=\"test\" type=\"string\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test (expected = NullPointerException.class)
  public void testNullValues_Exception() {
    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("sources", "Application has an logic to find this information.");

    InfoString infoString = new InfoString("Info String", extraAttr, null);
  }

  @Test (expected = NullPointerException.class)
  public void testNullName_Exception() {
    String value = "test";

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("sources", "Application has an logic to find this information.");
    InfoString infoString = new InfoString(null, extraAttr, value);
  }


  @Test (expected = NullPointerException.class)
  public void testAllNull_Exception(){
    InfoString infoList = new InfoString(null, null, null);
  }
}
