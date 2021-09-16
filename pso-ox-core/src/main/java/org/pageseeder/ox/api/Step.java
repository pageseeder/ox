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
