/*
 * Copyright 2024 Allette Systems (Australia)
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
package org.pageseeder.ox.psml.step;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.psml.split.PSMLSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>input</var> the psml file that will be splitted. It accepts dynamic value. It is required</li>
 *  <li><var>output</var> the output folder. It accepts dynamic value. It is required</li>
 *  <li><var>split-config</var> the split-config.xml location. It can placed in model directory or package directory.
 *  It is required</li>
 *  <li><var>media-folder</var> the media folder (default is images). Optional</li>
 * </ul>
 *
 * @author ccabral
 * @since 19 June 2024
 */
public class Splitter implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(Splitter.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    File input = getInput(data, info);
    File output = getOutput(data, info, input);
    DefaultResult result = new DefaultResult(model, data, info, output);

    try {
      PSMLSplitter.Builder builder = getBuilder(model, data, info, input, output);
      PSMLSplitter splitter = builder.build();
      splitter.process();
    } catch (OXException | IOException e) {
      LOGGER.debug("PSML splitter: {}", e.getMessage());
      result.setError(e);
    }

    return result;
  }

  private PSMLSplitter.Builder getBuilder (Model model, PackageData data, StepInfo info, File input, File output) throws OXException {
    PSMLSplitter.Builder builder = new PSMLSplitter.Builder();

    File splitConfig = getSplitConfig(model, data, info);

    if (input == null && !input.exists()) {
      throw new OXException("input must be specified");
    }

    if (output == null) {
      throw new OXException("output must be specified");
    } else if (!output.exists()) {
      output.mkdirs();
    }

    if (splitConfig == null && !splitConfig.exists()) {
      throw new OXException("split-config must be specified");
    }

    // build splitter
    //It is ok to set the mediaFolder
    String mediaFolder = StepUtils.getParameter(data, info, "media-folder", null);
    builder.source(input);
    builder.destination(output);
    builder.config(splitConfig);
    builder.media(mediaFolder);

    //maybe they are not necessary for ox
    //builder.working(new File(work));
    //builder.params()

    builder.log(LoggerFactory.getLogger(PSMLSplitter.class));

    return builder;
  }

  /**
   * Gets the input file.
   *
   * @param data the data
   * @param info the info
   * @return the input file
   */
  private File getInput(PackageData data, StepInfo info) {
    File input = StepUtils.getInput(data, info);
    return input;
  }

  /**
   * Gets the output file.
   *
   * @param data the data
   * @param info the info
   * @param input the input
   * @return the output file
   */
  private File getOutput(PackageData data, StepInfo info, File input) {
    File output = StepUtils.getOutput(data, info, input);
    return output;
  }

  /**
   * Gets the split config file.
   *
   * @param model the model
   * @param data the data
   * @param info the info
   * @return the split config file
   */
  private File getSplitConfig(Model model, PackageData data, StepInfo info) {
    File splitConfig = null;
    String splitConfigParemeter = StepUtils.getParameter(data, info, "split-config", info.output());
    if (!StringUtils.isBlank(splitConfigParemeter)) {
      splitConfig = model.getFile(splitConfigParemeter);
      if (!splitConfig.exists()) {
        splitConfig = data.getFile(splitConfigParemeter);
      }
    }
    return splitConfig;
  }
}
