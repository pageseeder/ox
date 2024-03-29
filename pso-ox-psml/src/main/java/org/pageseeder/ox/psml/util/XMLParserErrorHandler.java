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
package org.pageseeder.ox.psml.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects XML parsing errors and warnings.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 5.3902
 * @since 5.2302
 */
public final class XMLParserErrorHandler implements ErrorHandler {

  /**
   * Errors collected by error and fatal events.
   */
  private final List<String> errors = new ArrayList<>();

  /**
   * Warnings collected.
   */
  private final List<String> warnings = new ArrayList<>();

  @Override
  public void error(SAXParseException exception) throws SAXException {
    this.errors.add("ERROR: "+toMessage(exception));
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    this.errors.add("FATAL: "+toMessage(exception));
  }

  /**
   * Warnings are ignored.
   *
   * {@inheritDoc}
   */
  @Override
  public void warning(SAXParseException exception) throws SAXException {
    this.warnings.add("WARNING: "+toMessage(exception));
  }

  /**
   * Returns the warnings collected by this error handler.
   *
   * @return Returns the warnings collected by this error handler.
   */
  public List<String> getWarnings() {
    return this.warnings;
  }

  /**
   * @return <code>true</code> if there were errors collected.
   */
  public boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  /**
   * @return <code>true</code> if there were warnings collected.
   */
  public boolean hasWarnings() {
    return !this.warnings.isEmpty();
  }

  /**
   * Returns the errors collected by this error handler.
   *
   * @return Returns the errors collected by this error handler.
   */
  public List<String> getErrors() {
    return this.errors;
  }

  private String toMessage(SAXParseException exception) {
    return exception.getMessage()+" ["+exception.getLineNumber()+":"+exception.getColumnNumber()+"]";
  }
}
