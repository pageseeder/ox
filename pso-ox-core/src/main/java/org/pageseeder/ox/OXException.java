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

  /**
   */
  private static final long serialVersionUID = -3446542849381631998L;

  /**
   *
   */
  public OXException() {}

  /**
   * @param message the message of the Exception
   */
  public OXException(String message) {
    super(message);
  }

  /**
   * @param cause the {@link Throwable}
   */
  public OXException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message the message of the exception
   * @param cause the {@link Throwable}
   */
  public OXException(String message, Throwable cause) {
    super(message, cause);
  }

}
