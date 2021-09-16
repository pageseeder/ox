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
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var>The source file or folder to be copied from .</li>
 * <li><var>output</var>The destination.</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>Otherwise return {@link CopyResult}
 *
 * @author Carlos Cabral
 * @author Ciber Cai
 * @since  13 April 2015
 */
public class Copy implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(Copy.class);

  /**
   * Process.
   *
   * @param model the model
   * @param data the data
   * @param info the info
   * @return the result
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    // input file
    String source = StepUtils.applyDynamicParameterLogic(data, info, info.getParameter("input", info.input()));

    String destination = info.getParameter("output") != null
        ? info.getParameter("output")
        : (info.input().equals(info.output()) ? (info.output() + ".copy") : info.output());

    destination = StepUtils.applyDynamicParameterLogic(data, info, destination);

    File sourceFile = data.getFile(source);

    if (sourceFile == null || !sourceFile.exists()) {
      sourceFile = model.getFile(source);
    }

    File destinationFile = data.getFile(destination);

    // if the source file (directory) doesn't exist
    if (sourceFile == null || !sourceFile.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the input file " + source + ".")); }

    CopyResult result = new CopyResult(model, data, source, destination);
    try {
      FileUtils.copy(sourceFile, destinationFile);
    } catch (IOException ex) {
      LOGGER.error("Error while copying the files: " + ex.getMessage());
      result.setError(ex);
    }
    return result;
  }

  /**
   * The Class CopyResult.
   */
  private static class CopyResult extends ResultBase implements Result, Downloadable {

    /** The input. */
    private final String _input;

    /** The output. */
    private final String _output;

    private final File _outputFile;

    /**
     * Instantiates a new copy result.
     *
     * @param model the model
     * @param data the data
     * @param input the input
     * @param output the output
     */
    private CopyResult(Model model, PackageData data, String input, String output) {
      super(model, data);
      this._input = input;
      this._output = output;
      this._outputFile = data().getFile(this._output);
    }

    /**
     * To XML.
     *
     * @param xml the xml
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("name", "Copy");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));
      xml.attribute("path", data().getPath(downloadPath()));

      if (this._input != null) {
        xml.attribute("input", this._input);
      }
      if (this._output != null) {
        xml.attribute("output", this._output);
      }

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      }
      xml.closeElement();// result
    }

    /**
     * Download path.
     *
     * @return the file
     */
    @Override
    public File downloadPath() {
      return this._outputFile;
    }

    /**
     * Checks if is downloadable.
     *
     * @return true, if is downloadable
     */
    /* (non-Javadoc)
     * @see org.pageseeder.ox.tool.ResultBase#isDownloadable()
     */
    @Override
    public boolean isDownloadable() {
      return this._outputFile.isFile();
    }
  }

}
