/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.tool;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;

/**
 * Defines a command to run on a specific data package.
 *
 * <p>Additional parameters for the command can be specified as class attributes.
 *
 * @param T the type of results returned by this command.
 *
 * @author Christophe Lauret
 * @since  28 October 2013
 *
 * @deprecated this no longer as a interface
 */
@Deprecated
public interface Command<R extends Result> {

  /**
   * Return the model this command uses.
   *
   * @return the model this command uses.
   */
  Model getModel();

  /**
   * Processes the specified package data according to the model defined for this command.
   *
   * <p>This method should intercept errors occurring during processing and include them in the results.
   *
   * @param data the data to process
   *
   * @return the result of processing specific to this command and data.
   */
  R process(PackageData data);

}
