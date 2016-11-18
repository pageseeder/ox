/*
 * Copyright (c) 2016 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.util;


/**
 *
 * @author Carlos Cabral
 * @Since  4 March 2016
 */
public class StringUtils {

  public static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
