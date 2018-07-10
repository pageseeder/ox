/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */
package org.pageseeder.ox.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Defines an pipeline (a concept borrowed from xproc)
 *  
 * XML Structure:
 *  
 *  &gt;pipeline id="" name="" accepts="" description="" default="false" &lt;
 *    &gt;step&lt;
 *    &gt;/step&lt;
 *  &gt;/pipeline&lt;
 *
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  16 December 2013
 */
public final class Pipeline implements XMLWritable, Serializable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);
  
  private static final long serialVersionUID = 3462658965942218380L;

  /**
   * The unique id of pipeline, it will be uses the identify the pipeline. 
   */
  private final String _id;
  
  /**
   * The name of pipeline, it will be used to show on the screen (friendly text).
   */
  private final String _name;

  /**
   * The description of pipeline, it will be used to show as tooltips or another
   * way to help to user to understand goals of this pipeline.
   */
  private final String _description;

  /**
   * The media type that this pipeline accepts.
   */
  private final String _accepts;
  
  private final boolean _default;

  /**
   * Steps in this pipeline in sequence.
   */
  private final List<StepDefinition> _steps = new ArrayList<StepDefinition>();

  /** If there are any other attributes that are not expected. */
  private final Map<String, String> _extraAttributes = new HashMap<>();
  
  /** If there are any other elements that are not expected. */
  private final List<GenericInfo> _extraElements = new ArrayList<>();
  
  /**
   * Instantiates a new pipeline.
   *
   * @param id the id
   * @param name The name of pipeline
   * @param accepts the accept mine-type
   */
  public Pipeline(String id, String name, String accepts) {
    this(id, name, accepts, name, false);
  }

  /**
   * Instantiates a new pipeline.
   *
   * @param name The name of pipeline
   * @param accepts the accept mine-type
   * @param description the description of pipeline
   * @param defaultValue the default value
   */
  public Pipeline(String id, String name, String accepts, String description, boolean defaultValue) {
    if (StringUtils.isBlank(id)) throw new IllegalArgumentException("The pipeline id cannot be empty. Warning, before it was the attribute name.");
    if (StringUtils.isBlank(name)) throw new IllegalArgumentException("The pipeline name cannot be empty.");
    if (StringUtils.isBlank(accepts)) throw new IllegalArgumentException("The pipeline accepts cannot be empty.");
    this._id = id;
    this._name = name;
    this._accepts = accepts;
    this._description = description;
    this._default = defaultValue;
  }

  /**
   * @return The id of this pipeline.
   */
  public String id() {
    return this._id;
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
   * @return the description of this pipeline.
   */
  public boolean isDefault() {
    return this._default;
  }  

  /**
   * Adds the extra attributes.
   *
   * @param name the name
   * @param value the value
   */
  public void addExtraAttributes (String name, String value) {
    if (StringUtils.isBlank(name)) throw new IllegalArgumentException("The attribute cannot have empty name.");
    if (this._extraAttributes.containsKey(name)) {
      //Check the uniqueness of the step
      throw new IllegalArgumentException("The attribute " + name + " already exist in this pipeline " + this.name());
    }
    this._extraAttributes.put(name, value==null ? "" : value);
  }
  
  /**
   * Adds the extra attributes.
   *
   * @param extraAttributes the extra attributes
   */
  public void addExtraAttributes (Map<String, String> extraAttributes) {
    if (extraAttributes != null) {
      for (Entry<String, String> attribute: extraAttributes.entrySet()) {
        addExtraAttributes(attribute.getKey(), attribute.getValue());
      }
    }
  }  
  

  /**
   * Adds the extra attributes.
   *
   * @param name the name
   * @param value the value
   */
  public void addExtraElements (GenericInfo extraElement) {
    if (extraElement != null) this._extraElements.add(extraElement);
  }
  
  /**
   * Add the step to this pipeline and check the uniqueness
   * @param step
   */
  private void addStep (StepDefinition step) {
    for (StepDefinition s : this._steps) {
      //Check the uniqueness of the step
      if (s.id().equals(step.id())) { 
        throw new IllegalArgumentException("The pipeline " + this.id() + " already has the step " + step.id());
      }
    }
    this._steps.add(step);
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
    xml.attribute("id", this._id);
    xml.attribute("name", this._name);
    xml.attribute("description", this._description);
    xml.attribute("accepts", this._accepts);
    xml.attribute("default", String.valueOf(this._default));
    
    //Extra attributes
    for (Entry<String, String> attribute : this._extraAttributes.entrySet()) {
      xml.attribute(attribute.getKey(), attribute.getValue());
    }
    
    //Steps elements
    if (this._steps != null) {
      for (StepDefinition s : this._steps) {
        s.toXML(xml);
      }
    }
    
    //Extra Elements
    for (GenericInfo element:this._extraElements) {
      element.toXML(xml);
    }
    xml.closeElement(); // pipeline
  }

  static class PipelineHandler extends DefaultHandler implements ContentHandler {

    private Pipeline pipeline = null;

    private final StepDefinition.Builder builder;

    private boolean inStep = false;
    
    private GenericInfo extraElement;

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
        String id = "";
        String name = "";
        String accepts = "";
        String description = "";
        String defaultValue = "";
        Map<String, String> extraAttributes = new HashMap<>();
            
        for (int index=0; index < attributes.getLength(); index++) {
          String attributeName = attributes.getQName(index) != null ? attributes.getQName(index) : attributes.getLocalName(index);
          switch (attributeName) {
          case "id":
            id = attributes.getValue(index);
            break;
          case "name":
            name = attributes.getValue(index);
            break;
          case "accepts":
            accepts = attributes.getValue(index);
            break;
          case "description":
            description = attributes.getValue(index);
            break;
          case "default":
            defaultValue = attributes.getValue(index);
            break;
          default:
            extraAttributes.put(attributeName, attributes.getValue(index));            
          }
        }
        
        if (StringUtils.isBlank(id)) {
          LOGGER.warn("The id is empty, remember that this version is using the id to identify the pipeline instead of name.");
          id = name;
        }
        
        if (accepts == null) {
          accepts = "application/xml";
        }
        
        this.pipeline = new Pipeline(id, name, accepts, description, "true".equalsIgnoreCase(defaultValue));
        this.pipeline.addExtraAttributes(extraAttributes);
      }

      // step (@id, @name, @async, @class, @callback)
      else if (localName.equals("step")) {
        String classname = "";
        String callback = "";
        String name = "";     
        String downloadable = ""; 
        String viewable = "";
        String async = "";
        String failOnError = "";
        
        for (int index=0; index < attributes.getLength(); index++) {
          String attributeName = attributes.getQName(index) != null ? attributes.getQName(index) : attributes.getLocalName(index);
          String attributeValue = attributes.getValue(index);
          switch (attributeName) {
          case "id":
            this.stepId = attributeValue;
            break;
          case "name":
            name = attributeValue;
            break;
          case "class":
            classname = attributeValue;
            break;
          case "downloadable":
            downloadable = attributeValue;
            break;            
          case "viewable":
            viewable = attributeValue;
            break;
          case "fail-on-error":
            failOnError = attributeValue;
            break;
          case "callback":
            callback = attributeValue;
            break;
          case "async":
            async = attributeValue;
            break;
          default:
            this.builder.addExtraAttributes(attributeName, attributeValue);
          }
        }
        

        this.builder.setPipeline(this.pipeline);
        this.builder.setStepId(this.stepId);
        this.builder.setStepName(StringUtils.isBlank(name) ? this.stepId:name);
        this.builder.setAsync("true".equalsIgnoreCase(async));// default is false
        this.builder.setStepClass(classname);
        this.builder.setCallback(callback);
        this.builder.setDownloadable("true".equalsIgnoreCase(downloadable)); // default is false
        this.builder.setViewable("true".equalsIgnoreCase(viewable)); // default is false
        this.builder.setFailOnerror(!"false".equalsIgnoreCase(failOnError)); // default is true
        this.inStep = true;
      }

      // output (@file, @folder)
      else if (this.inStep && localName.equals("output")) {        
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
      // Is in step and unknown element.
      else if (this.inStep) {
        this.extraElement = new GenericInfo(localName);
        for (int index=0; index < attributes.getLength(); index++) {
          String attributeName = attributes.getQName(index) != null ? attributes.getQName(index) : attributes.getLocalName(index);
          extraElement.addAttributes(attributeName, attributes.getValue(index));
        }
      }
      // Is not in step and pipeline is not null and unknown element.
      else if (!this.inStep && this.pipeline != null) {
        this.extraElement = new GenericInfo(localName);
        for (int index=0; index < attributes.getLength(); index++) {
          String attributeName = attributes.getQName(index) != null ? attributes.getQName(index) : attributes.getLocalName(index);
          extraElement.addAttributes(attributeName, attributes.getValue(index));
        }
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (localName.equals("step")) {
        try {
          StepDefinition step = this.builder.build();
          this.pipeline.addStep(step);
          this.builder.reset();
          this.inStep = false;
        } catch (OXException ex) {
          // TODO: Or we could send a warning and ignore the step
          throw new SAXException(ex);
        }
      } else if (this.inStep && this.extraElement != null) {//Pipeline extra element
        this.builder.addExtraElements(extraElement);
        this.extraElement = null;
      } else if (!this.inStep && this.extraElement != null) {//Pipeline extra element
        this.pipeline.addExtraElements(extraElement);
        this.extraElement = null;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.extraElement != null) {
        this.extraElement.addText(new String (ch, start, length));
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
