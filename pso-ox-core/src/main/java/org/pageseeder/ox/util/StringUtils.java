/*
 * Copyright (c) 2016 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.util;


/**
 * The Class StringUtils.
 *
 * @author Carlos Cabral
 * @since  4 March 2016
 */
public class StringUtils {

  /**
   * Checks if is blank (null or lenght = 0).
   *
   * @param value the value
   * @return true, if is blank
   */
  public static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
