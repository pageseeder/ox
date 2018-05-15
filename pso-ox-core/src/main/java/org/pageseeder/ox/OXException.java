/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */
package org.pageseeder.ox;

/**
 * <p>The OX Exception.</p>
 *
 * @author Christophe Lauret
 * @since  08 November 2013
 *
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
   * @param message the message of the Exception
   */
  public OXException(String errorMessageCode, String message) {
    super(message);
    this._errorMessageCode = errorMessageCode;    
  }

  /**
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
   * @param cause the {@link Throwable}
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
