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
package org.pageseeder.ox.step;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 * A step does nothing.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  13 June 2014
 */
public final class NOPStep implements Step {

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    return new NOPResult(model, data);
  }

  /**
   * The results of a NOOP command.
   *
   * @author Christophe Lauret
   * @since  8 May 2014
   */
  public final class NOPResult extends ResultBase implements Result {

    /**
     * @param model the {@link Model}
     * @param data the {@link PackageData}
     */
    public NOPResult(Model model, PackageData data) {
      super(model, data);
    }

    @Override
    public Exception error() {
      return null;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("type", "no-operation");
      xml.attribute("name", "No Operation");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));
      xml.closeElement();
    }

    @Override
    public boolean isDownloadable() {
      return false;
    }

  };
}
