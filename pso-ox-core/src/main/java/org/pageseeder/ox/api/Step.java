/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.api;

import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;

/**
 * Implement this interface to define your own step.
 *
 * <p>Steps should be stateless, so that multiple {@link #process(Model, PackageData, StepInfo)} methods
 * can be invoked on different data, model or pipeline concurrently.
 *
 * @author Christophe Lauret
 * @since  13 June 2014
 */
public interface Step {

  /**
   * Processes the specified package data according to the model.
   *
   * <p>This method should intercept errors occurring during processing and include them in the results.
   *
   * @param model  the model this command uses.
   * @param data   the data to process.
   * @param info   information about the current step.
   *
   * @return the result of processing specific to this command and data.
   */
  Result process(Model model, PackageData data, StepInfo info);

}
