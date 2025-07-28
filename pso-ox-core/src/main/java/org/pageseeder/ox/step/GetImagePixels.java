/*
 * Copyright 2022 Allette Systems (Australia)
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
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.ImageInfoUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * <p>.</p>
 *
 * @author Carlos Cabral
 * @since 17 April 2018
 */
public class GetImagePixels implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(GetImagePixels.class);

  /** The image extensions allowed. */
  private String [] imageExtensionsAllowed;

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    // images folder
    String inputParameter = info.getParameter("input");

    // output file
    String outputParameter = info.getParameter("output");



    LOGGER.debug("Input {}, output {}", inputParameter, outputParameter);

    GetImagePixelsResult result = new GetImagePixelsResult(model, data, inputParameter, outputParameter);

    String errorMessage = "";

    if (StringUtils.isBlank(inputParameter)) {
      errorMessage += " Input is blank.";
    }

    if (StringUtils.isBlank(outputParameter)) {
      errorMessage += " Output is blank.";
    }

    if (errorMessage.length()==0) {
      File input = data.getFile(inputParameter);
      File output = data.getFile(outputParameter);

      try (FileWriter fw = new FileWriter(output);
          BufferedWriter bw = new BufferedWriter(fw);) {
        this.imageExtensionsAllowed = info.getParameter("image-extensions-allowed", "").toLowerCase().split(",");
        LOGGER.debug("Images Allowed: {}", this.imageExtensionsAllowed.toString());
        if(this.imageExtensionsAllowed.length > 0) Arrays.sort(this.imageExtensionsAllowed);

        String indent = !StringUtils.isBlank(info.parameters().get("_xslt-indent")) ? info.parameters().get("_xslt-indent") : data.getParameter("_xslt-indent");
        XMLWriterImpl writer = new XMLWriterImpl(bw, "yes".equalsIgnoreCase(indent));
        writer.openElement("images");
        if (input.isDirectory()) {
          handleDirectory(writer, input);
        } else {
          handleFile(writer,input);
        }
        writer.closeElement();
        writer.flush();
        writer.close();
      } catch (IOException ex) {
        LOGGER.error("Exception: {}", ex.getMessage());
        result.setError(ex);
      }
    } else {
      result.setError(new FileNotFoundException(errorMessage));
    }

    return result;

  }

  /**
   * Handle directory.
   *
   * @param writer the writer
   * @param directory the directory
   */
  private void handleDirectory(XMLWriterImpl writer, File directory) {
    if (directory.exists()) {
      for (File file:directory.listFiles()) {
        if (file.isDirectory()) {
          handleDirectory(writer, file);
        } else {
          handleFile(writer, file);
        }
      }
    }
  }

  /**
   * Handle file.
   *
   * @param writer the writer
   * @param file the file
   */
  private void handleFile(XMLWriterImpl writer, File file) {
    try {
      if (file != null && file.exists()) {
        //Images allowed PNG ,JPG, GIF, BMP, PDF, SVG

        int [] pixels = getPixel(file);
        writer.openElement("image");
        writer.attribute("filename", file.getName());
        writer.attribute("width", String.valueOf(pixels[0]));
        writer.attribute("height", String.valueOf(pixels[1]));
        writer.attribute("allowed", String.valueOf(isImageAllowed(file)));
        writer.closeElement();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
      LOGGER.error("Unable to calculate the pixels for {}.", file.getName());
    }
  }

  /**
   * Gets the pixel.
   *
   * @param image the image
   * @return the pixel
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private int[] getPixel(File image) throws IOException {
    int [] result = {0,0};
    try {
      BufferedImage hugeImage = ImageIO.read(image);
      if (hugeImage != null) {
        int width = hugeImage.getWidth();
        int height = hugeImage.getHeight();
        result [0] = width;
        result [1] = height;
      } else {
        result = getPixelRunningImageInfoUtility(image);
      }
    } catch (IIOException ex) {
      result = getPixelRunningImageInfoUtility(image);
    }
    return result;
  }

  /**
   * Gets the pixel running image info utility.
   *
   * @param image the image
   * @return the pixel running image info utility
   */
  private int[] getPixelRunningImageInfoUtility (File image){
    int [] result = {0,0};
    try (InputStream in = new FileInputStream(image);) {
      ImageInfoUtils imageInfoUtils = new ImageInfoUtils();
      imageInfoUtils.setInput(in);
      imageInfoUtils.setDetermineImageNumber(true);
      imageInfoUtils.setCollectComments(false);
      if (imageInfoUtils.check()) {
        result[0] = imageInfoUtils.getWidth();
        result[1] = imageInfoUtils.getHeight();
      }
    } catch (IOException ex) {
      LOGGER.debug("File not found {}: {}", image.getName(), ex.getMessage());
    }
    return result;
  }

  /**
   * Checks if is image allowed.
   *
   * @param image the image
   * @return true, if is image allowed
   */
  private boolean isImageAllowed(File image) {
    boolean isAllowed = false;
    String extension = FileUtils.getFileExtension(image).toLowerCase();
    if (Arrays.binarySearch(this.imageExtensionsAllowed, extension) >= 0) isAllowed = true;
    return isAllowed;
  }

  /**
   * The Class GetImagePixelsResult.
   */
  private static class GetImagePixelsResult extends ResultBase implements Result, Downloadable {

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
    private GetImagePixelsResult(Model model, PackageData data, String input,String output) {
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
      if (this._output != null) xml.attribute("path", data().getPath(downloadPath()));
      xml.attribute("input", this._input);

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