package org.pageseeder.ox.http;

import org.pageseeder.ox.ErrorMessage;

/**
 * @author ccabral
 * @since 16 September 2021
 */
public class HttpErrorMessage implements ErrorMessage {
  private final String message;
  private final String code;

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
