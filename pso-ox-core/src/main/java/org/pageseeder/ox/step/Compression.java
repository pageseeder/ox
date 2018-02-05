/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.step;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Downloadable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A step to compress the file </p>.
 *
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var> the input file or folder needs to compress, which is a relative path in {@link PackageData} .</li>
 * <li><var>output</var> the output file after compressed, which is a relative path in {@link PackageData} .</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>Otherwise return {@link CompressionResult}
 *
 *
 * @author Ciber Cai
 * @since  23 June 2014
 */
public class Compression implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(Compression.class);

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    // input file
    String input = info.getParameter("input", info.input());
    // output file
    String output = info.getParameter("output") != null
        ? info.getParameter("output")
        : (info.output().equals(info.input())) ? (info.output() + ".zip") : info.output();
    LOGGER.debug("input {}, output {}", input, output);

    File inputFile = data.getFile(input);
    File outputFile = data.getFile(output);

    // if the input file is not exist
    if (inputFile == null || !inputFile.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the input file " + input + ".")); }

    CompressionResult result = new CompressionResult(model, data, input, output);
    if (inputFile != null && inputFile.exists()) {
      try {
        ZipUtils.zip(inputFile, outputFile);
      } catch (IOException ex) {
        LOGGER.warn("Cannot compress file {} to {}", inputFile, outputFile, ex);
        result.setError(ex);
      }
    } else {
      result.setError(new FileNotFoundException("Cannot find input file " + input + "."));
    }

    return result;

  }

  /**
   * The Class CompressionResult.
   */
  private static class CompressionResult extends ResultBase implements Result, Downloadable {

    /** The input. */
    private final String _input;

    /** The output. */
    private final String _output;

    /**
     * Instantiates a new compression result.
     *
     * @param model the model
     * @param data the data
     * @param input the input
     * @param output the output
     */
    private CompressionResult(Model model, PackageData data, String input, String output) {
      super(model, data);
      this._input = input;
      this._output = output;
    }

    /* (non-Javadoc)
     * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
     */
    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("name", "Compression-Result");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));
      xml.attribute("path", data().getPath(downloadPath()));

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
     * @see org.pageseeder.ox.api.Downloadable#downloadPath()
     */
    @Override
    public File downloadPath() {
      File outputFile = data().getFile(this._output);
      return outputFile;
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
