package org.pageseeder.ox.step;

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
   * @oaram requestParameters  These are the parameters sent in the HTTP request. They are also used to create the
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
   * @return
   */
  public Result process (Step step, String input, String output, String stepName, Map<String, String> parameters) {
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
    return step.process(this.model, this.getData(), info);
  }

  /**
   *
   * @return
   */
  public PackageData getData() {
    return this.data;
  }
}
