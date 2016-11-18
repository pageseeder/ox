/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.step;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * <p>A step for validate the original file by schema. It can be internal (defined within XML file)
 * or can be external (specified in configuration file).  </p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>input</var> the xml file needs to be transformed, where is a relative path of package data.
 *  (if not specified, use upper step output as input.)</li>
 *  <li><var>schema</var> the schema file or folder, which is a relative path to model folder.</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns failed status of {@link SchemaResult }.</p>
 * <p>If <var>schema</var> does not exist, it returns failed status of {@link SchemaResult }.</p>
 * <p>Otherwise return {@link SchemaResult}
 *
 *
 * @author Ciber Cai
 * @since  12 June 2014
 */
public final class SchemaValidation implements Step {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidation.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    File xml = data.getFile(info.getParameter("input", info.input()));
    String schema = info.getParameter("schema");
    File[] schemaFiles = getSchemas(model, schema);

    if (schema == null || schemaFiles == null || schemaFiles.length < 1) {
      SchemaResult result = new SchemaResult(model, data);
      result.setError(new FileNotFoundException("Cannot find the schema file(s) " + schema + "."));
      return result;
    }

    LOGGER.debug("The validate file {} ", xml);
    // validate the xsd schema
    SchemaResult result = new SchemaResult(model, data, schemaFiles);
    if (xml != null && xml.exists()) {
      File schemaRoot = data.directory();
      result = validateXSDSchema(model, data, xml, new CustomSchemaResolver(schemaRoot), schemaFiles);
    } else {
      result.setError(new FileNotFoundException("Cannot find the xml file " + xml + "."));
    }
    result.done();
    return result;
  }

  /**
   * @param data the {@link PackageData}
   * @param schema the schema name
   * @return the list of schema files
   */
  private static File[] getSchemas(Model model, String schema) {
    if (schema == null) return null;
    File[] schemas = null;
    // TODO schema folder need to configurable
    File schemaLoc = model.getFile(schema);
    if (schemaLoc != null && schemaLoc.exists() && schemaLoc.isDirectory()) {
      schemas = schemaLoc.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(".xsd");
        }
      });
    } else if (schemaLoc != null && schemaLoc.exists() && schemaLoc.isFile() && schemaLoc.getName().endsWith(".xsd")) {
      schemas = new File[] { schemaLoc };
    }
    return schemas;
  }

  /**
   * @param model the Model to use
   * @param data the PackageData
   * @param xml  the xml file request to validate
   * @param schemaFiles the list of schema file.
   * @return the SchemaResult
   * @throws IOException when I/O error occur
   */
  private static SchemaResult validateXSDSchema(Model model, PackageData data, File xml, LSResourceResolver resolver, File... schemaFiles) {
    if (xml == null) { throw new NullPointerException("Cannot validate a null file"); }

    SchemaResult result = new SchemaResult(model, data, schemaFiles);
    StreamSource[] sources = null;
    int i = 0;
    if (schemaFiles != null) {
      sources = new StreamSource[schemaFiles.length];
      for (File f : schemaFiles) {
        sources[i] = new StreamSource(f);
        i++;
      }
    }

    StringWriter out = new StringWriter();
    try {
      SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
      // set resource resolver
      if (resolver != null) {
        factory.setResourceResolver(resolver);
      }
      Schema sch = (sources != null) ? factory.newSchema(sources) : factory.newSchema();
      javax.xml.validation.Validator validator = sch.newValidator();
      Source source = new StreamSource(xml);
      javax.xml.transform.Result validateResult = new StreamResult(out);
      validator.validate(source, validateResult);
      LOGGER.debug("Valid schema for {}", xml);
    } catch (SAXException | IOException ex) {
      result.setStatus(ResultStatus.ERROR);
      result.setError(ex);
    }
    return result;
  }

  /**
   * A Custom schema Resolver
   * @author Ciber Cai
   * @since  17 June 2014
   */
  private static class CustomSchemaResolver implements LSResourceResolver {

    /** The schema directory */
    private final File _schemaDir;

    /**
     * @param folder the folder to store the schema
     */
    private CustomSchemaResolver(File folder) {
      this._schemaDir = folder;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

      if (systemId == null) { return null; }
      LSInput input = new LSInputImpl();
      try {
        File schema = new File(this._schemaDir, systemId);
        if (schema != null && !schema.exists()) {
          FileInputStream is = new FileInputStream(schema);
          input.setByteStream(is);
          input.setPublicId(publicId);
          input.setSystemId(systemId);
          input.setBaseURI(baseURI);
          input.setCharacterStream(new InputStreamReader(is));
          is.close();
        } else {
          throw new FileNotFoundException("Cannot find the internal schema " + schema + ".");
        }
        return input;
      } catch (IOException ex) {
        LOGGER.warn("Cannot resolve schema.", ex);
        return null;
      }
    }
  }

  private static class LSInputImpl implements LSInput {
    private Reader characterStream;
    private InputStream byteStream;
    private String stringData;
    private String systemId;
    private String publicId;
    private String baseURI;
    private String encoding;
    private boolean certifiedText;

    @Override
    public Reader getCharacterStream() {
      return this.characterStream;
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
      this.characterStream = characterStream;
    }

    @Override
    public InputStream getByteStream() {
      return this.byteStream;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
      this.byteStream = byteStream;
    }

    @Override
    public String getStringData() {
      return this.stringData;
    }

    @Override
    public void setStringData(String stringData) {
      this.stringData = stringData;
    }

    @Override
    public String getSystemId() {
      return this.systemId;
    }

    @Override
    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

    @Override
    public String getPublicId() {
      return this.publicId;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
      return this.baseURI;
    }

    @Override
    public void setBaseURI(String baseURI) {
      this.baseURI = baseURI;
    }

    @Override
    public String getEncoding() {
      return this.encoding;
    }

    @Override
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
      this.certifiedText = certifiedText;
    }

    @Override
    public boolean getCertifiedText() {
      return this.certifiedText;
    }

  }

  /**
   * A class to represent the schema result.
   * @author Ciber Cai
   * @since  23 April 2014
   */
  private static class SchemaResult extends ResultBase implements Result {

    /** the list of schema files */
    private final File[] _schemaFiles;

    private SchemaResult(Model model, PackageData data) {
      super(model, data);
      this._schemaFiles = null;
    }

    private SchemaResult(Model model, PackageData data, File[] schemaFiles) {
      super(model, data);
      this._schemaFiles = schemaFiles;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("type", "Schema-Result");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      }

      // print the schema details
      xml.openElement("schemas");
      if (this._schemaFiles != null) {
        for (File schema : this._schemaFiles) {
          xml.openElement("schema");
          xml.attribute("name", schema.getName());
          xml.attribute("exist", schema.exists() ? "ok" : "fail");
          xml.closeElement();// schema
        }
      } else {
        xml.openElement("schema");
        xml.attribute("name", "internal");
        xml.closeElement();
      }
      xml.closeElement();// schemas

      xml.closeElement();
    }

    @Override
    public boolean isDownloadable() {
      return false;
    }
  }
}
