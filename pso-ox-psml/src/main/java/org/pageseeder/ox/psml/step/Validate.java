/* Copyright (c) 2016 Allette Systems pty. ltd. */
package org.pageseeder.ox.psml.step;

import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.psml.validation.CharactersValidator;
import org.pageseeder.ox.psml.validation.PSMLValidator;
import org.pageseeder.ox.psml.validation.ValidationResult;
import org.pageseeder.ox.psml.validation.ValidationStepResult;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *   <li><var>input</var>The file to validate.</li>
 *   <li><var>extension</var>The extension of the files to validate, only used if input is a folder.</li>
 *   <li><var>type</var>The type of validation, one of "characters", "well-formed", "schema", "schematron".</li>
 *   <li><var>output</var>The validation report as CSV or HTML.</li>
 * </ul>
 *
 *
 * @author Jean-Baptiste Reure
 * @version 8 March 2018
 */
public class Validate implements Step, Measurable {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(Validate.class);

  private int progress = 0;

  private enum VALIDATION_TYPE {
    CHARACTERS, WELL_FORMED, SCHEMA, SCHEMATRON;
    static VALIDATION_TYPE fromString(String s) {
      if ("characters" .equals(s)) return CHARACTERS;
      if ("well-formed".equals(s)) return WELL_FORMED;
      if ("schema"     .equals(s)) return SCHEMA;
      if ("schematron" .equals(s)) return SCHEMATRON;
      return null;
    }
  };

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    String input     = info.getParameter("input", data.getOriginal().getName());
    String type      = info.getParameter("type");
    String extension = info.getParameter("extension", "psml");
    String output    = info.getParameter("output");

    if (type == null || type.trim().isEmpty()) {
      LOGGER.warn("The validation type is missing.");
      return new InvalidResult(model, data).error(new IllegalArgumentException("The validation type is not specified."));
    }

    VALIDATION_TYPE vtype = VALIDATION_TYPE.fromString(type);
    if (vtype == null) {
      LOGGER.warn("The validation type is invalid.");
      return new InvalidResult(model, data).error(new IllegalArgumentException("The validation type is not invalid: only characters, well-formed, schema and schematron are supported."));
    }

    if (input == null || input.trim().isEmpty()) {
      LOGGER.warn("The input file is missing.");
      return new InvalidResult(model, data).error(new IllegalArgumentException("The input file is not specified."));
    }

    File file = data.getFile(input);
    if (file == null || !file.exists()) {
      LOGGER.warn("The input file does not exist {}.", input);
      return new InvalidResult(model, data).error(new IllegalArgumentException("The input file does not exist."));
    }

    // create result
    ValidationStepResult results = new ValidationStepResult(model, data, output);

    // validate all docs
    int done = 0;
    List<File> files = FileUtils.findFiles(file, extension);
    for (File f : files) {
      ValidationResult result = validate(f, vtype);
      results.addResults(f.equals(file) ? f.getName() : FileUtils.relativePath(f, file), result);
      if (vtype == VALIDATION_TYPE.CHARACTERS && result.hasErrors()) results.setStatus(ResultStatus.WARNING);
      this.progress = (++done) * 100 / files.size();
    }

    // create reports and return
    results.finished();
    return results;
  }

  @Override
  public int percentage() {
    return this.progress;
  }

  private ValidationResult validate(File file, VALIDATION_TYPE vtype) {
    switch (vtype) {
      case WELL_FORMED:
        return new PSMLValidator().validateWellFormed(file);
      case SCHEMATRON:
        return new PSMLValidator().validateWithSchematron(file);
      case SCHEMA:
        return new PSMLValidator().validateWithSchema(file);
      case CHARACTERS:
        return new CharactersValidator().validateCharacters(file);
    }
    return null;
  }

}
