package org.pageseeder.ox.tool;

import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represent some extra information for the result.
 *
 * @author vku
 * @since 02 March 2021
 */
public class ExtraResultInfos implements XMLWritable {

  private final String _infoName;
  private final List<Header> _headers;
  private final List<Info> _infos;


  /**
   * Instantiates a new Extra Result Infos
   */
  public ExtraResultInfos(String infoName, List<Header> headers, List<Info> infos) {
    Objects.requireNonNull(infoName);
    this._infoName = infoName;
    if (headers == null) {
      this._headers = new ArrayList<>();
    } else {
      this._headers = headers;
    }
    if (infos == null ) {
      this._infos = new ArrayList<>();
    } else {
      this._infos = infos;
    }
  }

  public String getInfoName() {
    return _infoName;
  }

  public List<Header> getHeaders() {
    return _headers;
  }

  public List<Info> getInfos() {
    return _infos;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("infos");
    if (!StringUtils.isBlank(this._infoName)) {
      xml.attribute("name", this._infoName);
    }

    xml.openElement("headers");
    for (Header header : this._headers) {
      header.toXML(xml);
    }
    xml.closeElement(); //headers

    for (Info info : _infos) {
      info.toXML(xml);
    }
    xml.closeElement(); //infos
  }
}
