/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.api;

import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.xmlwriter.XMLWritable;

/**
 * The result of a command.
 *
 * <p>It should have an XML representation and wrap the information about the result of a command.
 *
 * @author Christophe Lauret
 * @since  28 October 2013
 */
public interface Result extends XMLWritable {

  /**
   * @return The time it took to execute the command in milliseconds.
   */
  long time();

  /**
   * @return The status of that result.
   */
  ResultStatus status();

  /**
   * @return Any error that may have occurred and caused the command to fail.
   */
  Exception error();

}
