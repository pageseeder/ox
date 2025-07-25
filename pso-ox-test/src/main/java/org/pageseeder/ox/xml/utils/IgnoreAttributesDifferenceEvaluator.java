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

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import java.util.List;

/**
 * The Class IgnoreAttributesDifferenceEvaluator.
 *
 * @author Carlos Cabral
 * @since 20 Aug. 2018
 */
public class IgnoreAttributesDifferenceEvaluator implements DifferenceEvaluator {

  /** The attributes name. */
  private List<String> attributesName;

  /**
   * Instantiates a new ignore attributes difference evaluator.
   *
   * @param attributesName the attributes name
   */
  public IgnoreAttributesDifferenceEvaluator(@NotNull List<String> attributesName) {
      this.attributesName = attributesName;
  }

  @Override
  public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
      if (outcome == ComparisonResult.EQUAL)
          return outcome;
      final Node controlNode = comparison.getControlDetails().getTarget();
      if (controlNode instanceof Attr) {
          Attr attr = (Attr) controlNode;
          if (attributesName.contains(attr.getName())) {
              return ComparisonResult.SIMILAR;
          }
      }
      return outcome;
  }
}