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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author vku
 * @since 02 March 2021
 */
public class ExtraResultInfosTest {

  @Test
  public void testAll_Valid() {
    String name = "extra info test";
    List<Header> headers = new ArrayList<>();
    headers.add(new Header("headertext", "headervalue"));
    List<Info> infos = new ArrayList<>();
    String value = "test";
    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute Source.");
    infos.add(new InfoString("Info String", extraAttr, value));

    ExtraResultInfos extraResultInfos = new ExtraResultInfos(name, headers, infos);
    assertEquals("extra info test", extraResultInfos.getInfoName());
    assertEquals(headers, extraResultInfos.getHeaders());
    assertEquals(infos, extraResultInfos.getInfos());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultInfos.toXML(writer);
      assertEquals("<infos name=\"extra info test\"><headers><header text=\"headertext\" value=\"headervalue\"/></headers><info name=\"Info String\" value=\"test\" type=\"string\" source=\"Attribute Source.\"/></infos>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testNullHeaders_Valid() {
    String name = "extra info test";

    List<Info> infos = new ArrayList<>();
    String value = "test";
    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute Source.");
    infos.add(new InfoString("Info String", extraAttr, value));

    ExtraResultInfos extraResultInfos = new ExtraResultInfos(name, null, infos);
    assertEquals("extra info test", extraResultInfos.getInfoName());
    assertEquals(new ArrayList<>(), extraResultInfos.getHeaders());
    assertEquals(infos, extraResultInfos.getInfos());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultInfos.toXML(writer);
      assertEquals("<infos name=\"extra info test\"><headers/><info name=\"Info String\" value=\"test\" type=\"string\" source=\"Attribute Source.\"/></infos>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testNullInfos_Valid() {
    String name = "extra info test";

    List<Header> headers = new ArrayList<>();
    headers.add(new Header("headertext", "headervalue"));

    ExtraResultInfos extraResultInfos = new ExtraResultInfos(name, headers, null);
    assertEquals("extra info test", extraResultInfos.getInfoName());
    assertEquals(headers, extraResultInfos.getHeaders());
    assertEquals(new ArrayList<>(), extraResultInfos.getInfos());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultInfos.toXML(writer);
      assertEquals("<infos name=\"extra info test\"><headers><header text=\"headertext\" value=\"headervalue\"/></headers></infos>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test (expected = NullPointerException.class)
  public void testNullInfosName_Exception() {
    List<Header> headers = new ArrayList<>();
    headers.add(new Header("headertext", "headervalue"));
    List<Info> infos = new ArrayList<>();
    String value = "test";
    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute Source.");
    infos.add(new InfoString("Info String", extraAttr, value));


    ExtraResultInfos extraResultInfos = new ExtraResultInfos(null, headers, infos);
  }

  @Test (expected = NullPointerException.class)
  public void testAllNull_Exception(){
    ExtraResultInfos extraResultInfos = new ExtraResultInfos(null, null, null);
  }
}
