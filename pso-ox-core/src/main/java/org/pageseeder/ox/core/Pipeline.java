/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */
package org.pageseeder.ox.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pageseeder.ox.OXException;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Defines an pipeline (a concept borrowed from xproc)
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  16 December 2013
 */
public final class Pipeline implements XMLWritable, Serializable {

  private static final long serialVersionUID = 3462658965942218380L;

  /**
   * The name of pipeline
   */
  private final String _name;

  /**
   * The description of pipeline
   */
  private final String _description;

  /**
   * The media type that this pipeline accepts.
   */
  private final String _accepts;

  /**
   * Steps in this pipeline in sequence.
   */
  private final List<StepDefinition> _steps = new ArrayList<StepDefinition>();

  /**
   * @param name The name of pipeline
   * @param accepts the accept mine-type
   */
  public Pipeline(String name, String accepts) {
    this._name = name;
    this._accepts = accepts;
    this._description = name;
  }

  /**
   * @param name The name of pipeline
   * @param accepts the accept mine-type
   * @param description the description of pipeline
   */
  public Pipeline(String name, String accepts, String description) {
    this._name = name;
    this._accepts = accepts;
    this._description = description;
  }

  /**
   * @return The name of this pipeline.
   */
  public String name() {
    return this._name;
  }

  /**
   * @return The media type that this pipeline accepts.
   */
  public String accepts() {
    return this._accepts;
  }

  /**
   * @return the description of this pipeline.
   */
  public String description() {
    return this._description;
  }

  /**
   * Returns the step with the specified id.
   *
   * @return the corresponding step or <code>null</code>.
   */
  public StepDefinition getStep(String id) {
    for (StepDefinition s : this._steps) {
      if (s.id().equals(id)) { return s; }
    }
    return null;
  }

  /**
   * @return Returns the step for the specified index.
   */
  public StepDefinition getStep(int index) {
    return this._steps.get(index);
  }

  /**
   * @return the number of steps in the pipeline.
   */
  public int size() {
    return this._steps.size();
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("pipeline", true);
    xml.attribute("name", this._name);
    xml.attribute("description", this._description);
    xml.attribute("accepts", this._accepts);
    if (this._steps != null) {
      for (StepDefinition s : this._steps) {
        s.toXML(xml);
      }
    }
    xml.closeElement(); // pipeline
  }

  static class PipelineHandler extends DefaultHandler implements ContentHandler {

    private Pipeline pipeline = null;

    private final StepDefinition.Builder builder;

    private boolean inStep = false;

    private String stepId = null;

    /**
     *
     */
    public PipelineHandler(Model model) {
      this.builder = new StepDefinition.Builder(model);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

      // pipeline (@name, @accept, step)
      if (localName.equals("pipeline")) {
        String name = attributes.getValue("name");
        String accepts = attributes.getValue("accepts");
        String description = attributes.getValue("description");
        if (accepts == null) {
          accepts = "application/xml";
        }
        this.pipeline = new Pipeline(name, accepts, description);
        this.builder.setPipeline(this.pipeline);
      }

      // step (@id, @name, @async, @class, @callback)
      else if (localName.equals("step")) {
        this.stepId = attributes.getValue("id");
        String classname = attributes.getValue("class");
        String callback = attributes.getValue("callback");
        String name = attributes.getValue("name");

        this.builder.setStepId(this.stepId);
        this.builder.setStepName(name != null ? name : this.stepId);
        this.builder.setAsync("true".equalsIgnoreCase(attributes.getValue("async")) ? true : false );
        this.builder.setStepClass(classname);
        this.builder.setCallback(callback);
        this.inStep = true;
      }

      // output (@file, @folder)
      else if (this.inStep && localName.equals("output")) {
        // XXX: a little but dodgy...
        String output = attributes.getValue("file");
        if (output == null) {
          output = attributes.getValue("folder");
        }
        this.builder.setOutput(output);
      }

      // step parameter (@name, @value)
      else if (this.inStep && localName.equals("parameter")) {
        String name = attributes.getValue("name");
        String value = attributes.getValue("value");
        this.builder.addParameter(name, value);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (localName.equals("step")) {
        try {
          StepDefinition step = this.builder.build();
          this.pipeline._steps.add(step); // TODO check the uniqueness of step
          this.builder.reset();
          this.inStep = false;
        } catch (OXException ex) {
          // TODO: Or we could send a warning and ignore the step
          throw new SAXException(ex);
        }
      }
    }

    /**
     * @return the pipeline
     */
    public Pipeline getPipeline() {
      return this.pipeline;
    }

  }
}
