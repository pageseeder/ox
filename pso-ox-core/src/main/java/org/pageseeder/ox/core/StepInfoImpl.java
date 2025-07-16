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
package org.pageseeder.ox.core;

import org.pageseeder.ox.api.StepInfo;

import java.util.Map;

/**
 * Aa implementation of StepInfo.
 *
 * @author Christophe Lauret
 * @since  8 May 2014
 */
public final class StepInfoImpl implements StepInfo {

  /** the id of step */
  private final String _id;

  /** the name of step */
  private final String _name;

  /** the input path of Step **/
  private final String _input;

  /** the output path of Step **/
  private final String _output;

  /** the parameters of Step **/
  private final Map<String, String> _parameters;

  /**
   *
   * From May 2025 it will allow input and output to be null. Because the OX starts allowing the users to start a
   * pipeline without an input file.
   *
   * @param id The id of step
   * @param name The name of step.
   * @param input The input path
   * @param output The output path
   * @param parameters The list of parameters
   */
  public StepInfoImpl(String id, String name, String input, String output, Map<String, String> parameters) {
    if (id == null) { throw new NullPointerException("id is null."); }
    if (name == null) { throw new NullPointerException("name is null."); }
    if (parameters == null) { throw new NullPointerException("parameters is null."); }
    this._id = id;
    this._name = name;
    this._input = input;
    this._output = output;
    this._parameters = parameters;
  }

  @Override
  public String id() {
    return this._id;
  }

  @Override
  public String name() {
    return this._name;
  }

  @Override
  public String input() {
    return this._input;
  }

  @Override
  public String output() {
    return this._output;
  }

  @Override
  public Map<String, String> parameters() {
    return this._parameters;
  }

  @Override
  public String getParameter(String name) {
    return this._parameters.get(name);
  }

  @Override
  public String getParameter(String name, String def) {
    String val = this.getParameter(name);
    return val != null ? val : def;
  }

}
