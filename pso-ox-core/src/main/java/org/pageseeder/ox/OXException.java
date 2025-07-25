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
package org.pageseeder.ox;

/**
 * <p>The OX Exception.</p>
 *
 * @author Christophe Lauret
 * @since 08 November 2013
 */
public class OXException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3446542849381631998L;

  /** The error message code. */
  private final String _errorMessageCode;

  /**
   * Instantiates a new OX exception.
   */
  public OXException() {
    this(OXErrorMessage.UNKNOWN);
  }

  /**
   * Instantiates a new OX exception.
   *
   * @param errorMessage the error message
   */
  public OXException(ErrorMessage errorMessage) {
   this(errorMessage.getCode(), errorMessage.getMessage());
 }

  /**
   * Instantiates a new OX exception.
   *
   * @param errorMessageCode the error message code
   * @param message          the message of the Exception
   */
  public OXException(String errorMessageCode, String message) {
    super(message);
    this._errorMessageCode = errorMessageCode;
  }

  /**
   * Instantiates a new Ox exception.
   *
   * @param message the message of the Exception
   */
  public OXException(String message) {
    super(message);
    this._errorMessageCode = OXErrorMessage.UNKNOWN.getCode();
  }

  /**
   * Instantiates a new OX exception.
   *
   * @param cause the {@link Throwable}
   */
  public OXException(Throwable cause) {
    super(cause);
    this._errorMessageCode = OXErrorMessage.UNKNOWN.getCode();
  }

  /**
   * Instantiates a new OX exception.
   *
   * @param message the message of the exception
   * @param cause   the {@link Throwable}
   */
  public OXException(String message, Throwable cause) {
    super(message, cause);
    this._errorMessageCode = OXErrorMessage.UNKNOWN.getCode();
  }

  /**
   * Gets the error message code.
   *
   * @return the error message code
   */
  public String getErrorMessageCode() {
    return this._errorMessageCode;
  }
}
