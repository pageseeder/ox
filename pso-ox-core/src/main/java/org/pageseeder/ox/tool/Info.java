package org.pageseeder.ox.tool;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Extra information for a step
 *
 * @author vku
 * @since 02 March 2021
 */
public abstract class Info implements XMLWritable {

  private final String _name;
  private final InfoType _type;
  private final Map<String, String> _extraAttributes;

  /**
   * Instantiates a new Info parameter
   *
   * @param name
   * @param type has three possible values string, list and map. This will indicate how the attribute value should be
   *             displayed in the front end. The default option is string.
   * @param extraAttributes extra attributes that can be shown if specified in header element
   */
  public Info(String name, InfoType type, Map<String, String> extraAttributes) {
    Objects.requireNonNull(name, "Info name cannot be null");
    Objects.requireNonNull(type, "Info type cannot be null");
    this._name = name;
    this._type = type;
    this._extraAttributes = extraAttributes;
  }

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

  public String getName() {
    return _name;
  }

  public InfoType getType() {
    return _type;
  }

  public Map<String, String> getExtraAttributes() {
    return _extraAttributes;
  }

  public abstract String getValue();



  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("info");
    xml.attribute("name", this._name);
    xml.attribute("value", this.getValue());
    xml.attribute("type", this._type.name());

    if (this._extraAttributes != null) {
      for (Map.Entry<String, String> attribute : this._extraAttributes.entrySet()) {
        xml.attribute(attribute.getKey(), attribute.getValue());
      }
    }
    xml.closeElement(); //info
  }
}
