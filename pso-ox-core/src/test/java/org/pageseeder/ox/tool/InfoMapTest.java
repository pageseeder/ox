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
  public void test() {
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
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      infoMap.toXML(writer);
      System.out.println(writer.toString());
      Assert.assertEquals("<info name=\"repeated_page_labels\" value=\"I:1,2,3;II:4,5,6\" type=\"map\"/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }
}
