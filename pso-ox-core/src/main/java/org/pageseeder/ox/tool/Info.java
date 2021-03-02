package org.pageseeder.ox.tool;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Extra information for a step
 *
 * @author vku
 * @since 02 March 2021
 */
public abstract class Info implements XMLWritable {



  private String name;
  private InfoType type;
  private Map<String, String> extraAttributes;

  /**
   * Instantiates a new Info parameter
   *
   * @param name
   * @param type has three possible values string, list and map. This will indicate how the attribute value should be
   *             displayed in the front end. The default option is string.
   * @param extraAttributes extra attributes that can be shown if specified in header element
   */
  public Info(String name, InfoType type, Map<String, String> extraAttributes) {
    this.name = name;
    this.type = type;
    this.extraAttributes = extraAttributes;
  }

  public abstract String getValue();

  /**
   * Instantiates a new Info parameter
   *
   * @param name
   * @param type has three possible values string, list and map. This will indicate how the attribute value should be
   *         displayed in the front end. The default option is string.
   */
  public Info(String name, InfoType type) {
    this(name, type, null);
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("info");
    xml.attribute("name", this.name);
    xml.attribute("value", this.getValue());
    xml.attribute("type", this.type.name());

    if (this.extraAttributes != null) {
      for (Map.Entry<String, String> attribute : this.extraAttributes.entrySet()) {
        xml.attribute(attribute.getKey(), attribute.getValue());
      }
    }
    xml.closeElement(); //info
  }
}
