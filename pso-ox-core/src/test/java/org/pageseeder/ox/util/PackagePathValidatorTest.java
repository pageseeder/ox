/**
 * Copyright (c) 2026 Allette systems pty. ltd.
 *
 */
package org.pageseeder.ox.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Unit tests for {@link PackagePathValidator}.
 */
public class PackagePathValidatorTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private File packageDir;
  private PackagePathValidator validator;

  @Before
  public void setUp() throws IOException {
    this.packageDir = tempFolder.newFolder("PBS-WEBSITE-EB54A7-39B7");
    this.validator = new PackagePathValidator();
  }

  // =========================================================================
  // 1. Constructor & Fail-Closed Configuration Tests
  // =========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorRejectsNullExtensions() {
    new PackagePathValidator(null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorRejectsEmptyExtensions() {
    new PackagePathValidator(Collections.emptySet(), true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorRejectsBlankOnlyExtensions() {
    new PackagePathValidator(new HashSet<>(Arrays.asList(" ", ".")), true);
  }

  @Test
  public void testCustomExtensionNormalizationCaseInsensitive() {
    PackagePathValidator custom = new PackagePathValidator(
        new HashSet<>(Arrays.asList(".XML", "  Json ")), false);

    custom.validateDefinitionPath("data/output.xml", "output");
    custom.validateDefinitionPath("data/output.JSON", "output");
  }

  // =========================================================================
  // 2. Static Definition Validation Tests (models.xml load time)
  // =========================================================================

  @Test
  public void testDefinitionAllowsNullAndBlankPaths() {
    // Omitted input/output defaults to uploaded file or step default
    validator.validateDefinitionPath(null, "input");
    validator.validateDefinitionPath("", "input");
    validator.validateDefinitionPath("   ", "output");
  }

  @Test
  public void testDefinitionAllowsValidFilesAndDirectories() {
    validator.validateDefinitionPath("workbook.xml", "input");
    validator.validateDefinitionPath("data/workbook.xml", "input");
    validator.validateDefinitionPath("data/sub-folder_1/result.json", "output");
    validator.validateDefinitionPath("output/", "output");
    validator.validateDefinitionPath("data/dist", "output");
    validator.validateDefinitionPath(    "config.properties", "output");
  }

  @Test
  public void testDefinitionAllowsPlaceholdersWhenEnabled() {
    validator.validateDefinitionPath("{input}", "input");
    validator.validateDefinitionPath("data/{filename}.xml", "output");
    validator.validateDefinitionPath("{package}/output.{ext}", "output");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDefinitionRejectsPlaceholdersWhenDisabled() {
    PackagePathValidator strictValidator =
        new PackagePathValidator(PackagePathValidator.DEFAULT_ALLOWED_EXTENSIONS, false);
    strictValidator.validateDefinitionPath("data/{filename}.xml", "output");
  }

  @Test
  public void testDefinitionBlocksAttackerTraversalPayloads() {
    String[] maliciousInputs = {
        "../../../../bin/sh",
        "../../../../bin/env",
        "../../../../bin/ls",
        "../../../../../../../../../../usr/bin/find",
        "data/../../secret.xml",
        "..",
        "."
    };

    for (String payload : maliciousInputs) {
      try {
        validator.validateDefinitionPath(payload, "input");
        fail("Expected IllegalArgumentException for traversal payload: " + payload);
      } catch (IllegalArgumentException expected) {
        // Expected
      }
    }
  }

  @Test
  public void testDefinitionBlocksMatrixParameterBypasses() {
    String[] matrixPayloads = {
        "i9996ver.xml;.css",
        "i9996repo.txt;.css",
        "data;.css/workbook.xml",
        "output.xml;jsessionid=1234"
    };

    for (String payload : matrixPayloads) {
      try {
        validator.validateDefinitionPath(payload, "output");
        fail("Expected IllegalArgumentException for matrix parameter payload: " + payload);
      } catch (IllegalArgumentException expected) {
        // Expected
      }
    }
  }

  @Test
  public void testDefinitionBlocksDisallowedFileExtensions() {
    String[] disallowedFiles = {
        "shell.jsp",
        "backdoor.jspx",
        "script.sh",
        "app.jar",
        "payload.exe",
        "UPPERCASE.JSP"
    };

    for (String payload : disallowedFiles) {
      try {
        validator.validateDefinitionPath(payload, "output");
        fail("Expected IllegalArgumentException for disallowed extension: " + payload);
      } catch (IllegalArgumentException expected) {
        // Expected
      }
    }
  }

  @Test
  public void testDefinitionBlocksAbsolutePathsSchemesAndIllegalChars() {
    String[] invalidPaths = {
        "\\Windows\\System32\\cmd.exe",
        "C:/Windows/win.ini",
        "https://webhook.site/test.xml",
        "https:/webhook.site/test.xml",
        "data//workbook.xml",
        "data/workbook.xml?path=secret",
        "data/workbook.xml#anchor",
        "data/*.xml",
        "data/%2e%2e/secret.xml",
        "data/trailing-dot.",
        "data/trailing-space ",
        ".hidden.xml"
    };

    for (String payload : invalidPaths) {
      try {
        validator.validateDefinitionPath(payload, "input");
        fail("Expected IllegalArgumentException for invalid path: " + payload);
      } catch (IllegalArgumentException expected) {
        // Expected
      }
    }
  }

  // =========================================================================
  // 3. Runtime Resolution & Containment Tests (Step Execution)
  // =========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testRuntimeRejectsNullPackageDirectory() {
    validator.resolveAndValidate(null, "data/workbook.xml", "input");
  }

  @Test
  public void testRuntimeReturnsNullWhenPathIsNullOrBlank() {
    assertNull(validator.resolveAndValidate(packageDir, null, "input"));
    assertNull(validator.resolveAndValidate(packageDir, "", "input"));
    assertNull(validator.resolveAndValidate(packageDir, "   ", "output"));
  }

  @Test
  public void testRuntimeResolvesValidExistingInputFile() throws IOException {
    File dataDir = new File(packageDir, "data");
    assertTrue(dataDir.mkdirs());
    File workbook = new File(dataDir, "workbook.xml");
    assertTrue(workbook.createNewFile());

    Path resolved = validator.resolveAndValidate(packageDir, "data/workbook.xml", "input");
    assertNotNull(resolved);
    assertEquals(workbook.toPath().toAbsolutePath().normalize(), resolved);
  }

  @Test
  public void testRuntimeResolvesNonExistentOutputFileAndDirectory() {
    Path resolvedFile = validator.resolveAndValidate(packageDir, "out/result.xml", "output");
    assertNotNull(resolvedFile);
    assertTrue(resolvedFile.startsWith(packageDir.toPath().toAbsolutePath().normalize()));

    Path resolvedDir = validator.resolveAndValidate(packageDir, "out/reports/", "output");
    assertNotNull(resolvedDir);
    assertEquals(
        packageDir.toPath().resolve("out/reports").toAbsolutePath().normalize(),
        resolvedDir);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRuntimeRejectsUnresolvedPlaceholders() {
    // Even though default validator allows {placeholders} in models.xml,
    // runtime resolution must strictly reject unresolved '{' or '}' characters.
    validator.resolveAndValidate(packageDir, "data/{filename}.xml", "output");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRuntimeBlocksTraversalFromHttpParameter() {
    validator.resolveAndValidate(packageDir, "../../../../bin/sh", "input");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRuntimeBlocksMatrixSuffixFromHttpParameter() {
    validator.resolveAndValidate(packageDir, "i9996ver.xml;.css", "output");
  }

  @Test
  public void testRuntimeBlocksSymlinkEscapeOutsidePackageDir() throws IOException {
    File outsideDir = tempFolder.newFolder("outside-secrets");
    File secretFile = new File(outsideDir, "secret.xml");
    assertTrue(secretFile.createNewFile());

    Path symlinkDir = packageDir.toPath().resolve("linked-dir");
    try {
      Files.createSymbolicLink(symlinkDir, outsideDir.toPath());
    } catch (UnsupportedOperationException | IOException e) {
      // Skip test on Windows environments without symlink privileges
      assumeTrue("Symbolic links not supported in this test environment", false);
    }

    try {
      validator.resolveAndValidate(packageDir, "linked-dir/secret.xml", "input");
      fail("Expected SecurityException when resolving path through symlink outside packageDir");
    } catch (SecurityException expected) {
      assertTrue(expected.getMessage().contains("Symlink escape blocked"));
    }
  }

  @Test
  public void testPackageRootRelativePathsAreAllowedAndContained() {
    // Static definition check
    validator.validateDefinitionPath("/original/psml", "output");
    validator.validateDefinitionPath("/original/psml/", "output");
    validator.validateDefinitionPath("/original/psml/document.xml", "input");

    // Runtime resolution check: resolves inside packageDir, NEVER OS root
    Path resolvedDir = validator.resolveAndValidate(packageDir, "/original/psml", "output");
    assertEquals(
        packageDir.toPath().resolve("original/psml").toAbsolutePath().normalize(),
        resolvedDir);
    assertTrue(resolvedDir.startsWith(packageDir.toPath().toAbsolutePath().normalize()));
  }

  @Test
  public void testLeadingSlashTraversalStillBlocked() {
    String[] invalidPaths = {
        "/",
        "//etc/passwd",
        "/../bin/sh",
        "/original/../../bin/sh",
        "\\Windows\\System32\\cmd.exe",
        "C:/Windows/win.ini",
        "https://webhook.site/test.xml",
        "https:/webhook.site/test.xml"
    };

    for (String payload : invalidPaths) {
      try {
        validator.validateDefinitionPath(payload, "input");
        fail("Expected IllegalArgumentException for invalid path: " + payload);
      } catch (IllegalArgumentException expected) {
        // Expected
      }
    }
  }


}