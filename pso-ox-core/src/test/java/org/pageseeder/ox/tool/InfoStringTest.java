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
public class InfoStringTest {

  @Test
  public void testAll_Valid() {
    String value = "test";

    Map<String, String> extraAttr = new HashMap<>();
    extraAttr.put("source", "Attribute Source.");

    InfoString infoString = new InfoString("Info String", extraAttr, value);
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
  public void testNullAttributes_Valid() {
    String value = "test";

    InfoString infoString = new InfoString("Info String", null, value);
    assertEquals(InfoType.string, infoString.getType());
    assertEquals("Info String", infoString.getName());
    assertEquals("test", infoString.getValue());

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
  public void testNameNull_Exception() {
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
