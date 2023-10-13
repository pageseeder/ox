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
package org.pageseeder.ox.step;

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
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.XSLT;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * <p>A step for simplified a Docx document. </p>
 * <p>It can receive a DOCX or folder with unpacked DOCX. If it receives a DOCX, then it will unpack it (unpack means unzip).</p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *   <li><var>input</var> can be a docx file or a folder (the unpacked docx folder). If it is blank then this steps will
 *   the file uploaded.</li>
 *   <li><var>output</var> It should be a docx file. It it is a folder, it will create a docx inside this folder with
 *   the default value. If it is empty, then it will be create with the default value. The default value is the
 *   {package-id}-simplified.</li> *
 * </ul>
 *
 * <h3>Extra parameters that setups the simplifying process</h3>
 * <p>It is used in the XLST transformation of /word/document.xml. The value are  true or false.</p>
 * <p>The dynamic logic will no be applied and these parameter can defined in the step config or as input in the page.</p>
 * <ul>
 *   <li><var>remove-smart-tags</var></li>
 *   <li><var>remove-content-controls</var></li>
 *   <li><var>remove-rsid-info</var></li>
 *   <li><var>remove-permissions</var></li>
 *   <li><var>remove-proof</var></li>
 *   <li><var>remove-soft-hyphens</var></li>
 *   <li><var>remove-last-rendered-page-break</var></li>
 *   <li><var>remove-bookmarks</var></li>
 *   <li><var>remove-goback-bookmarks</var></li>
 *   <li><var>remove-web-hidden</var></li>
 *   <li><var>remove-language-info</var></li>
 *   <li><var>remove-comments</var></li>
 *   <li><var>remove-end-and-foot-notes</var></li>
 *   <li><var>remove-field-codes</var></li>
 *   <li><var>replace-nobreak-hyphens</var></li>
 *   <li><var>replace-tabs</var></li>
 *   <li><var>remove-font-info</var></li>
 *   <li><var>remove-paragraph-properties</var></li>
 * </ul>
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since 8 May 2014
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
    ResultBase result = null;
    try {
      File sourceDirectory = getInput(data, info);
      File output = getOutput(data, info);
      boolean sourceExist = sourceDirectory != null && sourceDirectory.exists();
      boolean outputExist = output != null;//We do not check if exists because it has not been created yet.

      //File output =
      if (sourceExist && outputExist) {
        return process(model, data, sourceDirectory, output, computeSettings(model, data.getParameters()));
      } else {
        InvalidResult invalid = new InvalidResult(model, data);
        StringBuilder builder = new StringBuilder();
        if (!sourceExist) {
          builder.append("Input file is invalid. ");
        }

        if (!outputExist) {
          builder.append("Output file is invalid.");
        }
        invalid.setError(new IllegalStateException(builder.toString()));
        result = invalid;
      }
    } catch (IOException ex) {
      result = new InvalidResult(model, data).error(ex);
    }
    return result;
  }

  private SimplifierResult process(Model model, PackageData data, File sourceDir, File output, Map<String, String> settings) {
    SimplifierResult result = new SimplifierResult(model, data, sourceDir, output, settings);

    //File source = data.getFile(sourceDir);
    File simplifiedDirectory = new File (output.getParentFile(), FileUtils.getNameWithoutExtension(output) + "-unpacked");

    try {
      // This gives us the WordProcessingML we need
      File original = new File(sourceDir, MAIN_PART);
      if (!original.exists()) {
        throw new FileNotFoundException(sourceDir.getName() + "/" + MAIN_PART);
      }

      // Copy original document unpacked to the simplified directory.
      FileUtils.copy(sourceDir, simplifiedDirectory);

      // Resulting XML
      File simplified = new File(simplifiedDirectory, MAIN_PART);
      LOGGER.debug("Deleting simplified {}.", simplified.getAbsolutePath());
      //Delete because it will be modified to simplify.
      simplified.delete();

      // Run XSLT
      Templates templates = XSLT.getTemplatesFromResource(BUILTIN_TEMPLATES);
      Transformer transformer = templates.newTransformer();
      for (Entry<String, String> s : settings.entrySet()) {
        transformer.setParameter(s.getKey(), s.getValue());
      }
      transformer.transform(new StreamSource(original), new StreamResult(simplified));


      ZipUtils.zip(simplifiedDirectory, output);
      result.downloadFile = output;

      // Stop the timer
      result.done();

    } catch (IOException | TransformerException ex) {
      result.setError(ex);
    }

    return result;
  }


  private File getInput(PackageData data, StepInfo info) throws IOException {
    File initialInput = StepUtils.getInput(data, info);
    File finalInput = initialInput;
    if (initialInput != null && initialInput.getPath().toLowerCase().endsWith(".docx")) {
      //In case it is a DOCX, it is necessary to unpack it.
      String filenameWithoutExtension = FileUtils.getNameWithoutExtension(initialInput);
      finalInput = new File(initialInput.getParent(), filenameWithoutExtension + "-unpacked");
      ZipUtils.unzip(initialInput, finalInput);
    }
    return finalInput;
  }

  private File getOutput(PackageData data, StepInfo info) {
    File output = StepUtils.getOutput(data, info, null);
    if (output != null) {
      if (data.directory().getPath().equals(output.getPath())) {
        //StepUtils.getOutput() returns the data directory
        output = new File(data.directory(), data.id() + "-simplified.docx");
      } else if (!output.getName().endsWith(".docx")) {
        //If the output is not a docx file, then we change it to docx
        output = new File(output, data.id() + "-simplified.docx");
      }
    }

    return output;
  }

  /**
   * Set all the known settings for the simplifier from the parameters in the content request as
   * parameters for the transformer
   *
   * @param parameters  The parameters from model
   */
  private static Map<String, String> computeSettings(Model model, PackageData data, StepInfo info) {
    Map<String, String> settings = new HashMap<String, String>();
    // Load values from the model
    Properties p = getModelSettings(model);
    for (Entry<Object, Object> e : p.entrySet()) {
      String name = e.getKey().toString();
      String value = e.getValue().toString();
      settings.put(name, value);
    }

    for (String setting : SIMPLIFIER_SETTINGS) {
      String value = StepUtils.getParameterWithoutDynamicLogic(data, info, setting, null);
      if (value != null) {
        settings.put(setting, value);
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

    private File _source;

    private final File _target;

    private final Map<String, String> _settings;

    private File downloadFile = null;

    private SimplifierResult(Model model, PackageData data, File source, File target, Map<String, String> settings) {
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

  /**
   * The Simplifier settings.
   */
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
