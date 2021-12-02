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
package org.pageseeder.ox.callback;

import org.pageseeder.ox.api.CallbackStep;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author ccabral
 * @since 02 December 2021
 */
public abstract class Transform  implements CallbackStep {


  /**
   * Gets the output file.
   *
   * @param data the data
   * @param info the info
   * @return the output file
   */
  protected File getOutputFile(PackageData data, StepInfo info) throws IOException {
    File output = null;
    String outputParemeter = StepUtils.getParameter(data, info, "callback-output", "");

    if (StringUtils.isBlank(outputParemeter) || outputParemeter.equals(info.input())) {
      outputParemeter = data.id() + System.nanoTime();
    }

    output = data.getFile(outputParemeter);
    if (!output.exists()) {
      output.getParentFile().mkdirs();
      output.createNewFile();
    }

    return output;
  }

  /**
   * Gets the XSL file.
   *
   * @param model the model
   * @param data  the data
   * @param info  the info
   * @return the XSL file
   */
  protected File getXSLFile(Model model, PackageData data, StepInfo info) {
    String xslParameter = info.getParameter("callback-xsl", data.getParameter("callback-xsl"));
    File xsl = null;
    if (!StringUtils.isBlank(xslParameter)) {
      xsl = model.getFile(xslParameter);
      if (xsl == null || !xsl.exists()) {
        xsl = data.getFile(xslParameter);
      }
    }
    return xsl;
  }
}
