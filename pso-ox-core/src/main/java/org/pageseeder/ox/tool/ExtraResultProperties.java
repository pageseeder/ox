package org.pageseeder.ox.tool;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Represent some extra properties for the result.
 *
 * @author vku
 * @since 2 March 2021
 */
public class ExtraResultProperties implements XMLWritable {

  /**
   * It is a map of properties used for steps
   * It must be valid.
   */
  private Map<String, String> properties;

   /**
   * Instantiates a new Extra result properties.
   *
   * @param properties the map of properties
   */
  public ExtraResultProperties(Map<String, String> properties) {
    this.properties = properties;
  }


  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("properties");
    if (properties != null) {
      for (Map.Entry<String, String> entry : properties.entrySet() ) {
        xml.openElement("property");
        xml.attribute("name", entry.getKey());
        xml.attribute("value", entry.getValue());
        xml.closeElement();
      }
    }
    xml.closeElement();//parameter
  }
}
