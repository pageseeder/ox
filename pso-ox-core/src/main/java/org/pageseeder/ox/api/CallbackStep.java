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
 * This interface defines the CallbackStep.
 *
 * @author Ciber Cai
 * @since 07 May 2015
 */
public interface CallbackStep {

  /**
   * To process the callback step.
   *
   * @param model  the model this callback uses.
   * @param data   the data.
   * @param result the current step result.
   * @param info   the step information.
   */
  void process(Model model, PackageData data, Result result, StepInfo info);

}
