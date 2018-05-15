/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox;

/**
 * The Enum OXErrorMessage.
 *
 * @author Carlos Cabral
 * @since 15 May 2018
 */
public enum OXErrorMessage implements ErrorMessage {
  
  /** The unknown. */
  UNKNOWN ("OX-CORE-0000", "Unknown error."),
  
  /** The file not selected. */
  FILE_NOT_SELECTED ("OX-CORE-0001", "A file has not been selected."),
  
  /** The file is empty. */
  FILE_IS_EMPTY ("OX-CORE-0002", "This file is invalid because is empty.");
  
  /** The message. */
  private final String message;
  
  /** The code. */
  private final String code;
  
  /**
   * Instantiates a new OX error message.
   *
   * @param message the message
   * @param code the code
   */
  private OXErrorMessage(String code, String message) {
    this.message = message;
    this.code = code;
  }
  
  /* (non-Javadoc)
   * @see org.pageseeder.ox.ErrorMessage#getMessage()
   */
  public String getMessage() {
    return message;
  }
  
  /* (non-Javadoc)
   * @see org.pageseeder.ox.ErrorMessage#getCode()
   */
  public String getCode() {
    return code;
  }
}
