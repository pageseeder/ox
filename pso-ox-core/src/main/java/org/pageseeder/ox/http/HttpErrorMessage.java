package org.pageseeder.ox.http;

import org.pageseeder.ox.ErrorMessage;

/**
 * The type Http error message.
 *
 * @author ccabral
 * @since 16 September 2021
 */
public class HttpErrorMessage implements ErrorMessage {
  private final String message;
  private final String code;

  /**
   * Instantiates a new Http error message.
   *
   * @param message the message
   * @param code    the code
   */
  public HttpErrorMessage(String message, String code) {
    this.message = message;
    this.code = code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getCode() {
    return message;
  }
}
