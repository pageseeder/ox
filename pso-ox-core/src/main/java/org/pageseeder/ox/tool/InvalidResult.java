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
 * A Result to indicate it's invalid.
 *
 * <h3>XML Result</h3>
 * <code>{@code
 *  <result type="invalid">... </result>
 *
 * } </code>
 *
 * @author Ciber Cai
 * @since 18 Jul 2016
 */
public class InvalidResult extends ResultBase implements Result {

  public InvalidResult(Model model, PackageData data) {
    super(model, data);
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result");
    xml.attribute("type", "invalid");
    if (error() != null) {
      xml.writeText(error().getMessage());
    }
    xml.closeElement();
  }

  /**
   * @param ex the Exception
   * @return InvalidResult
   */
  public InvalidResult error(Exception ex) {
    setError(ex);
    return this;
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }

}
