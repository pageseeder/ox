/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.xml.utils;

import org.eclipse.jdt.annotation.NonNull;
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
  public IgnoreAttributesDifferenceEvaluator(@NonNull List<String> attributesName) {
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