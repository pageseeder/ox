/*
 * Copyright 2026 Allette Systems (Australia)
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

import org.pageseeder.ox.core.PackageData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces the absolute location of a package directory in a message by a short placeholder.
 *
 * @author ccabral
 * @since 27 October 2026
 */
public final class PathMasker {

  private PathMasker() {
  }

  /**
   * Replaces the absolute location of the package directory by a short placeholder
   * so that messages do not leak server paths.
   *
   * @param message the message
   * @param data    the data
   * @return the string
   */
  public static String maskPath(String message, PackageData data) {
    if (message == null) return null;
    File dir = data.directory();
    return maskPath(message, dir, data.id());
  }

  /**
   * Masks every known form (native, URI, forward slashes, canonical) of the directory.
   *
   * @param message the message that may contain the directory
   * @param dir     the package directory, e.g. data.directory()
   * @param id      the package id, e.g. data.id(); it is kept in the placeholder
   * @return the message with the directory replaced by [id]
   */
  public static String maskPath(String message, File dir, String id) {
    if (message == null || dir == null) return message;
    String placeholder = "[" + id + "]";

    Set<String> variants = new LinkedHashSet<>();
    addVariants(variants, dir);
    try {
      File canonical = dir.getCanonicalFile();
      if (!canonical.equals(dir.getAbsoluteFile())) addVariants(variants, canonical);
    } catch (IOException ex) {
      // ignore, the absolute variants are still used
    }

    // Longest first so that a shorter variant never leaves a partial prefix behind
    List<String> ordered = new ArrayList<>(variants);
    ordered.sort(Comparator.comparingInt(String::length).reversed());

    String masked = message;
    for (String v : ordered) {
      if (v.isEmpty()) continue;
      // Case-insensitive to cope with drive letter differences (C: versus c:)
      masked = Pattern.compile(Pattern.quote(v), Pattern.CASE_INSENSITIVE)
          .matcher(masked).replaceAll(Matcher.quoteReplacement(placeholder));
    }
    return masked;
  }

  private static void addVariants(Set<String> variants, File dir) {
    String abs = trimTrailingSeparator(dir.getAbsolutePath());
    variants.add(abs);                       // native: C:\Users\x or /tmp/x
    variants.add(abs.replace('\\', '/'));    // C:/Users/x

    String uri = trimTrailingSeparator(dir.toURI().toString()); // file:/C:/Users/x or file:/tmp/x
    variants.add(uri);
    String noScheme = uri.replaceFirst("^file:", "");           // /C:/Users/x or /tmp/x
    variants.add(noScheme);
    variants.add("file://" + noScheme);                         // file:///C:/Users/x
    if (noScheme.matches("^/[A-Za-z]:.*")) {
      variants.add(noScheme.substring(1));                      // C:/Users/x
    }
  }

  private static String trimTrailingSeparator(String s) {
    while (s.length() > 1 && (s.endsWith("/") || s.endsWith("\\"))) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
}
