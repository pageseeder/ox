package org.pageseeder.ox.berlioz.test.xml.utils;

import org.junit.Test;
import org.pageseeder.ox.xml.utils.IgnoreElementsDifferenceEvaluator;
import org.pageseeder.ox.xml.utils.XMLComparator;

import java.util.Arrays;
import java.util.List;

/**
 * @author ccabral
 * @since 19 January 2023
 */
public class IgnoreElementsDifferenceEvaluatorTest {


  @Test
  public void test(){
    String expectedXML = "<root><ignore>Text</ignore><not-ignore>test 01</not-ignore></root>";
    String xml = "<root><ignore>Text Changed</ignore><not-ignore>test 01</not-ignore></root>";
    List<String> elementsToIgnore = Arrays.asList("ignore");
    XMLComparator.isSimilar(expectedXML, xml, new IgnoreElementsDifferenceEvaluator(elementsToIgnore));
  }
}
