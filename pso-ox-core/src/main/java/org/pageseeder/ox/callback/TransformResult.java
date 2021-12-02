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

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.XSLT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 * Transform the result xml in something else.
 *
 * <ul>
 *   <li><b>callback-xslt</b> it will be used to change the result XML.</li>
 *   <li><b>callback-output</b> The place where the transformed XML will be placed.</li>
 * </ul>
 * @author ccabral
 * @since 2 December 2021
 */
public class TransformResult extends Transform {
  private final static Logger LOGGER = LoggerFactory.getLogger(TransformResult.class);

  @Override
  public void process(Model model, PackageData data, Result result, StepInfo info) {

    try {
      File xslt = getXSLFile(model, data, info);
      File output = getOutputFile(data, info);

      if (xslt != null && output != null) {
        Transformer transformer = XSLT.buildXSLTTransformer(xslt, data, info);
        XSLT.transform(result, output, transformer);
        if (result instanceof DefaultResult) {
          String outputXML = FileUtils.read(output);
          ((DefaultResult) result).addExtraXML(new ExtraResultStringXML(outputXML));
        }
      }
    } catch (TransformerException | IOException e) {
      LOGGER.error("Callback failed {}", e.getMessage());
    }
  }


}
