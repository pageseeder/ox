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
import java.util.*;

/**
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoMapTest {

  @Test
  public void testAll_Valid() {
    Map<String, List<String>> mapValues = new TreeMap<>();

    List<String> entryValue1 = new ArrayList<>();
    entryValue1.add("1");
    entryValue1.add("2");
    entryValue1.add("3");
    mapValues.put("I", entryValue1);

    List<String> entryValue2 = new ArrayList<>();
    entryValue2.add("4");
    entryValue2.add("5");
    entryValue2.add("6");
    mapValues.put("II", entryValue2);

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute source.");

    InfoMap infoMap = new InfoMap("repeated_page_labels", extraAttr, mapValues);
    Assert.assertEquals(InfoType.map, infoMap.getType());
    Assert.assertEquals("repeated_page_labels", infoMap.getName());
    Assert.assertEquals(extraAttr, infoMap.getExtraAttributes());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoMap.toXML(writer);
      Assert.assertEquals("<info name=\"repeated_page_labels\" value=\"I:1,2,3;II:4,5,6\" type=\"map\" source=\"Attribute source.\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testWithoutAttributes_Valid() {
    Map<String, List<String>> mapValues = new TreeMap<>();

    List<String> entryValue1 = new ArrayList<>();
    entryValue1.add("1");
    entryValue1.add("2");
    entryValue1.add("3");
    mapValues.put("I", entryValue1);

    List<String> entryValue2 = new ArrayList<>();
    entryValue2.add("4");
    entryValue2.add("5");
    entryValue2.add("6");
    mapValues.put("II", entryValue2);


    InfoMap infoMap = new InfoMap("repeated_page_labels", mapValues);
    Assert.assertEquals(InfoType.map, infoMap.getType());
    Assert.assertEquals("repeated_page_labels", infoMap.getName());
    Assert.assertEquals(null, infoMap.getExtraAttributes());
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoMap.toXML(writer);
      Assert.assertEquals("<info name=\"repeated_page_labels\" value=\"I:1,2,3;II:4,5,6\" type=\"map\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testNullAttributes_Valid() {
    Map<String, List<String>> mapValues = new TreeMap<>();

    List<String> entryValue1 = new ArrayList<>();
    entryValue1.add("1");
    entryValue1.add("2");
    entryValue1.add("3");
    mapValues.put("I", entryValue1);

    List<String> entryValue2 = new ArrayList<>();
    entryValue2.add("4");
    entryValue2.add("5");
    entryValue2.add("6");
    mapValues.put("II", entryValue2);


    InfoMap infoMap = new InfoMap("repeated_page_labels", null, mapValues);
    Assert.assertEquals(InfoType.map, infoMap.getType());
    Assert.assertEquals("repeated_page_labels", infoMap.getName());
    Assert.assertEquals(null, infoMap.getExtraAttributes());
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoMap.toXML(writer);
      Assert.assertEquals("<info name=\"repeated_page_labels\" value=\"I:1,2,3;II:4,5,6\" type=\"map\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test (expected = NullPointerException.class)
  public void testNullName_Exception() {
    Map<String, List<String>> mapValues = new TreeMap<>();

    List<String> entryValue1 = new ArrayList<>();
    entryValue1.add("1");
    entryValue1.add("2");
    entryValue1.add("3");
    mapValues.put("I", entryValue1);

    List<String> entryValue2 = new ArrayList<>();
    entryValue2.add("4");
    entryValue2.add("5");
    entryValue2.add("6");
    mapValues.put("II", entryValue2);

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute source.");

    InfoMap infoMap = new InfoMap(null, extraAttr, mapValues);
  }

  @Test (expected = NullPointerException.class)
  public void testNullValues_Exception() {

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute source.");

    InfoMap infoMap = new InfoMap("repeated_page_labels", extraAttr, null);
  }

  @Test (expected = NullPointerException.class)
  public void testAllNull_Exception() {
    InfoMap infoMap = new InfoMap(null, null, null);
  }
}
