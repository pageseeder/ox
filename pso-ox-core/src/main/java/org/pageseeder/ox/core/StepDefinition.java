/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */
package org.pageseeder.ox.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.CallbackStep;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.step.NOPStep;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Lauret
 * @since  8 May 2014
 */
public final class StepDefinition implements XMLWritable, Serializable {

  private static final long serialVersionUID = -4800248056806271809L;

  /** the logger */
  private final static Logger LOGGER = LoggerFactory.getLogger(StepDefinition.class);

  /** Model this step is part of */
  private final Model _model;

  /** Pipeline this step is part of */
  private final Pipeline _pipeline;

  /** ID for the step (unique within pipeline) */
  private final String _id;

  /** The name of the step */
  private final String _name;

  /** The step  */
  private final Step _step;

  /** The callback step */
  private final CallbackStep _callbackStep;

  /** The output of the step or <code>null</code> for no output. */
  private final String _output;

  /** Parameters to supply to the underlying step. */
  private final Map<String, String> _parameters;

  /** Position of step in pipeline lazily initialized */
  private transient int _position = -1;

  /**
   * Creates a new abstract step.
   *
   * <p>This constructor is <i>protected</i> so that only implementations use it.
   *
   * <p>To create step instance, use the {@link StepFactory}.
   *
   * @param model      The model this step is part of
   * @param pipeline   The pipeline this step is part of
   * @param id         ID for the step (unique within pipeline)
   * @param name       The name of step
   * @param parameters The list of parameter
   * @param output     The output of step
   * @param step       The step
   *
   * @throws NullPointerException if any of the argument is <code>null</code>
   * @throws IllegalArgumentException If the ID is not valid.
   */
  private StepDefinition(Model model, Pipeline pipeline, String id, String name, Map<String, String> parameters, String output, Step step) {
    this(model, pipeline, id, name, parameters, output, step, null);
  }

  /**
   * Creates a new abstract step.
   *
   * <p>This constructor is <i>protected</i> so that only implementations use it.
   *
   * <p>To create step instance, use the {@link StepFactory}.
   *
   * @param model        The model this step is part of
   * @param pipeline     The pipeline this step is part of
   * @param id           ID for the step (unique within pipeline)
   * @param name         The name of step
   * @param parameters   The list of parameter
   * @param output       The output of step
   * @param step         The step
   * @param callbackStep The callback step
   *
   * @throws NullPointerException if any of the argument is <code>null</code>
   * @throws IllegalArgumentException If the ID is not valid.
   */
  private StepDefinition(Model model, Pipeline pipeline, String id, String name, Map<String, String> parameters, String output, Step step, CallbackStep callbackStep) {
    if (model == null) { throw new NullPointerException("model is null."); }
    if (pipeline == null) { throw new NullPointerException("pipeline is null."); }
    if (id == null) { throw new NullPointerException("id is null."); }
    if (!isValidID(id)) { throw new IllegalArgumentException("id is invalid."); }
    this._model = model;
    this._pipeline = pipeline;
    this._id = id;
    this._name = name;
    this._step = step;
    this._callbackStep = callbackStep;
    this._parameters = parameters;
    this._output = output;
  }

  /**
   * @return A unique ID for the step within the pipeline.
   */
  public final String id() {
    return this._id;
  }

  /**
   * @return The step name.
   */
  public final String name() {
    return this._name;
  }

  /**
   * @return The model the pipeline is part of
   */
  public final Model model() {
    return this._model;
  }

  /**
   * @return The pipeline this step is part of.
   */
  public final Pipeline pipeline() {
    return this._pipeline;
  }

  /**
   * @return The previous step if any
   */
  public final StepDefinition previous() {
    if (this._position < 0) {
      this._position = findSelfPosition();
    }
    return (this._position > 0) ? this._pipeline.getStep(this._position - 1) : null;
  }

  /**
   * @return The next step if any
   */
  public final StepDefinition next() {
    if (this._position < 0) {
      this._position = findSelfPosition();
    }
    return (this._position < this._pipeline.size() - 1) ? this._pipeline.getStep(this._position + 1) : null;
  }

  /**
   * @return the output of this step
   */
  public final String output() {
    return this._output;
  }

  /**
   * Execute this step for specified data.
   *
   * @param data The package data
   *
   * @return The result of this set
   */
  public Result exec(PackageData data) {
    // TODO replace tokens in output and parameters
    String input = getInput(data);
    // use input as output if output is null
    String output = this._output != null ? this._output : input;
    Map<String, String> parameters = Collections.unmodifiableMap(this._parameters);
    // step info
    StepInfoImpl info = new StepInfoImpl(this._id, this._name, input, output, parameters);
    // process the step
    Result result = this._step.process(this._model, data, info);
    // process the callback step
    if (this._callbackStep != null) {
      try {
        // put the step result to callback step
        this._callbackStep.process(data, result, info);
      } catch (Exception ex) {
        // if error occur, show the warning but doesn't effect the actual step.
        LOGGER.warn("Execute callback step error.", ex);
      }
    }

    return result;
  }

  /**
   * @return current Step
   */
  public Step getStep() {
    return this._step;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    toXML(null, xml);
  }

  /**
   * @param result the Result
   * @param xml the XMLWriter
   * @throws IOException when IO exception occur
   */
  public void toXML(Result result, XMLWriter xml) throws IOException {
    xml.openElement("step");
    xml.attribute("id", id());
    xml.attribute("model", model().name());

    if (name() != null) {
      xml.attribute("name", name());
    }

    if (this._step != null) {
      xml.attribute("step", this._step.getClass().getName());
    }

    if (this._callbackStep != null) {
      xml.attribute("callback", this._callbackStep.getClass().getName());
    }

    if (output() != null) {
      xml.attribute("output", output());
    }

    if (this._position > 1) {
      xml.attribute("position", this._position);
    }
    if (next() != null) {
      xml.attribute("next-id", next().id());
    }
    if (this._parameters != null) {
      for (String name : this._parameters.keySet()) {
        xml.openElement("parameter");
        xml.attribute("name", name);
        xml.attribute("value", this._parameters.get(name));
        xml.closeElement();// parameter
      }
    }

    if (result != null) {
      result.toXML(xml);
    }
    xml.closeElement();// step
  }

  // static methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Indicates whether the specified id can be used as a step ID.
   *
   * @param id the step ID to test
   * @return <code>true</code> if the ID is valid; <code>false</code> otherwise.
   */
  public static boolean isValidID(String id) {
    return id != null && id.matches("[A-Za-z_0-9\\-]+");
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Find this step in the internal pipeline
   *
   * @return the position;
   *
   * @throws IllegalStateException if the step does not belong to the pipeline
   */
  private int findSelfPosition() {
    for (int i = 0; i < this._pipeline.size(); i++) {
      if (this == this._pipeline.getStep(i)) { return i; }
    }
    throw new IllegalStateException("This step does not belong to the pipeline");
  }

  /**
   * @param data the PackageData
   * @return the input
   */
  private String getInput(PackageData data) {
    String input = null;
    // Find most recent output
    StepDefinition previous = this;
    while (input == null && previous != null) {
      previous = previous.previous();
      if (previous != null) {
        input = previous.output();
      }
    }
    // If no output, fallback on source
    if (input == null) {
      input = data.getPath(data.getOriginal());
    }
    return input;
  }

  /**
   * Used to construct a step definition - mostly for the benefit of the parser.
   *
   * @author Christophe Lauret
   * @since  13 June 2014
   */
  protected static final class Builder {

    private final Model _model;

    private Pipeline pipeline;

    private String classname;

    private String callbackClassname;

    private String id;

    private String name;

    private Map<String, String> parameters = new HashMap<String, String>();

    private String output = null;

    /**
     * @param model the Model.
     */
    public Builder(Model model) {
      if (model == null) { throw new NullPointerException("Model must be specified"); }
      this._model = model;
    }

    /**
     * @param pipeline the pipeline to set
     * return the {@link Builder}
     */
    public Builder setPipeline(Pipeline pipeline) {
      this.pipeline = pipeline;
      return this;
    }

    /**
     * @param classname the classname to set
     * return the {@link Builder}
     */
    public Builder setStepClass(String classname) {
      this.classname = classname;
      return this;
    }

    /**
     * @param callback the callback classname to set
     * return the {@link Builder}
     */
    public Builder setCallback(String callback) {
      this.callbackClassname = callback;
      return this;
    }

    /**
     * @param id the id to set
     * return the {@link Builder}
     */
    public Builder setStepId(String id) {
      this.id = id;
      return this;
    }

    /**
     *
     * @param name the name of step
     * return the {@link Builder}
     */
    public Builder setStepName(String name) {
      this.name = name;
      return this;
    }

    /**
     * @param name the name of parameter
     * @param value the value of parameter
     */
    public Builder addParameter(String name, String value) {
      this.parameters.put(name, value);
      return this;
    }

    /**
     * @param output the output to set
     * return the {@link Builder}
     */
    public Builder setOutput(String output) {
      this.output = output;
      return this;
    }

    /**
     * Reset the parameters and output
     */
    public void reset() {
      this.parameters = new HashMap<String, String>();
      this.output = null;
    }

    /**
     * Build the step from the arguments of this class;
     *
     * @return the StepDefinition.
     */
    @SuppressWarnings("unchecked")
    public StepDefinition build() throws OXException {
      StepDefinition definition = null;
      try {
        Step step = null;

        // if not class name defined uses NOPStep instead
        if (this.classname == null) {
          this.classname = NOPStep.class.getName();
        }

        // Instantiate the step
        try {
          // try to get the class
          Class<Step> c = (Class<Step>) Class.forName(this.classname);
          step = c.newInstance();
        } catch (ClassNotFoundException ex) {
          throw new OXException("Cannot find Class " + this.classname, ex);
        } catch (InstantiationException ex) {
          throw new OXException("Cannot instance of a class " + this.classname, ex);
        } catch (ClassCastException ex) {
          throw new OXException("Cannot instance of a Step class " + this.classname, ex);
        }

        // Instantiate the callback step
        CallbackStep callback = null;
        try {
          if (this.callbackClassname != null) {
            Class<CallbackStep> c = (Class<CallbackStep>) Class.forName(this.callbackClassname);
            callback = c.newInstance();
          }
        } catch (ClassNotFoundException ex) {
          throw new OXException("Cannot find Class " + this.callbackClassname, ex);
        } catch (InstantiationException ex) {
          throw new OXException("Cannot instance of a class " + this.callbackClassname, ex);
        } catch (ClassCastException ex) {
          throw new OXException("Cannot instance of a Callback Step class " + this.callbackClassname, ex);
        }

        if (callback != null) {
          definition = new StepDefinition(this._model, this.pipeline, this.id, this.name, this.parameters, this.output, step, callback);
        } else {
          definition = new StepDefinition(this._model, this.pipeline, this.id, this.name, this.parameters, this.output, step);
        }
      } catch (Exception ex) {
        throw new OXException("Unable to build step.", ex);
      }
      return definition;
    }
  }
}
