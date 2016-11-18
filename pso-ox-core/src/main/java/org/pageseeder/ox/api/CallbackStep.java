/* Copyright (c) 1999-2015 Allette systems pty. ltd. */
package org.pageseeder.ox.api;

import org.pageseeder.ox.core.PackageData;

/**
 * This interface defines the CallbackStep.
 *
 * @author Ciber Cai
 * @since  07 May 2015
 */
public interface CallbackStep {

  /**
   * To process the callback step.
   *
   * @param data the data.
   * @param result the current step result.
   * @param info the step information.
   */
  void process(PackageData data, Result result, StepInfo info);

}
