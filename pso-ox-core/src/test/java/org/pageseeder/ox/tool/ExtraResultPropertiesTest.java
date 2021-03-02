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
public class ExtraResultPropertiesTest {

  @Test
  public void testAll_Valid() {
    Map<String, String> properties = new HashMap<>();
    properties.put("property-name", "property-value");

    ExtraResultProperties extraResultProperties = new ExtraResultProperties(properties);

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultProperties.toXML(writer);
      assertEquals("<properties><property name=\"property-name\" value=\"property-value\"/></properties>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testAllNull_valid() {
    ExtraResultProperties extraResultProperties = new ExtraResultProperties(null);
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultProperties.toXML(writer);
      assertEquals("<properties/>",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }


}
