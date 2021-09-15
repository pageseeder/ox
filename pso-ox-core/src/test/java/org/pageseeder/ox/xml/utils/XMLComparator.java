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
package org.pageseeder.ox.xml.utils;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The Class XMLComparator.
 *
 * @author Carlos Cabral
 * @since 20 Aug. 2018
 */
public class XMLComparator {
  /**
   * Compare XML file.
   *
   * @param expected the expected
   * @param target the target
   */
  public static void compareXMLFile(File expected, File target) {
    assertNotNull("Expected File cannot be null.",expected);
    assertNotNull("Target File cannot be null.", target);
    assertThat(target, CompareMatcher.isIdenticalTo(expected));
  }

  /**
   * Compare XML file.
   *
   * @param expected the expected
   * @param target the target
   */
  public static void compareXMLString(String expected, String target) {
    assertNotNull("Expected File cannot be null.",expected);
    assertNotNull("Target File cannot be null.", target);
    assertThat(target, CompareMatcher.isIdenticalTo(expected));
  }

  /**
   * Checks if is similar.
   *
   * @param expected the expected
   * @param target the target
   * @param attributesToIgnore the attributes to ignore
   */
  public static void isSimilar (String expected, String target, List<String> attributesToIgnore) {
    isSimilar(expected, target, new IgnoreAttributesDifferenceEvaluator(attributesToIgnore));
  }

  /**
   * Checks if is similar.
   *
   * @param expected the expected
   * @param target the target
   * @param differenceEvaluator the difference evaluator
   */
  public static void isSimilar (String expected, String target, DifferenceEvaluator differenceEvaluator) {
    Diff myDiff = DiffBuilder.compare(expected)
        .withTest(target)
        .withDifferenceEvaluator(differenceEvaluator)
        .checkForSimilar()
        .build();
    assertFalse(myDiff.toString(), myDiff.hasDifferences());
  }
}