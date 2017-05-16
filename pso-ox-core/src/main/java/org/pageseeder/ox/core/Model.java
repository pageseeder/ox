/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.core;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXEntityResolver;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.core.Pipeline.PipelineHandler;
import org.pageseeder.ox.util.XSLT;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Christophe Lauret
 * @since  31 October 2013
 */
public final class Model implements XMLWritable {

  private final static Logger LOGGER = LoggerFactory.getLogger(Model.class);

  /**
   * Name of the model.
   */
  private final String _name;

  /**
   * The list of pipelines defined for this model.
   */
  private final List<Pipeline> _pipelines = new ArrayList<Pipeline>();

  /**
   * The status to indicate whether the config file has been loaded
   */
  private boolean hasLoaded = false;

  /**
   * Create a new model.
   * @param name the name of model.
   */
  public Model(String name) {
    this._name = name;
  }

  /**
   * @return the name of this model
   */
  public String name() {
    return this._name;
  }

  /**
   * Add the pipeline to the list and check if it is unique.
   * 
   * @param pipeline
   */
  private void addPipeline (Pipeline pipeline) {
    for (Pipeline s : this._pipelines) {
      //Check the uniqueness of the step
      if (s.id().equals(pipeline.id())) { 
        throw new IllegalArgumentException("The model " + this.name() + "already has the pipeline " + pipeline.id());
      }
    }
    this._pipelines.add(pipeline);
  }
  
  /**
   * Loads the model definition to know what the pipelines, steps and parameters are.
   * @return the status of load.
   */
  public boolean load() {
    boolean ok = false;
    File definition = getFile("model.xml");
    if (definition != null && definition.exists() && !this.hasLoaded) {
      try {
        parse(this, definition);
        ok = true;
      } catch (OXException ex) {
        LOGGER.warn("Cannot load configuration file", ex);
        ok = false;
      } catch (IOException ex) {
        LOGGER.warn("Cannot load configuration file", ex);
        ok = false;
      }
      this.hasLoaded = true;
    }
    return ok;
  }

  /**
   * To reload the model definition. (reload pipelines, step, etc)
   *
   * @return the status of reload
   */
  public boolean reload() {
    this.hasLoaded = false;
    return load();
  }

  /**
   * Returns a file given a path relative to this model.
   *
   * @param path the relative path a file within the model
   * @return The corresponding file.
   */
  public File getFile(String path) {
    File dir = new File(getModelsDirectory(), this._name);
    if (path == null) {
      return null;
    } else {
      return new File(dir, path);
    }
  }

  /**
   * @return the model root.
   */
  public File getRoot() {
    return new File(getModelsDirectory(), this._name);
  }

  /**
   * Load a properties file from the package.
   *
   * @param path the path to the properties file.
   *
   * @return the properties loaded from the model or <code>null</code>.
   */
  public Properties getProperties(String path) {
    File file = getFile(path);
    if (file == null) { return null; }
    Properties p = new Properties();
    try (FileReader reader = new FileReader(file)) {
      p.load(reader);
      LOGGER.debug("Using properties '{}' defined by model {}", path, this._name);
    } catch (IOException ex) {
      LOGGER.warn("Unable to use properties '{}' defined by model {}", path, this._name);
    }
    return p;
  }

  /**
   * Returns templates from this model, falling back on the built-in templates.
   *
   * @param path the path to the template
   *
   * @return the templates loaded from the model or <code>null</code>.
   * @throws IOException when IO error occur.
   * @throws TransformerConfigurationException when transformation occur.
   */
  public Templates getTemplates(String path) throws IOException, TransformerConfigurationException {
    Templates templates = null;
    // Get the templates
    File xslt = getFile(path);
    if (xslt.exists()) {
      templates = XSLT.getTemplates(xslt);
    } else {
      templates = XSLT.getTemplatesFromResource("org/pageseeder/ox/builtin/" + path);
    }
    return templates;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("model", true);
    xml.attribute("name", this._name);
    for (Pipeline p : this._pipelines) {
      p.toXML(xml);
    }
    xml.closeElement();
  }

  // Static methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Indicates whether the model exists.
   * @param name the name of model
   * @return the status whether is defined
   */
  public static boolean isDefined(String name) {
    File dir = new File(getModelsDirectory(), name);
    return dir.exists();
  }

  /**
   * Returns the directory containing all the models.
   *
   * @return the directory containing all the models.
   */
  private static File getModelsDirectory() {
    return OXConfig.get().getModelsDirectory();
  }

  /**
   * Returns the default model (if only one defined)
   * 
   * Note: Before use this Method, check if the OXCOnfig model directory is defined.  
   */
  public static Model getDefault() {
    List<Model> models = listModels();
    return models == null || models.isEmpty() ? null : models.get(0);

  }

  /**
   * @return the list of {@link Model}s
   */
  public static List<Model> listModels() {
    return listModels(false);
  }

  /**
   * @param reload whether to reload the models
   * @return the list of {@link Model}s
   */
  public static List<Model> listModels(boolean reload) {
    List<Model> models = new ArrayList<Model>();

    System.out.println("Model Directory: " + getModelsDirectory().getAbsoluteFile());
    String[] names = getModelsDirectory().list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return dir != null && dir.isDirectory();
      }
    });

    if (names != null) {
      for (String name : names) {
        Model model = new Model(name);
        models.add(model);
        if (reload) {
          model.reload();
        }
      }
    }
    return models;
  }

  // XML Parsing
  // ----------------------------------------------------------------------------------------------

  /**
   * Parses the model
   *
   * @param model
   * @param definition
   * @throws OXException
   * @throws IOException
   */
  private static void parse(Model model, File definition) throws OXException, IOException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      SAXParser parser = factory.newSAXParser();

      XMLReader reader = parser.getXMLReader();
      ModelHandler handler = new ModelHandler(model);
      reader.setContentHandler(handler);
      reader.setEntityResolver(OXEntityResolver.getInstance());
      // parser.parse(definition, handler);
      reader.parse(new InputSource(definition.toURI().toString()));
    } catch (ParserConfigurationException ex) {
      throw new OXException("Unable to parse model definition", ex);
    } catch (SAXException ex) {
      throw new OXException("Unable to parse model definition", ex);
    }
  }

  /**
   * @param id the id of the pipeline
   * @return the Pipeline based on the name.
   */
  public Pipeline getPipeline(String id) {
    load();
    for (Pipeline p : this._pipelines) {
      if (p.id().equals(id)) { return p; }
    }
    return null;
  }

  /**
   * Return the default pipeline for this model.
   * 
   * @return the first Pipeline found which default attribute has value true. If
   * not found then returns the first.
   */
  public Pipeline getPipelineDefault() {
    load();
    Pipeline defaultPipeline = null;
    for (Pipeline p : this._pipelines) {
      if (p.isDefault()) {
        defaultPipeline = p;
        break;
      }
    }
    if (defaultPipeline == null && !this._pipelines.isEmpty()) {
      defaultPipeline = this._pipelines.get(0);
    }
    return defaultPipeline;
  }

  /**
   * @return the number of pipeline in the model.
   */
  public int size() {
    load();
    return this._pipelines.size();
  }

  /**
   * SAX parser for a model.
   *
   * This parser delegates the task of parsing the pipeline.
   */
  static class ModelHandler extends DefaultHandler implements ContentHandler {

    /**
     * The model being parsed.
     */
    private Model _model;

    private PipelineHandler handler = null;

    private Locator _locator = null;

    /**
     *
     */
    public ModelHandler(Model model) {
      this._model = model;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      this._locator = locator;
      super.setDocumentLocator(locator);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (this.handler != null) {
        this.handler.startElement(uri, localName, qName, attributes);
      } else {

        if (localName.equals("model")) {
          String name = attributes.getValue("name");

          if (this._model == null) {
            this._model = new Model(name);
          } else {
            if (name != null && !name.equals(this._model.name())) {
              LOGGER.warn("The name of this model '{}' does not match '{}' (line {})", name, this._model.name(), this._locator.getLineNumber());
            }
          }

        } else if (localName.equals("pipeline")) {
          // A new pipeline
          this.handler = new PipelineHandler(this._model);
          this.handler.startElement(uri, localName, qName, attributes);
        }

      }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (this.handler != null) {
        this.handler.endElement(uri, localName, qName);
        if (localName.equals("pipeline")) {
          Pipeline pipeline = this.handler.getPipeline();
          if (pipeline != null) {
            this._model.addPipeline(pipeline);
          }
          this.handler = null;
        }
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.handler != null) {
        this.handler.characters(ch, start, length);
      }
    }

    /**
     * @return the _model
     */
    public Model getModel() {
      return this._model;
    }

  }

}
