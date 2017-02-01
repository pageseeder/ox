/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.step;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Downloadable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.XSLT;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A step to transform the specified input by using provided stylesheet.</p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>input</var> the xml file needs to be transformed, where is a relative path of package data.
 *  (if not specified, use upper step output as input.)</li>
 *  <li><var>output</var> the output file, where is a relative path of package data (optional)</li>
 *  <li><var>output-folder</var> the output directory, where is a relative path of package data (optional)</li>
 *  <li><var>xsl</var> the stylesheet file to transform, which is a relative path of model folder.
 *  (This parameter will override the property <var>parameter-xsl</var>)</li>
 *  <li><var>display-result</var> whether to display the result xml into Result XML (default: true)</li>
 *  <li><var>_xslt-</var>Every parameter with the preffix "_xslt-" will be send to the xslt (without the preffix "_xslt-").</li>
 * </ul>
 *
 * <h3>Data Properties</h3>
 * <ul>
 *  <li><var>xsl</var> the stylesheet filename, which is a relative path to model folder. (required).
 *   It will be overridden by the parameter <var>xsl</var> if that has set as well.
 *  </li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>If <var>xsl</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>Otherwise return {@link TransformResult}
 *
 *
 * @author Ciber Cai
 * @since  17 June 2014
 */
public final class Transformation implements Step {

  private static final Logger LOGGER = LoggerFactory.getLogger(Transformation.class);

  private static final String OUTPUT_FOLDER = "transformation";

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    // input
    String xml = info.getParameter("input", info.input());
    File source = data.getFile(xml);

    // throw the error
    if (source == null || !source.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the source file " + xml + ".")); }

    // the xsl folder
    File xslRoot = model.getRoot();
    // the transformation file
    String xsl = info.getParameter("xsl", data.getProperty("parameter-xsl"));
    File xslFile = new File(xslRoot, xsl);

    // throw the error
    if (xslFile == null || !xslFile.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the stylesheet file " + xsl + ".")); }

    // output file
    String output = null;
    if (info.getParameter("output") != null) {
      output = info.getParameter("output");
    } else if (info.output() != null) {
      output = info.output();
    } else {
      output = OUTPUT_FOLDER + "/" + data.id() + System.nanoTime() + ".xml";
    }
    File target = data.getFile(output);

    // display result
    boolean displayResult = "false".equals(info.getParameter("display-result")) ? false : true;

    // transform the result
    TransformResult result = new TransformResult(model, data, xml, output, xsl);
    try {
      Templates templates = null;
      Transformer transformer = null;
      URIResolver resolver = new CustomURIResolver(xslRoot);
      templates = XSLT.getTemplates(xslFile);
      transformer = templates.newTransformer();
      transformer.setURIResolver(resolver);

      // Add the parameters from post request
      for (Entry<String, String> p : data.getParameters().entrySet()) {
        transformer.setParameter(p.getKey(), p.getValue());
      }
      
      // Add the parameters from step definition in model.xml
      // these parameters should use the prefix _xslt-
      for (Entry<String, String> p :info.parameters().entrySet()) {
        if (p.getKey().startsWith("_xslt-")) {
          transformer.setParameter(p.getKey().replaceAll("_xslt-", ""), p.getValue());
        }
      }
            
      transformer.transform(new StreamSource(source), new StreamResult(target));

      // whether to display the transformation result
      if (displayResult && target != null && target.exists()) {
        result.output = new String(Files.readAllBytes(target.toPath()), "UTF-8");
      }
      result.done();

    } catch (TransformerException | IOException ex) {
      LOGGER.error("Transform configuration exception: {}", ex.getMessage(), ex);
      result.setError(ex);
    }

    return result;
  }

  /**
   * A custom URI resolver to get the stylesheet file.
   * @author Ciber Cai
   * @since  17 June 2014
   */
  private static class CustomURIResolver implements URIResolver {

    /** the root of stylesheet **/
    private final File _root;

    /**
     * @param r the folder of the stylesheet
     */
    public CustomURIResolver(File r) {
      this._root = r;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      File xsl = new File(this._root, href);
      Source source = null;
      try {
        if (xsl != null && xsl.exists()) {
          source = new StreamSource(new FileInputStream(xsl));
        }
      } catch (FileNotFoundException e) {
        LOGGER.warn("Cannot find the stylesheet file {}", href);
      }
      return source;
    }
  }

  /**
  *
  * @author Christophe Lauret
  * @author Ciber Cai
  */
  private class TransformResult extends ResultBase implements Result, Downloadable {

    private final String _source;

    private final String _target;

    private final String _template;

    private String output = null;

    /** */
    private TransformResult(Model model, PackageData data, String source, String target, String template) {
      super(model, data);
      this._source = source;
      this._target = target;
      this._template = template;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("type", "transform");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));
      xml.attribute("path", data().getPath(downloadPath()));
      if (this._target != null) {
        xml.attribute("output", this._target);
      }

      if (this._source != null) {
        xml.openElement("source");
        xml.attribute("path", this._source);
        xml.closeElement();
      }

      if (this._target != null) {
        xml.openElement("target");
        xml.attribute("path", this._target);
        xml.closeElement();
      }

      if (this._template != null) {
        xml.openElement("template");
        xml.attribute("path", this._target);
        xml.closeElement();
      }

      // Include the generated content
      if (this.output != null) {
        xml.openElement("content");
        xml.writeCDATA(this.output);
        xml.closeElement();
      }

      // The details of any error
      if (this.error() != null) {
        OXErrors.toXML(error(), xml, true);
      }
      xml.closeElement();
    }

    @Override
    public File downloadPath() {
      File outputFile = data().getFile(this._target);
      return outputFile;
    }

    @Override
    public boolean isDownloadable() {
      return true;
    }

  }
}
