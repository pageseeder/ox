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
package org.pageseeder.ox.schematron.step;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.schematron.tool.SchematronCommand;
import org.pageseeder.ox.tool.InvalidResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A step for validate the specified input(define by <var>input</var> parameter ) by schematron file.
 * </p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var> the xml file needs to validate, which is a relative path in datapackage.</li>
 * <li><var>schema-folder</var> the schema root folder, which is a relative path to model folder. (default: schema)</li>
 * <li><var>schema</var> the schema file to validate, which is a relative path of model folder.</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>
 * Generally, it executes the {@link SchematronCommand} and return the Result based on the specified inputs.
 * </p>
 *
 * @author Ciber Cai
 * @version 20 June 2014
 */
public final class SchematronValidation implements Step {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SchematronValidation.class);

  /**
   * Process.
   *
   * @param model the model
   * @param data the data
   * @param info the info
   * @return the result
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    SchematronCommand cmd = new SchematronCommand(model);
    String schema = info.getParameter("schema");
    String docPath = info.getParameter("input");

    // throw the error
    if (docPath == null) return new InvalidResult(model, data).error(new IllegalArgumentException("Parameter input must be specified."));
    if (schema == null) return new InvalidResult(model, data).error(new IllegalArgumentException("Parameter schema must be specified."));

    if (schema != null) {
      LOGGER.debug("schema {}", schema);
      cmd.setSchema(schema);
    }

    LOGGER.debug("docPath {}", docPath);
    cmd.setDocumentPath(docPath);

    return cmd.process(data);

  }
}
