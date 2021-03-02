package org.pageseeder.ox.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import sun.reflect.generics.tree.Tree;

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
