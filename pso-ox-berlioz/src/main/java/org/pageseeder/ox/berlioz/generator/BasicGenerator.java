/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.generator;

import org.pageseeder.berlioz.content.ContentGenerator;

/**
 * A basic generator for LIXI
 * @author Ciber Cai
 * @version 19 February 2014
 */
public abstract class BasicGenerator implements ContentGenerator {

  protected static boolean validType(String name) {
    if ("schema".equals(name) || "xsl".equals(name)) {
      return true;
    } else {
      return false;
    }
  }

}
