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

import org.pageseeder.ox.api.CallbackStep;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;
import org.pageseeder.ox.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ccabral
 * @since 08 January 2021
 */
public class StepSimulator {
  private final Model model;
  private final PackageData data;

  /**
   *
   * The
   *
   * @param modelName          The model name will be used just to create a model. It will not use to read a
   *                           configuration.
   * @param uploadedFile       This does not necessary need to be the uploaded file, it can be the file/folder that will
   *                           be used as input for the step. This file is used to create the PackageData.
   * @param requestParameters  These are the parameters sent in the HTTP request. They are also used to create the
   *                           PackageData.
   */
  public StepSimulator(String modelName, File uploadedFile, Map<String, String> requestParameters) {
    if (StringUtils.isBlank(modelName)) {
      throw new IllegalArgumentException("The model name cannot be blank (null or empty)");
    }

    if (uploadedFile == null) {
      throw new IllegalArgumentException("The uploaded file cannot be null.");
    }

    this.model = new Model(modelName);
    this.data = PackageData.newPackageData(modelName, uploadedFile);

    if (requestParameters != null) {
      for (Map.Entry<String, String> parameter: requestParameters.entrySet()) {
        this.data.setParameter(parameter.getKey(), parameter.getValue());
      }
    }
  }

  /**
   *
   * @param step         is an instance of the step we are going to process.
   * @param input        The input file/folder for this step. It can be null if it is the uploadedFile used in this class
   *                     constructor.
   * @param output       The output file/folder for this step. It can be null.
   * @param stepName     The step name cannot be null or empty.
   * @param parameters   The parameters that exclusively belongs to this step.
   * @return result
   */
  public Result process (Step step, String input, String output, String stepName, Map<String, String> parameters) {
    // process the step
    return process(step, input, output, stepName, parameters, null);
  }

  /**
   *
   * @param step         is an instance of the step we are going to process.
   * @param input        The input file/folder for this step. It can be null if it is the uploadedFile used in this class
   *                     constructor.
   * @param output       The output file/folder for this step. It can be null.
   * @param stepName     The step name cannot be null or empty.
   * @param parameters   The parameters that exclusively belongs to this step.
   * @param callbackStep The callback step
   * @return result
   */
  public Result process (Step step, String input, String output, String stepName, Map<String, String> parameters,
                         CallbackStep callbackStep) {
    if (StringUtils.isBlank(stepName)) {
      throw new IllegalArgumentException("The step name cannot be blank (null or empty)");
    }

    if (input == null) {
      input = this.getData().getPath(this.getData().getOriginal());
    }

    // use input as output if output is null
    if (output == null) {
      output = input;
    }

    if (parameters == null) {
      parameters = new HashMap<>();
    }

    // step info
    StepInfoImpl info = new StepInfoImpl(this.getData().id(), stepName, input, output, parameters);

    // process the step
    Result result = step.process(this.model, this.getData(), info);

    // process the callback step
    if (callbackStep != null) {
      // put the step result to callback step
      callbackStep.process(this.model, this.getData(), result, info);
    }

    return result;
  }

  /**
   *
   * @return PackageData
   */
  public PackageData getData() {
    return this.data;
  }
}
