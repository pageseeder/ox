/*
 * Copyright (c) 2016 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.util.ArrayList;
import java.util.List;
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
   *
   * @param path the path
   * @return true, if is comma separate file list
   */
  public static boolean isCommaSeparateFileList(String path) {
    final String FILE_NAME_PATTERN = "[\\d\\w\\\\/:_\\-\\.\\s]";
    return Pattern.matches("(?:" + FILE_NAME_PATTERN + "+(?:," + FILE_NAME_PATTERN + "+)+)?", path);
  }
  
  /**
   * Convert to string.
   *
   * @param values the values
   * @param separator the separator
   * @return the string
   */
  public static String convertToString(String [] values, String separator) {
    StringBuilder converted = new StringBuilder();
    if (values != null) {
      for (String value : values) {
        if (converted.length() > 0) {
          converted.append(separator);
        }
        converted.append(value);
      }
    }
    return converted.toString();
  }
  
  /**
   * Convert to string.
   *
   * @param values the values
   * @param separator the separator
   * @return the string
   */
  public static String convertToString(List<String> values, String separator) {
    StringBuilder converted = new StringBuilder();
    if (values != null) {
      for (String value : values) {
        if (converted.length() > 0) {
          converted.append(separator);
        }
        converted.append(value);
      }
    }
    return converted.toString();
  }
  
  /**
   * Convert a String List.
   *
   * @param valuesCommaSeparated the values comma separated
   * @return the string
   */
  public static List<String> convertToStringList(String valuesCommaSeparated) {
    List<String> stringValues = new ArrayList<>();
    if (valuesCommaSeparated != null) {
      for(String value:valuesCommaSeparated.split(",")) {
        if (!isBlank(value)) {
          stringValues.add(value);
        }
      }
    }
    return stringValues;
  }
}
