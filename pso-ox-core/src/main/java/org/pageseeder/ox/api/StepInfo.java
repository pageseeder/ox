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

import java.util.Map;

/**
 * This interface defines the context information for a step.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since 13 June 2014
 */
public interface StepInfo {

  /***
   * The id of the step;
   * @return the id of step (never <code>null</code>)
   */
  String id();

  /**
   * The name of step.
   *
   * @return the name (title) of step
   */
  String name();

  /**
   * The path to the input to process within the package data.
   *
   * @return the path to the input to process within the package data (never <code>null</code>)
   */
  String input();

  /**
   * The path to the output of the step within the package data.
   *
   * @return the path to the output of the step within the package data (never <code>null</code>)
   */
  String output();

  // XXX: We might want to use null to specify when there is no output for the step.

  /**
   * The parameters that may be used by the step.
   *
   * <p>This method should return an empty map if there are no parameters.
   *
   * @return parameters that may be used by the step (never <code>null</code>)
   */
  Map<String, String> parameters();

  /**
   * Returns the parameter for the specified name
   *
   * @param name the name of the parameter to retrieve.
   * @return the parameter value or <code>null</code> if no corresponding parameter value.
   */
  String getParameter(String name);

  /**
   * Returns the parameter for the specified name
   *
   * @param name the name of the parameter to retrieve.
   * @param def  the default value of the parameter when name is null.
   * @return the parameter value or <code>null</code> if no corresponding parameter value.
   */
  String getParameter(String name, String def);

}
