/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.core;

import org.pageseeder.ox.api.StepInfo;

import java.util.Map;

/**
 * A implementation of StepInfo.
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
   * @param id The id of step
   * @param name The name of step.
   * @param input The input path
   * @param output The output path
   * @param parameters The list of parameters
   */
  public StepInfoImpl(String id, String name, String input, String output, Map<String, String> parameters) {
    if (id == null) { throw new NullPointerException("id is null."); }
    if (name == null) { throw new NullPointerException("name is null."); }
    if (input == null) { throw new NullPointerException("input is null."); }
    if (output == null) { throw new NullPointerException("output is null."); }
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
