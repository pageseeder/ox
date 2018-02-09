/*
 * Copyright (c) 2016 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.util.regex.Pattern;

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
  
  /**
   * Will just validate if there is at least a comma between 2 files path.
   * If it starts or ends with comma, it will return false
   * @param path
   * @return
   */
  public static boolean isCommaSeparateFileList(String path) {
    final String FILE_NAME_PATTERN = "[\\d\\w\\\\/:_\\-\\.\\s]";
    return Pattern.matches("(?:" + FILE_NAME_PATTERN + "+(?:," + FILE_NAME_PATTERN + "+)+)?", path);
  }
}
