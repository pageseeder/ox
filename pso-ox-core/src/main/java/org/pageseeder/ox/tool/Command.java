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
package org.pageseeder.ox.tool;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;

/**
 * Defines a command to run on a specific data package.
 *
 * <p>Additional parameters for the command can be specified as class attributes.
 * <p>
 * param R the type of results returned by this command.
 *
 * @param <R> the type parameter
 * @author Christophe Lauret
 * @since 28 October 2013
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
   * @return the result of processing specific to this command and data.
   */
  R process(PackageData data);

}
