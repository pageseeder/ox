/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
   *
   * @param infoName the info name
   * @param headers  the headers
   * @param infos    the infos
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

  /**
   * Gets info name.
   *
   * @return the info name
   */
  public String getInfoName() {
    return _infoName;
  }

  /**
   * Gets headers.
   *
   * @return the headers
   */
  public List<Header> getHeaders() {
    return _headers;
  }

  /**
   * Gets infos.
   *
   * @return the infos
   */
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
