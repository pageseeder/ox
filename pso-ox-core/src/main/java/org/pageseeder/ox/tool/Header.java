package org.pageseeder.ox.tool;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * Header parameters will be used as table headers
 *
 * @author vku
 * @since 02 March 2021
 */
public class Header implements XMLWritable {

  private final String _text;
  private final String _value;

  /**
   * Instantiates a new Header object
   *
   * @param text the value that is displayed in the front end
   * @param value the value used to match the information with the attribute name in the element info
   */
  public Header(String text, String value) {
    Objects.requireNonNull(text);
    Objects.requireNonNull(value);
    this._text = text;
    this._value = value;
  }

  public String getText() {
    return _text;
  }

  public String getValue() {
    return _value;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("header");
    xml.attribute("text", this._text);
    xml.attribute("value", this._value);
    xml.closeElement();
  }
}
