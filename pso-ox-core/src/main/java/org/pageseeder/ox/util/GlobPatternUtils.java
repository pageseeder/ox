/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author Carlos Cabral
 * @since 02 Feb. 2018
 */
public class GlobPatternUtils {
  private static final String GLOB_STRUCTURE_PATTERN = "^.*[\\*\\[\\]\\{\\}\\?!]+.*$";
  
  /**
   * Check if the pattern has at least one of the following characteres: *, [, ], {, }, ? and !
   * @param pattern
   * @return
   */
  public static boolean isGlobPattern(String pattern) {
    return Pattern.matches(GLOB_STRUCTURE_PATTERN, pattern);
  }
  
  /**
   * This method was got from ant class.
   * 
   * @param pattern
   * @return
   */
  public static String normalizePattern (String pattern) {
    String newPattern = pattern.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    if (pattern.endsWith(File.separator)) {
      pattern += "**";
    }
    return newPattern;
  }
}
