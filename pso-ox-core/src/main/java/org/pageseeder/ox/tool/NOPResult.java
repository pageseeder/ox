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

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 * A implementation of {@link Result} which is to indicate no operation of the result.
 *
 * <h3>Return XML</h3>
 * <pre>{@code
 *   <result type="no-operation" downloadable="false"/>
 * }*</pre>
 *
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class NOPResult extends ResultBase implements Result {

  /**
   * Instantiates a new Nop result.
   *
   * @param model the model
   * @param data  the data
   */
  public NOPResult(Model model, PackageData data) {
    super(model, data);
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result");
    xml.attribute("type", "no-operation");
    xml.attribute("downloadable", String.valueOf(isDownloadable()));
    xml.closeElement();
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }
}
