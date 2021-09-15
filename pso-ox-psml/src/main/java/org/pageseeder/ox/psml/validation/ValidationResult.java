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

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class ValidationResult implements XMLWritable {

  /**
   * the validation name.
   */
  private final String _schemaName;

  /**
   * the validation name.
   */
  private final String _validationName;

  /**
   * a flag indicating whether there was any validation.
   */
  private final boolean _validated;

  /**
   * An error message
   */
  private final Collection<String> _errors;

  /**
   * Some extra data
   */
  private final ExtraData _extra;

  /**
   * Creates new validation results.
   *
   * @param name      Validation name
   * @param validated <code>true</code> if the file has been validated
   * @param schema    The schema used to validate (can be null)
   * @param errors    Any error detail if an error occurred.
   */
  public ValidationResult(String name, boolean validated, String schema, Collection<String> errors) {
    this(name, validated, schema, errors, null);
  }

  /**
   * Creates new validation results.
   *
   * @param name      Validation name
   * @param validated <code>true</code> if the file has been validated
   * @param schema    The schema used to validate (can be null)
   * @param errors    Any error detail if an error occurred.
   * @param data      Some extra data
   */
  public ValidationResult(String name, boolean validated, String schema, Collection<String> errors, ExtraData data) {
    this._validationName = name;
    this._validated = validated;
    this._schemaName = schema;
    this._errors = errors;
    this._extra = data;
  }

  /**
   * Creates new validation results.
   *
   * @param name      Validation name
   * @param validated <code>true</code> if the file has been validated
   * @param schema    The schema used to validate (can be null)
   * @param errors    Any error detail if an error occurred.
   */
  public ValidationResult(String name, boolean validated, String schema, String errors) {
    this._validationName = name;
    this._validated = validated;
    this._schemaName = schema;
    this._errors = errors == null || errors.isEmpty() ? null : Collections.singleton(errors);
    this._extra = null;
  }

  /**
   * @return the name of the validation
   */
  public String name() {
    return this._validationName;
  }

  /**
   * @return the name of the schema used to validate (can be null)
   */
  public String schema() {
    return this._schemaName;
  }

  /**
   * @return the validated
   */
  public boolean isValidated() {
    return this._validated;
  }

  /**
   * @return the errors
   */
  public Collection<String> errors() {
    return this._errors;
  }

  /**
   * @return some extra data
   */
  public ExtraData extraData() {
    return this._extra;
  }
  /**
   * Indicates whether some errors were reported.
   *
   * @return <code>true</code> if the errors is a non-empty string;
   * <code>false</code> if errors is <code>null</code> or empty string "".
   */
  public boolean hasErrors() {
    return this._errors != null && this._errors.size() > 0;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("validation-result");
    xml.attribute("name", this._validationName);
    xml.attribute("validated", this._validated ? "true" : "false");
    if (this._schemaName != null) xml.attribute("schema", this._schemaName);
    xml.attribute("status", !hasErrors() ? "valid" : "invalid");
    if (this._extra != null) this._extra.toXML(xml);
    if (hasErrors()) {
      xml.openElement("errors");
      for (String e : this._errors) {
        xml.element("error", e);
      }
      xml.closeElement();
    }
    xml.closeElement();
  }

  public interface ExtraData extends XMLWritable {}
}