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

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.CallbackStep;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.step.NOPStep;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

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

  /**
   * Indicate if this step should be executed as asynchronous. The default false.
   */
  private final boolean _async;

  /**
   * If the output of the step is viewable.
   */
  private final boolean _viewable;

  /**
   * If the process should stop on the first error.
   */
  private final boolean _failOnError;

  /**
   * Send of parameter when the pipeline is synchronous.
   */
  private final boolean _downloadable;

  /** The output of the step or <code>null</code> for no output. */
  private final String _output;

  /** Parameters to supply to the underlying step. */
  private final Map<String, String> _parameters;

  /** If there are any other attributes that are not expected. */
  private final Map<String, String> _extraAttributes;

  /** If there are any other elements that are not expected. */
  private final List<GenericInfo> _extraElements;

  /** Position of step in pipeline lazily initialized */
  private transient int _position = -1;

  /**
   * Creates a new abstract step.
   *
   * <p>This constructor is <i>protected</i> so that only implementations use it.
   *
   * <p>To create step instance, use the {@link Builder}.
   *
   * @param model         The model this step is part of
   * @param pipeline      The pipeline this step is part of
   * @param id            ID for the step (unique within pipeline)
   * @param name          The name of step
   * @param parameters    The list of parameter
   * @param output        The output of step
   * @param async         The async
   * @param viewable      The viewable
   * @param downloadable  The downloadable
   * @param failOnError   If the process stops on the first error
   * @param step          The step
   * @param extraAttributes The extra attributes
   * @param extraElements The extra elements
   * @throws NullPointerException if any of the argument is <code>null</code>
   * @throws IllegalArgumentException If the ID is not valid.
   */
  private StepDefinition(Model model, Pipeline pipeline, String id, String name, Map<String, String> parameters, String output,
                         boolean async, boolean viewable, boolean downloadable, boolean failOnError, Step step,
                         Map<String, String> extraAttributes,  List<GenericInfo> extraElements) {
    this(model, pipeline, id, name, parameters, output, async, viewable, downloadable, failOnError, step, extraAttributes, extraElements, null);
  }

  /**
   * Creates a new abstract step.
   *
   * <p>This constructor is <i>protected</i> so that only implementations use it.
   *
   * <p>To create step instance, use the {@link Builder}.
   *
   * @param model        The model this step is part of
   * @param pipeline     The pipeline this step is part of
   * @param id           ID for the step (unique within pipeline)
   * @param name         The name of step
   * @param parameters   The list of parameter
   * @param output       The output of step
   * @param async        The async
   * @param viewable     The viewable
   * @param failOnError  If the process stops on the first error
   * @param downloadable The downloadable
   * @param step         The step
   * @param callbackStep The callback step
   * @throws NullPointerException if any of the argument is <code>null</code>
   * @throws IllegalArgumentException If the ID is not valid.
   */
  private StepDefinition(Model model, Pipeline pipeline, String id, String name, Map<String, String> parameters, String output,
                         boolean async, boolean viewable, boolean downloadable, boolean failOnError, Step step,
                         Map<String, String> extraAttributes, List<GenericInfo> extraElements, CallbackStep callbackStep) {
    if (model == null) { throw new NullPointerException("model is null."); }
    if (pipeline == null) { throw new NullPointerException("pipeline is null."); }
    if (id == null) { throw new NullPointerException("id is null."); }
    if (!isValidID(id)) { throw new IllegalArgumentException("id is invalid."); }
    this._model = model;
    this._pipeline = pipeline;
    this._id = id;
    this._name = name;
    this._step = step;
    this._async = async;
    this._viewable = viewable;
    this._failOnError = failOnError;
    this._downloadable = downloadable;
    this._callbackStep = callbackStep;
    this._parameters = parameters;
    this._output = output;
    this._extraAttributes = extraAttributes;
    this._extraElements = extraElements;
  }

  /**
   * @return A unique ID for the step within the pipeline.
   */
  public String id() {
    return this._id;
  }

  /**
   * @return The step name.
   */
  public String name() {
    return this._name;
  }

  /**
   * @return The model the pipeline is part of
   */
  public Model model() {
    return this._model;
  }

  /**
   * @return The pipeline this step is part of.
   */
  public Pipeline pipeline() {
    return this._pipeline;
  }

  /**
   * @return is async.
   */
  public boolean async() {
    return this._async;
  }

  /**
   * @return fails on error.
   */
  public boolean failOnError() {
    return this._failOnError;
  }

  /**
   * @return is viewable.
   */
  public boolean viewable() {
    return this._viewable;
  }

  /**
   * @return is downloadable.
   */
  public boolean downloadable() {
    return this._downloadable;
  }

  /**
   * Step parameters.
   */
  public Map<String, String> parameters() {
    return Collections.unmodifiableMap(this._parameters);
  }


  /**
   * @return The previous step if any
   */
  public StepDefinition previous() {
    if (this._position < 0) {
      this._position = findSelfPosition();
    }
    return (this._position > 0) ? this._pipeline.getStep(this._position - 1) : null;
  }

  /**
   * @return The next step if any
   */
  public StepDefinition next() {
    if (this._position < 0) {
      this._position = findSelfPosition();
    }
    return (this._position < this._pipeline.size() - 1) ? this._pipeline.getStep(this._position + 1) : null;
  }

  /**
   * @return the output of this step
   */
  public  String output() {
    return this._output;
  }

  /**
   *
   * @return the callback of step.
   */
  public CallbackStep callbackStep() {
    return this._callbackStep;
  }
  /**
   * Execute this step for specified data.
   *
   * @param data The package data
   *
   * @return The result of this set
   */
  public Result exec(PackageData data) {
    //TODO Maybe it will need to receive the session in order to allow the steps to get user logged
    Result result;
    try {
      // TODO replace tokens in output and parameters
      String input = getInput(data);
      // use input as output if output is null
      String output = this._output != null ? this._output : input;
      Map<String, String> parameters = this.parameters();
      // step info
      StepInfoImpl info = new StepInfoImpl(this._id, this._name, input, output, parameters);
      // process the step
      result = this._step.process(this._model, data, info);
      // process the callback step
      if (this._callbackStep != null) {
        try {
          // put the step result to a callback step
          this._callbackStep.process(this._model, data, result, info);
        } catch (Exception ex) {
          // if an error occurs, show the warning but doesn't affect the actual step.
          LOGGER.warn("Execute callback step error.", ex);
        }
      }
    } catch (Exception ex) {
      LOGGER.error("Step execution error.", ex);
      InvalidResult invalidResult = new InvalidResult(this._model, data);
      invalidResult.error(ex);
      invalidResult.setStatus(ResultStatus.ERROR);
      result = invalidResult;
    }

    return result;
  }

  /**
   * Return the percentage of the process.
   *
   * @return the pecerntage in %.
   */
  public int percentage () {
    int percentage = -1;
    if (this._step != null && this._step instanceof Measurable){
      percentage = ((Measurable)this._step).percentage();
    }
    return percentage;
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

    xml.attribute("async", String.valueOf(this.async()));

    xml.attribute("viewable", String.valueOf(this.viewable()));

    xml.attribute("fail-on-error", String.valueOf(this.failOnError()));

    xml.attribute("downloadable", String.valueOf(this.downloadable()));

    if (this._step != null) {
      xml.attribute("class", this._step.getClass().getName());
    }

    if (this._extraAttributes != null) {
      for (Entry<String, String> extraAttribute: this._extraAttributes.entrySet()) {
        xml.attribute(extraAttribute.getKey(), extraAttribute.getValue());
      }
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
    StepDefinition nextStep = next();
    if (nextStep != null) {
      xml.attribute("next-id", nextStep.id());
    }
    if (this._parameters != null) {
      for (String name : this._parameters.keySet()) {
        xml.openElement("parameter");
        xml.attribute("name", name);
        xml.attribute("value", this._parameters.get(name));
        xml.closeElement();// parameter
      }
    }

    if (this._extraElements != null) {
      for (GenericInfo element : this._extraElements) {
        element.toXML(xml);
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

    private boolean async = false;
    private boolean viewable = false;
    private boolean downloadable = false;
    private boolean failOnError = true;

    private Map<String, String> parameters = new HashMap<>();

    /** If there are any other attributes that are not expected. */
    private Map<String, String> extraAttributes = new HashMap<>();

    /** extra element inside the step definition. */
    private List<GenericInfo> extraElements = new ArrayList<>();

    /** TODO it is not in use any more. */
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
     *
     * @param async the asyns
     * return the {@link Builder}
     */
    public Builder setAsync(boolean async) {
      this.async = async;
      return this;
    }

    /**
     *
     * @param fail the fail on error flag
     * return the {@link Builder}
     */
    public Builder setFailOnerror(boolean fail) {
      this.failOnError = fail;
      return this;
    }

    /**
    *
    * @param viewable the viewable
    * return the {@link Builder}
    */
   public Builder setViewable(boolean viewable) {
     this.viewable = viewable;
     return this;
   }

   /**
   *
   * @param downloadable the downloadable
   * return the {@link Builder}
   */
   public Builder setDownloadable(boolean downloadable) {
    this.downloadable = downloadable;
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
     * Adds the extra attributes.
     *
     * @param name the name
     * @param value the value
     */
    public Builder addExtraAttributes (String name, String value) {
      if (StringUtils.isBlank(name)) throw new IllegalArgumentException("The attribute cannot have empty name.");
      if (this.extraAttributes.containsKey(name)) {
        //Check the uniqueness of the step
        throw new IllegalArgumentException("The attribute " + name + " already exist in this step " + this.name);
      }
      this.extraAttributes.put(name, value==null ? "" : value);
      return this;
    }

    /**
     * Adds the extra attributes.
     *
     * @param extraElement the extra element
     * @return the builder
     */
    public Builder addExtraElements (GenericInfo extraElement) {
      if (extraElement != null) this.extraElements.add(extraElement);
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
      this.pipeline = null;
      this.classname = null;
      this.callbackClassname = null;
      this.id = null;
      this.name = null;
      this.async = false;
      this.viewable = false;
      this.downloadable = false;
      this.failOnError = true;
      this.parameters = new HashMap<>();
      this.extraAttributes = new HashMap<>();
      this.extraElements = new ArrayList<>();
      this.output = null;
    }

    /**
     * Build the step from the arguments of this class;
     *
     * @return the StepDefinition.
     */
    @SuppressWarnings("unchecked")
    public StepDefinition build() throws OXException {
      StepDefinition definition;
      try {
        Step step;

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
          if (!StringUtils.isBlank(this.callbackClassname)) {
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
          definition = new StepDefinition(this._model, this.pipeline, this.id, this.name, this.parameters, this.output,
              this.async, this.viewable, this.downloadable, this.failOnError, step, this.extraAttributes, this.extraElements, callback);
        } else {
          definition = new StepDefinition(this._model, this.pipeline, this.id, this.name, this.parameters, this.output,
              this.async, this.viewable, this.downloadable, this.failOnError, step, this.extraAttributes, this.extraElements);
        }
      } catch (Exception ex) {
        throw new OXException("Unable to build step.", ex);
      }
      return definition;
    }
  }
}
