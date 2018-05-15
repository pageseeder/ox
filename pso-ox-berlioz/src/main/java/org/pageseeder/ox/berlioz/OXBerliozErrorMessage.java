/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.berlioz;

import org.pageseeder.ox.ErrorMessage;

/**
 * The Enum OXBerliozErrorMessage.
 *
 * @author Carlos Cabral
 * @since 15 May 2018
 */
public enum OXBerliozErrorMessage implements ErrorMessage {
  
  /** The request is not multipart. */
  REQUEST_IS_NOT_MULTIPART ("OX-BZ-0001", "The specified request is not a multipart request. It should be multipart/form-data or multipart/mixed stream.");
  
  /** The message. */
  private final String message;
  
  /** The code. */
  private final String code;
  
  /**
   * Instantiates a new OX berlioz error message.
   *
   * @param message the message
   * @param code the code
   */
  private OXBerliozErrorMessage(String message, String code) {
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
