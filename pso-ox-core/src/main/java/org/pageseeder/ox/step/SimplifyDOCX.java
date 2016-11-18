/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.step;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.XSLT;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A step for simplified a Docx document. </p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li>No parameter is required.</li>
 * </ul>
 *
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  8 May 2014
 */
public final class SimplifyDOCX implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(SimplifyDOCX.class);

  /**
   * Name of the properties file for the DOCX simplifier
   */
  private static final String SETTINGS = "simplify-docx.properties";

  /**
   * Name of the main document part in Word ML.
   */
  private static final String MAIN_PART = "word/document.xml";

  /**
   * The resource path to the builtin templates.
   */
  private static final String BUILTIN_TEMPLATES = "org/pageseeder/ox/docx/builtin/simplify-docx.xsl";

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    // unpack data
    boolean hasPacked = unpack(data);

    if (hasPacked) {
      return process(model, data, "unpacked", "simplified", computeSettings(model, data.getParameters()));
    } else {
      InvalidResult invalid = new InvalidResult(model, data);
      invalid.setError(new IllegalStateException("The specified docx file cannot unpack."));
      return invalid;
    }
  }

  private static boolean unpack(PackageData data) {
    // make sure unpack the docx
    boolean hasPacked = false;
    try {
      boolean unpacked = data.isUnpacked();
      if (!unpacked) {
        data.unpack();
      }
      hasPacked = true;
    } catch (IOException ex) {
      hasPacked = false;
      LOGGER.warn("Cannot unpack doc", ex);
    }
    return hasPacked;
  }

  private SimplifierResult process(Model model, PackageData data, String sourceDir, String targetDir, Map<String, String> settings) {
    SimplifierResult result = new SimplifierResult(model, data, sourceDir, targetDir, settings);

    File source = data.getFile(sourceDir);
    File target = data.getFile(targetDir);

    try {
      // This gives us the WordProcessingML we need
      File original = new File(source, MAIN_PART);
      if (!original.exists()) { throw new FileNotFoundException(source.getName() + "/" + MAIN_PART); }

      // Copy directory structure
      FileUtils.copy(source, target);

      // Resulting XML
      File simplified = new File(target, MAIN_PART);
      simplified.delete();

      // Run XSLT
      Templates templates = XSLT.getTemplatesFromResource(BUILTIN_TEMPLATES);
      Transformer transformer = templates.newTransformer();
      for (Entry<String, String> s : settings.entrySet()) {
        transformer.setParameter(s.getKey(), s.getValue());
      }
      transformer.transform(new StreamSource(original), new StreamResult(simplified));

      // Package up the DOCX document
      File sub = data.getDownloadDir(data.getFile("download"));
      String name = data.getProperty("name", data.id());
      File downloadable = new File(sub, name + "-simplified.docx");

      ZipUtils.zip(data.getFile("simplified"), downloadable);
      result.downloadFile = downloadable;

      // Stop the timer
      result.done();

    } catch (IOException | TransformerException ex) {
      result.setError(ex);
    }

    return result;
  }

  /**
   * Set all the known settings for the simplifier from the parameters in the content request as
   * parameters for the transformer
   *
   * @param transformer The transformer.
   * @param parameters  The parameters from model
   */
  private static Map<String, String> computeSettings(Model model, Map<String, String> parameters) {
    Map<String, String> settings = new HashMap<String, String>();
    // Load values from the model
    Properties p = getModelSettings(model);
    for (Entry<Object, Object> e : p.entrySet()) {
      String name = e.getKey().toString();
      String value = e.getValue().toString();
      settings.put(name, value);
    }
    // Load values from the model
    if (parameters != null) {
      for (String setting : SIMPLIFIER_SETTINGS) {
        String value = parameters.get(setting);
        if (value != null) {
          settings.put(setting, value);
        }
      }
    }
    return settings;
  }

  /**
   * Load the settings defined by the model.
   *
   * @param model
   * @return the properties
   */
  private static Properties getModelSettings(Model model) {
    // Try to load config from model
    Properties p = new Properties();
    File config = model.getFile(SETTINGS);
    if (config.exists()) {
      try (FileReader reader = new FileReader(config)) {
        p.load(reader);
        LOGGER.warn("Using simplifier properties defined by model {}", model.name());
      } catch (IOException ex) {
        LOGGER.warn("Unable to use simplifier properties defined by model {}", model.name());
      }
    }
    return p;
  }

  private final class SimplifierResult extends ResultBase implements Result, Downloadable {

    private final String _source;

    private final String _target;

    private final Map<String, String> _settings;

    private File downloadFile = null;

    private SimplifierResult(Model model, PackageData data, String source, String target, Map<String, String> settings) {
      super(model, data);
      this._source = source;
      this._target = target;
      this._settings = settings;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result", true);
      xml.attribute("type", "simplify-docx");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));
      xml.attribute("path", data().getPath(downloadPath()));

      // Source document
      xml.openElement("source");
      xml.attribute("path", this._source + "/" + MAIN_PART);
      xml.closeElement();

      // Target document
      xml.openElement("target");
      xml.attribute("path", this._target + "/" + MAIN_PART);
      xml.closeElement();

      // Settings specified
      xml.openElement("settings", true);
      if (this._settings != null) {
        for (Entry<String, String> p : this._settings.entrySet()) {
          xml.openElement("setting");
          xml.attribute("name", p.getKey());
          xml.attribute("value", p.getValue());
          xml.closeElement();
        }
      }
      xml.closeElement();

      // Return SVRL data if available
      if (this.downloadFile != null) {
        xml.openElement("download");
        xml.attribute("href", "/" + this.downloadFile.getName());
        xml.closeElement();
      }

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      }

      xml.closeElement();
    }

    @Override
    public File downloadPath() {
      return this.downloadFile;
    }

    @Override
    public boolean isDownloadable() {
      return true;
    }

  }

  public final static List<String> SIMPLIFIER_SETTINGS = Collections.unmodifiableList(Arrays.asList("remove-custom-xml",
  //
  "remove-smart-tags",
  //
  "remove-content-controls",
  //
  "remove-rsid-info",
  //
  "remove-permissions",
  //
  "remove-proof",
  //
  "remove-soft-hyphens",
  //
  "remove-last-rendered-page-break",
  //
  "remove-bookmarks",
  //
  "remove-goback-bookmarks",
  //
  "remove-web-hidden",
  //
  "remove-language-info",
  //
  "remove-comments",
  //
  "remove-end-and-foot-notes",
  //
  "remove-field-codes",
  //
  "replace-nobreak-hyphens",
  //
  "replace-tabs",
  //
  "remove-font-info",
  //
  "remove-paragraph-properties"));
}
