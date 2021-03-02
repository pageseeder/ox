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
public class ExtraResultStringXMLTest {

  @Test
  public void testAll_Valid() {
    String extraXML = "extra xml";

    ExtraResultStringXML extraResultStringXML = new ExtraResultStringXML(extraXML);

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultStringXML.toXML(writer);
      assertEquals("extra xml",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }


  @Test
  public void testAllNull_valid() {
    ExtraResultStringXML extraResultStringXML = new ExtraResultStringXML(null);
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      extraResultStringXML.toXML(writer);
      assertEquals("",
          writer.toString());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  
}
