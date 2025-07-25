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

/**
 * Represent some extra information for the result.
 *
 * @author ccabral
 * @since 18 January 2021
 */
public class ExtraResultStringXML implements XMLWritable {

  /**
   * It is an extra xml that will be written to the result xml.
   * It must be valid.
   */
  private String extraXML;

  /**
   * Instantiates a new Extra result info.
   *
   * @param extraXML the extra information
   */
  public ExtraResultStringXML(String extraXML) {
    this.extraXML = extraXML;
  }


  @Override
  public void toXML(XMLWriter xml) throws IOException {
    if (!StringUtils.isBlank(this.extraXML)) {
      xml.writeXML(this.extraXML);
    }
  }
}
