/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * The Class GlobPatternUtils.
 *
 * @author Carlos Cabral
 * @since 02 Feb. 2018
 */
public class GlobPatternUtils {

  /** The Constant GLOB_STRUCTURE_PATTERN. */
  private static final String GLOB_STRUCTURE_PATTERN = "^.*[\\*\\[\\]\\{\\}\\?!]+.*$";

  /**
   * Check if the pattern has at least one of the following characteres: *, [, ], {, }, ? and !.
   *
   * @param pattern the pattern
   * @return true, if is glob pattern
   */
  public static boolean isGlobPattern(String pattern) {
    return Pattern.matches(GLOB_STRUCTURE_PATTERN, pattern);
  }

  /**
   * This method was got from ant class.
   *
   * @param pattern the pattern
   * @return the string
   */
  public static String normalizePattern (String pattern) {
    String newPattern = pattern.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    if (pattern.endsWith(File.separator)) {
      pattern += "**";
    }
    return newPattern;
  }
}
