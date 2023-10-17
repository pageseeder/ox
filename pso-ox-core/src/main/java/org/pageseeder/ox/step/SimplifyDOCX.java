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
import org.pageseeder.ox.tool.*;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.XSLT;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
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
        result = process(model, data, info, sourceDirectory, output, computeSettings(model, data, info));
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

  /**
   *
   * @param model
   * @param data
   * @param info
   * @param sourceDir
   * @param output
   * @param settings
   * @return
   */
  private DefaultResult process(Model model, PackageData data, StepInfo info, File sourceDir, File output, Map<String, String> settings) {
    DefaultResult result = new DefaultResult(model, data, info, output);

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

      XMLStringWriter xmlStringWriter = new XMLStringWriter(XML.NamespaceAware.No);
      xmlStringWriter.openElement("docx-main-part");
      xmlStringWriter.attribute("source-path", data.getPath(original));
      xmlStringWriter.attribute("simplified-path", data.getPath(simplified));
      xmlStringWriter.closeElement();
      xmlStringWriter.close();
      ExtraResultStringXML resultStringXML = new ExtraResultStringXML(xmlStringWriter.toString());
      result.addExtraXML(resultStringXML);

      ZipUtils.zip(simplifiedDirectory, output);

    } catch (IOException | TransformerException ex) {
      result.setError(ex);
    }

    return result;
  }


  /**
   *
   * @param data
   * @param info
   * @return
   * @throws IOException
   */
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

  /**
   *
   * @param data
   * @param info
   * @return
   */
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
   * @param model
   * @param data
   * @param info
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
