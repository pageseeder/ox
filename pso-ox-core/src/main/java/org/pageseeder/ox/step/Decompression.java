/* Copyright (c) 2014 Allette Systems pty. ltd. */
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
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <p>A step to decompress the zip file </p>.
 *
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var> the input file needs to depress, which is a relative path in {@link PackageData} .</li>
 * <li><var>output</var> the output file after depressed, which is a relative path in {@link PackageData} .</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>Otherwise return {@link DecompressionResult}
 *
 * <p><b>Note: </b> Input and output allows dynamic/variable value.</p>
 *
 * @author Ciber Cai
 * @since  23 June 2014
 */
public class Decompression implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(Decompression.class);

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    // input file
    String input = StepUtils.getParameter(data, info, "input", info.input());

    // output file
    String output = StepUtils.getParameter(data, info, "output", info.output());
    if (output.equals(input)) {
      output += "-decompress";
    }

    File inputFile = data.getFile(input);
    File outputFile = data.getFile(output);
    // if the input file is not exist
    // if the input file is not exist
    if (inputFile == null || !inputFile.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the input file " + input + ".")); }

    // remove the outpuf if exists.
    try {
      LOGGER.debug("Deleting file {}.", outputFile.getAbsolutePath());
      FileUtils.delete(outputFile);
    } catch (IOException ex) {
      LOGGER.warn("Cannot delete the file {}", outputFile, ex);
    }

    DecompressionResult result = new DecompressionResult(model, data, input, output);
    try {
      ZipUtils.unzip(inputFile, outputFile);
    } catch (IOException ex) {
      LOGGER.error("Cannot decompress file {} to {}", inputFile, outputFile, ex);
      result.setError(ex);
    }

    return result;

  }

  /**
   * The Class DecompressionResult.
   */
  private static class DecompressionResult extends ResultBase implements Result, Downloadable {

    /**  the input. */
    private final String _input;

    /**  the output. */
    private final String _output;

    /**
     * Instantiates a new decompression result.
     *
     * @param model  the model
     * @param data the packageData
     * @param input the input
     * @param output the output
     */
    private DecompressionResult(Model model, PackageData data, String input, String output) {
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
      xml.attribute("name", "Decompression-Result");
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
      return false;
    }
  }
}
