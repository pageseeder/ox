package org.pageseeder.ox.tool;

import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represent some extra information for the result.
 *
 * @author vku
 * @since 02 March 2021
 */
public class ExtraResultInfos implements XMLWritable {

  /**
   * It is an extra xml that will be written to the result xml.
   * It must be valid.
   */
  private String extraXML;

  private String infoName;
  //  private List<Header> headers;
  private Map<String, String> headers;
  private List<Info> infos;


  /**
   * Instantiates a new Extra Result Infos
   */
  public ExtraResultInfos(String infoName, Map<String, String> headers, List<Info> infos) {
    this.infoName = infoName;
    this.headers = headers;
    this.infos = infos;
  }


  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("infos");
    if (!StringUtils.isBlank(this.infoName)) {
      xml.attribute("name", this.infoName);
    }
    xml.openElement("headers");

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      xml.openElement("header");
      xml.attribute("text", entry.getKey());
      xml.attribute("value", entry.getValue());
      xml.closeElement(); //header
    }
//    for (Header header : headers) {
//      header.toXML(xml);
//    }
    xml.closeElement(); //headers

    for (Info info : infos) {
      info.toXML(xml);
    }
    xml.closeElement(); //infos
  }
}
