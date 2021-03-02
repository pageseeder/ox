package org.pageseeder.ox.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author vku
 * @since 02 March 2021
 */
public class InfoListTest {

  @Test
  public void testAll_Valid() {
    List<String> infos = new ArrayList<>();

    infos.add("1.1 DEFINITIONS");
    infos.add("1.1.1 Sub title");

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Application has an logic to find this information.");

    InfoList infoList = new InfoList("bookmarks_missing_destination", extraAttr, infos);
    assertEquals(InfoType.list, infoList.getType());
    assertEquals("bookmarks_missing_destination", infoList.getName());
    assertEquals("1.1 DEFINITIONS;1.1.1 Sub title", infoList.getValue());
    assertEquals(extraAttr, infoList.getExtraAttributes());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoList.toXML(writer);
      assertEquals("<info name=\"bookmarks_missing_destination\" value=\"1.1 DEFINITIONS;1.1.1 Sub title\" type=\"list\" source=\"Application has an logic to find this information.\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testNullAttributes_Valid() {
    List<String> infos = new ArrayList<>();

    infos.add("1.1 DEFINITIONS");
    infos.add("1.1.1 Sub title");

    InfoList infoList = new InfoList("Info List", null, infos);
    assertEquals(InfoType.list, infoList.getType());
    assertEquals("Info List", infoList.getName());
    assertEquals("1.1 DEFINITIONS;1.1.1 Sub title", infoList.getValue());

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoList.toXML(writer);
      assertEquals("<info name=\"Info List\" value=\"1.1 DEFINITIONS;1.1.1 Sub title\" type=\"list\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test (expected = NullPointerException.class)
  public void testNullValues_Exception() {
    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("sources", "Application has an logic to find this information.");

    InfoList infoList = new InfoList("Info List", extraAttr, null);
  }

  @Test (expected = NullPointerException.class)
  public void testNameNull_Exception() {
    List<String> infos = new ArrayList<>();

    infos.add("1.1 DEFINITIONS");
    infos.add("1.1.1 Sub title");

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("sources", "Application has an logic to find this information.");
    InfoList infoList = new InfoList(null, extraAttr, infos);
  }


  @Test (expected = NullPointerException.class)
  public void testAllNull_Exception(){
    InfoList infoList = new InfoList(null, null, null);
  }
}
