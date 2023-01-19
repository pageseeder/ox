/*
 * Copyright 2023 Allette Systems (Australia)
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

import org.eclipse.jdt.annotation.NonNull;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import java.util.List;

/**
 * The Class IgnoreElementsDifferenceEvaluator.
 *
 * @author Carlos Cabral
 * @since 19 January 2023
 */
public class IgnoreElementsDifferenceEvaluator implements DifferenceEvaluator {

  /**
   * The Elements name.
   */
  private List<String> elementsName;

  /**
   * Instantiates a new ignore elements difference evaluator.
   *
   *
   * @note: It is still experimental.
   * @param elementsName the elements name
   */
  public IgnoreElementsDifferenceEvaluator(@NonNull List<String> elementsName) {
    this.elementsName = elementsName;
  }

  @Override
  public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
    if (outcome == ComparisonResult.EQUAL)
      return outcome;
    final Node controlNode = comparison.getControlDetails().getTarget();
    if (controlNode instanceof Element) {
      Element element = (Element) controlNode;
      if (elementsName.contains(element.getTagName())) {
        return ComparisonResult.SIMILAR;
      }
    } else if (controlNode instanceof Text) {
      Text text = (Text) controlNode;
      if (elementsName.contains(text.getParentNode().getNodeName())){
        return ComparisonResult.SIMILAR;
      }
    }
    return outcome;
  }
}