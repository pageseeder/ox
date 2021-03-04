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
