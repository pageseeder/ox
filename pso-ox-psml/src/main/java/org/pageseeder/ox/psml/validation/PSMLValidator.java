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
package org.pageseeder.ox.psml.validation;

import org.pageseeder.ox.psml.util.Validators;
import org.pageseeder.schematron.SchematronException;
import org.pageseeder.schematron.SchematronResult;
import org.pageseeder.schematron.Validator;
import org.pageseeder.schematron.ValidatorFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jean-Baptiste Reure
 * @version 8 March 2018
 */
public class PSMLValidator {

  /**
   * The schema to use.
   * <p>
   * <p>If <code>null</code>, then the default will be used (using the format of the XML file)
   */
  private String schema = "ps://org/pageseeder/ox/psml/psml-portable.xsd";

  /**
   * The schematron to use.
   * <p>
   * <p>If <code>null</code>, then the default will be used (using the format of the XML file)
   */
  private String schematron = "/org/pageseeder/ox/psml/psml-portable.sch";

  /**
   * Schematron validator factory object
   */
  private static final ValidatorFactory SCHEMATRON_FACTORY = new ValidatorFactory();

  /**
   * An internal cache for schematron validators
   * (only for this instance: if schematrons change, they are reloaded for each upload)
   */
  private static final Map<String, Validator> validators = new HashMap<>();

  /**
   * Validate with a schema.
   *
   * @param original   the actual file to validate
   * @return the result of the validation, containing errors and if it was actually validated
   */
  public ValidationResult validateWithSchema(File original) {
    return validateWithSchema(this.schema, original);
  }

  /**
   * Validate with a schema.
   *
   * @param schemaPath the schema path
   * @param original   the actual file to validate
   * @return the result of the validation, containing errors and if it was actually validated
   */
  public ValidationResult validateWithSchema(String schemaPath, File original) {
    String schemaName = schemaPath == null ? null : schemaPath.substring(schemaPath.lastIndexOf('/') + 1);
    String error = null;
    try (InputStream reader = new FileInputStream(original);) {

      return new ValidationResult("schema", true, schemaName, Validators.validateXmlFileWithSchemaReturnErrors(reader, schemaPath, null));
    } catch (SAXException ex) {
      error = "Error when validating file: " + ex.getMessage();
    } catch (IOException ex) {
      error = "Error when reading file: " + ex.getMessage();
    }
    return new ValidationResult("schema", false, schemaName, error);
  }

  /**
   * Validate with a schematron
   *
   * @param original   the file to validate
   * @return the result of the validation, containing errors and if it was actually validated
   */
  public ValidationResult validateWithSchematron( File original) {
    return validateWithSchematron(this.schematron, original);
  }

  /**
   * Validate with a schematron
   *
   * @param schematron the name of the schematron to use (file name without extension)
   * @param original   the file to validate
   * @return the result of the validation, containing errors and if it was actually validated
   */
  public ValidationResult validateWithSchematron(String schematron, File original) {
    String error = null;
    // find schematron and build validator
    InputStream schematronStream = PSMLValidator.class.getResourceAsStream(schematron);
    Validator validator = null;
    try {
      // find a schematron validator (re-use old one if it's there)
      validator = loadSchematronValidator(schematron, schematronStream);
    } catch (SchematronException ex) {
      error = "Error when loading schematron schema " + schematron + ": " + ex.getMessage();
    }
    if (validator != null) {
      // validate then if found
      try (InputStream reader = new FileInputStream(original);){
        // validate
        SchematronResult result = validator.validate(new StreamSource(reader));
        return new ValidationResult("schematron", true, schematron, result.getFailedAssertions());
      } catch (SchematronException ex) {
        error = "Error when validating file:, " + ex.getMessage();
      } catch (IOException ex) {
        error = "Error when reading file: " + ex.getMessage();
      }
    }
    return new ValidationResult("schematron", false, schematron, error);
  }

  /**
   * Find a schematron validator: re-use an old one if there is one.
   *
   * @param key the key that defines the validator
   * @param sch the schematron source
   * @return a scheamtron validator ready to be used
   */
  private Validator loadSchematronValidator(String key, InputStream sch) throws SchematronException {
    // use default preprocessor: iso_svrl.xsl (included in ant_schematron.jar)
    Validator validator = validators.get(key);
    if (validator == null) {
      validator = SCHEMATRON_FACTORY.newValidator(new StreamSource(sch));
      validators.put(key, validator);
    }
    return validator;
  }

  /**
   * Validate well formed XML
   *
   * @param original the file to validate
   * @return the result of the validation, containing errors and if it was actually validated
   */
  public ValidationResult validateWellFormed(File original) {
    String error = null;
    try (InputStream reader = new FileInputStream(original)){
      return new ValidationResult("well-formed", true, null, Validators.validateWellFormednessReturnErrors(reader));
    } catch (IOException | SAXException ex) {
      error = "Error when validating file: " + ex.getMessage();
    }
    return new ValidationResult("well-formed", true, null, error);
  }

}
