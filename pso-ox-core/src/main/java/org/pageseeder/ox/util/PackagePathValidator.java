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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates step {@code input}, {@code output}, and {@code xslt} paths both statically (when
 * parsing {@code models.xml} into a {@code StepDefinition}) and dynamically (when executing a
 * {@code Step} against a base directory).
 */
public final class PackagePathValidator {

  /** Strict allowlist for runtime path segments without wildcards. */
  private static final Pattern SAFE_RUNTIME_SEGMENT =
      Pattern.compile("^[A-Za-z0-9_][A-Za-z0-9_.\\- ]{0,254}$");

  /** Allowlist for the final filename segment when glob wildcards ('*') are permitted on input. */
  private static final Pattern SAFE_GLOB_FILENAME =
      Pattern.compile("^[A-Za-z0-9_*][A-Za-z0-9_*.\\- ]{0,254}$");

  /** Static segment pattern for models.xml (allows {placeholder} tokens, no '*'). */
  private static final Pattern SAFE_DEFINITION_SEGMENT =
      Pattern.compile("^[A-Za-z0-9_{][A-Za-z0-9_.{}\\- ]{0,254}$");

  /** Static final filename pattern for models.xml when glob wildcards ('*') are permitted on input. */
  private static final Pattern SAFE_DEFINITION_GLOB_FILENAME =
      Pattern.compile("^[A-Za-z0-9_{*][A-Za-z0-9_*.{}\\- ]{0,254}$");

  /** Default safe extensions for OX pipeline inputs and outputs (lowercase, without leading dot). */
  public static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList(
          "xml", "json", "html", "htm", "xhtml", "txt", "csv", "css", "js", "svg", "png", "jpg", "jpeg", "gif", "webp",
          "pdf", "zip", "docx", "xlsx", "pptx", "epub", "md", "yaml", "yml", "xsl", "xslt", "sch", "sql", "psml",
          "properties"
      )));

  private final Set<String> allowedExtensions;
  private final boolean allowPlaceholdersInDefinition;

  public PackagePathValidator() {
    this(DEFAULT_ALLOWED_EXTENSIONS, true);
  }

  public PackagePathValidator(Set<String> allowedExtensions, boolean allowPlaceholdersInDefinition) {
    if (allowedExtensions == null || allowedExtensions.isEmpty()) {
      throw new IllegalArgumentException("Allowed extensions set must not be empty (fail-closed)");
    }
    Set<String> normalized = new HashSet<>();
    for (String ext : allowedExtensions) {
      if (ext != null) {
        String clean = ext.trim().toLowerCase(Locale.ROOT);
        if (clean.startsWith(".")) {
          clean = clean.substring(1);
        }
        if (!clean.isEmpty()) {
          normalized.add(clean);
        }
      }
    }
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Allowed extensions set must contain at least one valid extension");
    }
    this.allowedExtensions = Collections.unmodifiableSet(normalized);
    this.allowPlaceholdersInDefinition = allowPlaceholdersInDefinition;
  }

  /**
   * 1. STATIC VALIDATION (Use when building StepDefinition from models.xml).
   * Validates syntax, blocks traversal (".."), matrix params (";"), absolute OS paths,
   * and enforces extension rules ({@code .xsl} only for {@code xslt}, allowlist for {@code input}/{@code output}).
   *
   * @param rawPath the input, output, or xslt parameter value from models.xml (may be null/blank)
   * @param paramName "input", "output", or "xslt"
   * @throws IllegalArgumentException if the path is unsafe or has a disallowed extension
   */
  public void validateDefinitionPath(String rawPath, String paramName) {
    if (rawPath == null || rawPath.trim().isEmpty()) {
      return; // Omitted input/output/xslt defaults to step default
    }
    validateLexicalPath(rawPath, paramName, this.allowPlaceholdersInDefinition);
  }

  /**
   * 2. RUNTIME VALIDATION AND RESOLUTION (Use inside Step execution).
   * Validates the runtime parameter (rejecting any unresolved placeholders) and resolves
   * it safely inside {@code baseDirectory} (e.g., packageDir or modelDir).
   *
   * @param baseDirectory the root directory (package folder for input/output, or model folder for xslt)
   * @param rawPath the runtime input, output, or xslt path (may be null/blank)
   * @param paramName "input", "output", or "xslt"
   * @return the canonical, containment-verified {@link Path} inside {@code baseDirectory},
   *         or {@code null} if {@code rawPath} was null/blank
   * @throws SecurityException if traversal, symlink escape, or invalid path is detected
   * @throws IllegalArgumentException if syntax or extension rules are violated
   */
  public Path resolveAndValidate(File baseDirectory, String rawPath, String paramName) {
    if (baseDirectory == null) {
      throw new IllegalArgumentException("Base directory must not be null");
    }
    if (rawPath == null || rawPath.trim().isEmpty()) {
      return null;
    }

    // Strict runtime lexical check (no {placeholders} allowed at runtime)
    validateLexicalPath(rawPath, paramName, false);

    // Strip leading '/' (root-relative) and trailing '/' (directory marker)
    String cleaned = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
    if (cleaned.endsWith("/")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1);
    }

    Path baseDir = baseDirectory.toPath().toAbsolutePath().normalize();

    int lastSlash = cleaned.lastIndexOf('/');
    String parentRel = (lastSlash >= 0) ? cleaned.substring(0, lastSlash) : "";
    String leafName = (lastSlash >= 0) ? cleaned.substring(lastSlash + 1) : cleaned;

    Path targetParent = parentRel.isEmpty() ? baseDir : baseDir.resolve(parentRel).normalize();
    if (!targetParent.startsWith(baseDir)) {
      throw new SecurityException("Path traversal blocked for " + paramName + ": " + rawPath);
    }

    // Verify parent directory symlink containment on disk
    verifyRealPathContainment(baseDir, targetParent, paramName, rawPath);

    if (leafName.contains("*")) {
      try {
        Path candidate = targetParent.resolve(leafName).normalize();
        if (!candidate.startsWith(baseDir) || candidate.equals(baseDir)) {
          throw new SecurityException("Path traversal blocked for " + paramName + ": " + rawPath);
        }
        return candidate;
      } catch (java.nio.file.InvalidPathException e) {
        return targetParent;
      }
    }

    Path candidate = targetParent.resolve(leafName).normalize();
    if (!candidate.startsWith(baseDir) || candidate.equals(baseDir)) {
      throw new SecurityException("Path traversal blocked for " + paramName + ": " + rawPath);
    }

    verifyRealPathContainment(baseDir, candidate, paramName, rawPath);
    return candidate;
  }

  private void verifyRealPathContainment(Path baseDir, Path candidate, String paramName, String rawPath) {
    try {
      Path realBase = Files.exists(baseDir) ? baseDir.toRealPath() : baseDir;
      Path existingAncestor = findNearestExistingAncestor(candidate);
      if (existingAncestor != null) {
        Path realAncestor = existingAncestor.toRealPath();
        if (!realAncestor.startsWith(realBase)) {
          throw new SecurityException("Symlink escape blocked for " + paramName + ": " + rawPath);
        }
      }
      if (Files.isSymbolicLink(candidate)) {
        throw new SecurityException("Symbolic links are not allowed for " + paramName + ": " + rawPath);
      }
    } catch (IOException ex) {
      throw new SecurityException("Unable to verify real path for " + paramName + ": " + rawPath, ex);
    }
  }

  private void validateLexicalPath(String rawPath, String paramName, boolean allowPlaceholders) {
    if (rawPath.length() > 1024) {
      throw new IllegalArgumentException("Path too long for " + paramName);
    }

    if (rawPath.startsWith("\\") || rawPath.contains(":")) {
      throw new IllegalArgumentException("Windows root paths or schemes not allowed for " + paramName + ": " + rawPath);
    }
    for (int i = 0; i < rawPath.length(); i++) {
      char c = rawPath.charAt(i);
      // Note: '*' is checked per-segment below (allowed only in the final filename of an "input" parameter)
      if (c < 0x20 || c == 0x7f || c == '\\' || c == ';' || c == '?' || c == '#' || c == '%') {
        throw new IllegalArgumentException("Illegal character in " + paramName + ": " + rawPath);
      }
    }
    if (rawPath.contains("//")) {
      throw new IllegalArgumentException("Empty path segment '//' not allowed in " + paramName + ": " + rawPath);
    }

    // Strip single leading '/' (root-relative) and optional trailing '/' (explicit directory)
    String withoutLeading = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
    boolean explicitlyDirectory = withoutLeading.endsWith("/");
    String trimmed = explicitlyDirectory
        ? withoutLeading.substring(0, withoutLeading.length() - 1)
        : withoutLeading;

    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Root directory '/' is not a valid " + paramName + " path");
    }

    boolean isXsltParam = "xslt".equalsIgnoreCase(paramName);
    if (isXsltParam && explicitlyDirectory) {
      throw new IllegalArgumentException("Directory path is not allowed for xslt: " + rawPath);
    }

    String[] segments = trimmed.split("/", -1);
    boolean isInputParam = "input".equalsIgnoreCase(paramName);

    for (int i = 0; i < segments.length; i++) {
      String segment = segments[i];
      boolean isLastSegment = (i == segments.length - 1);

      if (".".equals(segment) || "..".equals(segment)) {
        throw new IllegalArgumentException("Path traversal ('..') not allowed in " + paramName + ": " + rawPath);
      }

      // Allow '*' ONLY in the final non-directory filename segment of an "input" parameter
      boolean allowGlobHere = isInputParam && isLastSegment && !explicitlyDirectory;
      Pattern segmentPattern;
      if (allowPlaceholders) {
        segmentPattern = allowGlobHere ? SAFE_DEFINITION_GLOB_FILENAME : SAFE_DEFINITION_SEGMENT;
      } else {
        segmentPattern = allowGlobHere ? SAFE_GLOB_FILENAME : SAFE_RUNTIME_SEGMENT;
      }

      if (!segmentPattern.matcher(segment).matches()) {
        throw new IllegalArgumentException("Invalid segment '" + segment + "' in " + paramName + ": " + rawPath);
      }
      if (segment.endsWith(".") || segment.endsWith(" ")) {
        throw new IllegalArgumentException("Trailing dot or space not allowed in " + paramName + ": " + rawPath);
      }
    }

    String lastSegment = segments[segments.length - 1];

    // If a wildcard '*' is used in the filename, it MUST specify a valid file extension (e.g., "*.xlsx"),
    // never a bare "*" or "*.*" that would match executable/script files.
    if (lastSegment.contains("*") && lastSegment.indexOf('.') < 0) {
      throw new IllegalArgumentException(
          "Wildcard in " + paramName + " must specify an allowed file extension: " + rawPath);
    }

    // Strict check for "xslt": must be a file ending specifically in ".xsl"
    if (isXsltParam) {
      int lastDot = lastSegment.lastIndexOf('.');
      if (lastDot < 0) {
        throw new IllegalArgumentException("XSLT path must specify a .xsl file, not a directory: " + rawPath);
      }
      String ext = lastSegment.substring(lastDot + 1).toLowerCase(Locale.ROOT);
      if (allowPlaceholders && ext.startsWith("{") && ext.endsWith("}")) {
        return;
      }
      if (!"xsl".equals(ext)) {
        throw new IllegalArgumentException(
            "File extension '." + ext + "' is not allowed for xslt (only .xsl is permitted): " + rawPath);
      }
      return;
    }

    // Check file extension on the final segment for input/output if it is not explicitly a directory ("dir/")
    // and contains a dot (indicating a filename rather than an extensionless directory name).
    if (!explicitlyDirectory && lastSegment.indexOf('.') >= 0) {
      int lastDot = lastSegment.lastIndexOf('.');
      String ext = lastSegment.substring(lastDot + 1).toLowerCase(Locale.ROOT);
      if (allowPlaceholders && ext.startsWith("{") && ext.endsWith("}")) {
        return; // Dynamic extension placeholder in models.xml; will be enforced at runtime
      }
      if (!this.allowedExtensions.contains(ext)) {
        throw new IllegalArgumentException(
            "File extension '." + ext + "' is not allowed for " + paramName + ": " + rawPath);
      }
    }
  }

  private static Path findNearestExistingAncestor(Path path) {
    Path current = path;
    while (current != null && !Files.exists(current)) {
      current = current.getParent();
    }
    return current;
  }
}