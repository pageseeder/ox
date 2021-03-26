/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */
package org.pageseeder.ox.schematron.tool;

import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.tool.Command;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.BasicCache;
import org.pageseeder.schematron.SchematronException;
import org.pageseeder.schematron.SchematronResult;
import org.pageseeder.schematron.Validator;
import org.pageseeder.schematron.ValidatorFactory;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Runs schematron on the specified document.
 *
 * @author Christophe Lauret
 * @version 28 October 2013
 */
public class SchematronCommand implements Command<Result> {

  /** Logger */
  private final static Logger LOGGER = LoggerFactory.getLogger(SchematronCommand.class);

  /**
   * Maps XSLT templates to their URL as a string for easy retrieval.
   */
  private static final BasicCache<Validator> CACHE = new BasicCache<>();

  /**
   * The name of the default schematron to use for this.
   */
  public static final String DEFAULT_SCHEMA = "default.sch";

  /**
   * The resource path to the builtin schematron.
   */
  private static final String BUILTIN_SCHEMA = "org/pageseeder/ox/schematron/builtin/" + DEFAULT_SCHEMA;

  /**
   * The model used by this command.
   */
  private final Model _model;

  /**
   * Path to the document to process.
   */
  private String path = null;

  /**
   * Path to the schema to use within the model
   */
  private String schema = DEFAULT_SCHEMA;

  /**
   * Create a new command.
   *
   * @param model The model to use
   */
  public SchematronCommand(Model model) {
    this._model = model;
  }

  /**
   * @param path The path to the document to validate.
   */
  public void setDocumentPath(String path) {
    this.path = path;
  }

  /**
   * @param schema The path to the schema to use within the model.
   */
  public void setSchema(String schema) {
    this.schema = schema;
  }

  @Override
  public Model getModel() {
    return this._model;
  }

  @Override
  public Result process(PackageData data) {

    SchemaResult result = null;

    // File to validate with schematron
    File document = data.getFile(this.path);

    // Run Schematron on WordProcessingML
    try {
      Validator validator = getValidator(this.schema);
      SchematronResult r = validator.validate(document);

      result = new SchemaResult(this._model, data, this.path, r);
      result.done();

    } catch (SchematronException ex) {
      LOGGER.error("Unable to run schematron schema: {}", ex.getMessage(), ex);
      result = new SchemaResult(this._model, data, this.path);
      result.setError(ex);
    }

    return result;
  }

  /**
   * Returns the Schematron validator used.
   *
   * <p>This method will return the schematron in the model if one is found;
   * otherwise it will revert to using the default built-in schematron.
   *
   * @return the Schematron validator used.
   *
   * @throws SchematronException If the schematron could not be parsed as a validator.
   */
  public Validator getValidator(String schematron) throws SchematronException {
    File schema = this._model.getFile(schematron);
    LOGGER.debug("Checking if schematron schema exists: {} -> {}", schematron, schema.exists());
    if (schema != null && schema.exists()) return getModelValidator(schema);
    else return getBuiltinValidator();
  }

  /**
   * @param schema
   * @return {@link Validator}
   * @throws SchematronException
   */
  public Validator getValidator(File schema) throws SchematronException {
    LOGGER.debug("Checking if schematron schema exists: {} -> {}", schema, schema.exists());
    return getModelValidator(schema);
  }

  /**
   * Return the model schema
   *
   * @param schema the file use read as a schema.
   * @return the corresponding validator.
   *
   * @throws SchematronException If the schematron could not be parsed as a validator.
   */
  private static Validator getModelValidator(File schema) throws SchematronException {
    String url = schema.toURI().toString();
    Validator validator = CACHE.get(schema.toURI().toString(), schema.lastModified());
    if (validator == null) {
      LOGGER.debug("Using schematron schema: {}", schema.getName());
      ValidatorFactory factory = new ValidatorFactory();
      validator = factory.newValidator(schema);
      CACHE.put(url, validator);
    }
    return validator;
  }

  /**
   * Returns the built-in validator
   *
   * @return The built-in validator.
   *
   * @throws SchematronException If the schematron could not be parsed as a validator.
   */
  public static Validator getBuiltinValidator() throws SchematronException {
    URL url = SchematronCommand.class.getResource("/" + BUILTIN_SCHEMA);
    Validator validator = CACHE.get(url.toString(), 0L);
    if (validator == null) {
      try (InputStream in = url.openStream()) {
        Source source = new StreamSource(in);
        LOGGER.debug("Using builtin schema: {}", BUILTIN_SCHEMA);
        ValidatorFactory factory = new ValidatorFactory();
        validator = factory.newValidator(source);
        CACHE.put(url.toString(), validator);
      } catch (IOException ex) {
        LOGGER.error("Cannot load the built in validator.", ex);
      }
    }
    return validator;
  }

  private static class SchemaResult extends ResultBase implements Result {

    /** The results of the schematron validation reported by schematron */
    private final SchematronResult _result;

    /** the path */
    private final String _path;

    public SchemaResult(Model model, PackageData data, String path) {
      this(model, data, path, null);
    }

    /** */
    public SchemaResult(Model model, PackageData data, String path, SchematronResult result) {
      super(model, data);
      this._path = path;
      this._result = result;
      if (result != null && !result.isValid()) {
        setStatus(ResultStatus.WARNING);
      }
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("type", "schematron");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      xml.openElement("document");
      xml.attribute("path", this._path);
      xml.closeElement();

      // TODO: specify schema used

      // Return SVRL data if available
      if (this._result != null) {
        xml.writeXML(this._result.getSVRLAsString());
      }

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      }

      xml.closeElement();
    }

    /* (non-Javadoc)
     * @see org.pageseeder.ox.tool.ResultBase#isDownloadable()
     */
    @Override
    public boolean isDownloadable() {
      return false;
    }

  }

}
