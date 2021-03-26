/*
 * Copyright (c) 1999-2016 Allette Systems Pty Ltd
 */
package org.pageseeder.ox.psml.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects validation errors.
 *
 * @author Philip Rutherford
 *
 * @version 3.1000
 * @since 3.1000
 */
public final class ValidationErrorHandler implements ErrorHandler {

  /**
   * Validation errors collected by error and fatal events.
   */
  private final List<String> errors = new ArrayList<>();

  /**
   * Constructor.
   */
  public ValidationErrorHandler() {
  }

  @Override
  public void error(SAXParseException exception) {
    String loc = "";
    if (exception.getLineNumber() > 0) {
      if (exception.getColumnNumber() > 0) {
        loc = " (line: "+exception.getLineNumber()+", column: "+exception.getColumnNumber()+")";
      } else {
        loc = " (line: "+exception.getLineNumber()+")";
      }
    } else if (exception.getColumnNumber() > 0) {
      loc = " (column: "+exception.getColumnNumber()+")";
    }
    this.errors.add(exception.getMessage() + loc);
  }

  @Override
  public void fatalError(SAXParseException exception) {
    String loc = "";
    if (exception.getLineNumber() > 0) {
      if (exception.getColumnNumber() > 0) {
        loc = "(line: "+exception.getLineNumber()+", column: "+exception.getColumnNumber()+")";
      } else {
        loc = "(line: "+exception.getLineNumber()+")";
      }
    } else if (exception.getColumnNumber() > 0) {
      loc = "(column: "+exception.getColumnNumber()+")";
    }
    this.errors.add(exception.getMessage() + loc);
  }

  /**
   * Warnings are ignored.
   *
   * {@inheritDoc}
   */
  @Override
  public void warning(SAXParseException exception)  {
    // ignore warnings
  }

  /**
   * Returns the errors collected by this error handler.
   *
   * @return Returns the errors collected by this error handler.
   */
  public String getErrors() {
    StringBuilder err = new StringBuilder();
    for (String e : this.errors) {
      err.append(e).append('\n');
    }
    return err.toString();
  }

  /**
   * @return the errors as a list
   */
  public List<String> getErrorList() {
    return this.errors;
  }
}
