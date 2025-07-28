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
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var> the PackageData property which represents the inputs
 *     files to join, which is a relative path in {@link PackageData}. It could be a comma separted list.</li>
 * <li><var>output</var> the PackageData property which represents the output
 *     file where the images result files will be saved, which is a relative path
 *     in {@link PackageData}. </li>
 * </ul>
 *
 * @author Carlos Cabral
 * @version 26 March 2018
 */
public class JoinFiles implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(JoinFiles.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    // input file
    String input = info.getParameter("input", info.input());

    // output file
    String output = info.getParameter("output", info.output());
    return process(input, output, model, data, info);
  }

  /**
   * Process.
   *
   * @param input  the input
   * @param output the output
   * @param model  the model
   * @param data   the data
   * @param info   the info
   * @return the result
   */
  protected Result process(String input, String output, Model model, PackageData data, StepInfo info) {

    JoinFilesResult result = new JoinFilesResult(model, data, input, output);
    try {
      if (!StringUtils.isBlank(input) && !StringUtils.isBlank(output)) {

        File foutput = null;
        if (!StringUtils.isBlank(output)) {
          foutput = data.getFile(output);
        }

        XMLStringWriter writer = new XMLStringWriter(NamespaceAware.No);
        writer.openElement("root");
        List<File> files = data.getFiles(input);
        for (File inputFile:files) {
          if (inputFile.exists()) {
            writer.writeXML(FileUtils.read(inputFile));
          }
        }

        writer.closeElement();//root
        writer.close();
        FileUtils.write(writer.toString(), foutput);

      } else {
        LOGGER.warn("Cannot find file {} or output {}.", input, output);
        result.setError(new FileNotFoundException("The input/output file is null or invalid."));
      }
    } catch (IOException ex) {
      LOGGER.warn("Cannot convert file {} and save it in the folder {}", input, output, ex);
      result.setError(ex);
    }
    return result;
  }


  /* *********************************************************************
   * Result inner Class
   * *********************************************************************/
  /**
   * The Class PDFToImageResult.
   */
  private final class JoinFilesResult extends ResultBase {

    /** The input. */
    private final String _input;

    /** The output. */
    private final String _output;

    /**
     * Instantiates a new PDF to Image result.
     *
     * @param model  the model
     * @param data   the data
     * @param input  the input
     * @param output the output
     */
    public JoinFilesResult(Model model, PackageData data, String input, String output) {
      super(model, data);
      this._input = input;
      this._output = output;
    }

    /* (non-Javadoc)
     * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
     */
    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("name", "Join Files");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));
      xml.attribute("path", this._output);

      if (this._input != null) {
        xml.attribute("input", this._input);
      }

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      }
      xml.closeElement();// result
    }

    /* (non-Javadoc)
     * @see org.pageseeder.ox.tool.ResultBase#isDownloadable()
     */
    @Override
    public boolean isDownloadable() {
      return true;
    }
  }
}