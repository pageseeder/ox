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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.tool.FileResultInfo;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.MultipleFilesResult;
import org.pageseeder.ox.util.*;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * <p>A step to transform the specified input by using provided stylesheet.</p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>input</var> the xml file needs to be transformed, where is a relative path of package data.
 *  (if not specified, use upper step output as input.) It accepts dynamic value.</li>
 *  <li><var>output</var> the output file, where is a relative path of package data (optional).
 *  It accepts dynamic value.</li>
 *  <li><var>xsl</var> the stylesheet file to transform, which is a relative path of model folder.
 *  (This parameter will override the property <var>parameter-xsl</var>)</li>
 *  <li><var>display-result</var> whether to display the result xml into Result XML (default: true)</li>
 *  <li><var>_xslt-</var>Every parameter with the preffix "_xslt-" will be send to the xslt (without the preffix "_xslt-").</li>
 *  <li><var>input-extensions</var> If the input is a zip, the caller can specify which files it want to be transformed.</li>
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
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult}.</p>
 * <p>If <var>xsl</var> does not exist, it returns {@link InvalidResult}.</p>
 * <p>Otherwise return {@link TransformResult}
 *
 *
 * @author Ciber Cai
 * @since  17 June 2014
 */
public final class Transformation implements Step {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(Transformation.class);

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    Result result = null;
    try {
      //## Handle input files
      List<File> inputs = getInputFiles(model, data, info);
      if (inputs.isEmpty()) {
        return new InvalidResult(model, data).error(new FileNotFoundException("Cannot find the input file."));
      }

    //## Handle Output file
      File output = getOutputFile(data, info);
      final boolean isOutputAZip =  FileUtils.isZip(output);
      File zipOutput = null;
      if (isOutputAZip) {
        zipOutput = output;
        //The the output need to be a folder
        output = data.getFile(data.id() + System.nanoTime());
      } else if (inputs.size() > 1 || output.isDirectory()) {
        //If there is more than one input, than the output must be a zip
        zipOutput = data.getFile(getNewNameBaseOnOther("output.zip", true));
      }

      if (output.getName().indexOf(".") > -1) {
        //It is a file, therefore only creates the parent folder if necessary
        output.getParentFile().mkdir();
      } else {
        //It is a folder
        output.mkdirs();
      }

      //## Handle the transformation file
      File xsl = getXSLFile(inputs.get(0).getParentFile(), model, data, info);

      // throw the error
      if (xsl == null || !xsl.exists()) {
        return new InvalidResult(model, data).error(new FileNotFoundException("Cannot find the stylesheet file."));
      }

      // transform the result
      List <FileResultInfo> fileResultInfos = new ArrayList<>();
      File downloadableOuput = zipOutput != null ? zipOutput : output;
      result = new TransformResult(model, data, info, downloadableOuput, fileResultInfos, xsl);

      try {
        Transformer transformer = buildXSLTTransformer(xsl, data, info);
        for(File extraInput:inputs) {
          fileResultInfos.add(processFile(extraInput, output, transformer));
        }

        //If zip output is not null, then zip
        if (zipOutput != null) {
          ZipUtils.zip(output, zipOutput);
        }

        ((TransformResult)result).done();
      } catch (TransformerException | IOException ex) {
        LOGGER.error("Transform configuration exception: {}", ex.getMessage(), ex);
        ((TransformResult)result).setError(ex);
      }
    } catch (IOException ex) {
      LOGGER.error("Unexpected transformtion exception happened: {}", ex.getMessage(), ex);
      result = new InvalidResult(model, data).error(new FileNotFoundException("Unexpected transformtion exception happened: " + ex.getMessage()));
    }
    return result;
  }

  /**
   * Process file.
   *
   * @param input the input
   * @param output the output
   * @param transformer the transformer
   * @return the file result info
   */
  private FileResultInfo processFile(File input, File output, Transformer transformer) {
    ResultStatus status = ResultStatus.OK;
    //Output cannot be a folder
    File finalOutput = output;
    if (output.isDirectory()) {

      if (output.getAbsolutePath().equals(input.getParentFile().getAbsolutePath())) {
        //If the output is same folder of the input parent folder, then it changes the name.
        finalOutput = new File(output, getNewNameBaseOnOther(input.getName(), false));
      } else {
        //If the output is different folder of the input parent, then we keep the same name.
        finalOutput = new File(output, input.getName());
      }
    }

    try {
      transformer.transform(new StreamSource(input), new StreamResult(finalOutput));
    } catch (TransformerException ex) {
      LOGGER.error("Failed to trasnform the file {} because of '{}'", input.getAbsolutePath(), ex.getMessage());
      status = ResultStatus.ERROR;
    }
    return new FileResultInfo(input, finalOutput, status);
  }

  /**
   * Builds the XSLT transformer.
   *
   * @param xsl the xsl
   * @param data the data
   * @param info the info
   * @return the transformer
   * @throws TransformerConfigurationException the transformer configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Transformer buildXSLTTransformer (File xsl, PackageData data, StepInfo info) throws TransformerConfigurationException, IOException {
    URIResolver resolver = new CustomURIResolver(xsl.getParentFile());
    Templates templates = XSLT.getTemplates(xsl);
    Transformer transformer = templates.newTransformer();
    transformer.setURIResolver(resolver);

    // Add the parameters from post request
    for (Entry<String, String> p : data.getParameters().entrySet()) {
      transformer.setParameter(p.getKey(), p.getValue());
    }

    String originalFileName = data.getProperty(PackageData.ORIGINAL_PROPERTY);
    if (originalFileName != null) {
      transformer.setParameter("original_file", originalFileName);
    }
    transformer.setParameter("data-id", data.id());
    transformer.setParameter("data-repository", data.directory().getAbsolutePath().replace('\\', '/'));

    // Add the parameters from step definition in model.xml
    // these parameters should use the prefix _xslt-
    for (Entry<String, String> p :info.parameters().entrySet()) {
      if (p.getKey().startsWith("_xslt-")) {
        String newValue = StepUtils.applyDynamicParameterLogic(data, info, p.getValue());
        transformer.setParameter(p.getKey().replaceAll("_xslt-", ""), newValue);
      }
    }

    String indent = !StringUtils.isBlank(info.parameters().get("_xslt-indent")) ? info.parameters().get("_xslt-indent") : data.getParameter("_xslt-indent");
    if (!StringUtils.isBlank(indent)) {
      transformer.setOutputProperty(OutputKeys.INDENT, indent.equalsIgnoreCase("yes")?"yes":"no");
    }

    return transformer;
  }

  /**
   * If the input is a folder or zip, it gets the input files inside the folder or zip.
   *
   * @param model the model
   * @param data the data
   * @param info the info
   * @return the input files
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private @NonNull List<File> getInputFiles(Model model, PackageData data, StepInfo info) throws IOException {

    //Original inputs
    String inputParemeter = StepUtils.getParameter(data, info, "input", info.input());
    List<File> originalInputs = data.getFiles(inputParemeter);

    if (originalInputs == null || originalInputs.isEmpty()) {
      originalInputs = new ArrayList<>();
      File tempInput = model.getFile(inputParemeter);
      if (tempInput != null) originalInputs.add(tempInput);
    }

    //Load the extesion Allowed
    String extensionParameters = data.getParameter("input-extensions");
    if (StringUtils.isBlank(extensionParameters)) {
      extensionParameters = info.getParameter("input-extensions");
    }
    List<String> extensions = StringUtils.isBlank(extensionParameters) ? FileUtils.getXMLExtensions():StringUtils.convertToStringList(extensionParameters);

    List<File> finalInputs = new ArrayList<>();

    //Handle each input
    for (File input : originalInputs) {
      input.mkdirs();
      if (input.exists()) {
        //handle zip file
        final boolean isInputAZip =  FileUtils.isZip(input);
        if (isInputAZip) {
          input = unzipFile(input);
        }

        //Load candidate inputs
        if (input.isDirectory()) {
          FileFilter filter = FileUtils.filter(extensions, true);
          finalInputs.addAll(FileUtils.findFiles(input, filter));
        } else {
          finalInputs.add(input);
        }
      }
    }
    return finalInputs;
  }

  /**
   * Gets the output file.
   *
   * @param data the data
   * @param info the info
   * @return the output file
   */
  private File getOutputFile(PackageData data, StepInfo info) {
    File output = null;
    String outputParemeter = StepUtils.getParameter(data, info, "output", info.output());
    if (StringUtils.isBlank(outputParemeter) || outputParemeter.equals(info.input())) {
      outputParemeter = data.id() + System.nanoTime();
      output = data.getFile(outputParemeter);
      output.mkdirs();
    } else {
      output = data.getFile(outputParemeter);
    }
    return data.getFile(outputParemeter);
  }


  /**
   * Gets the XSL file.
   *
   * @param unzipedFolder the unziped folder
   * @param data the data
   * @param info the info
   * @return the XSL file
   */
  private File getXSLFile(File unzipedFolder, Model model, PackageData data, StepInfo info) {
    String xslParameter = info.getParameter("xsl", data.getParameter("xsl"));
    File xsl = null;
    if (!StringUtils.isBlank(xslParameter)) {
      xsl = model.getFile(xslParameter);
      if (xsl == null || !xsl.exists()) {
        xsl = data.getFile(xslParameter);
      }
    } else if (unzipedFolder.isDirectory()) {
      List<String> extensions = Arrays.asList("xsl");
      FileFilter filter = FileUtils.filter(extensions, true);
      List<File> filesFound = FileUtils.findFiles(unzipedFolder, filter);
      if (!filesFound.isEmpty()) {
        xsl = filesFound.get(0);
      }
    }
    return xsl;
  }

  /**
   * Unzip in a folder with the same of the zip.
   *
   * @param file the file
   * @return the file
   * @throws IOException
   */
  private File unzipFile(File file) throws IOException {
    //It is a zip, then unzip in a folderwith the same of the zip
    File newInput = new File(file.getParentFile(), FileUtils.getNameWithoutExtension(file));
    newInput.mkdirs();
    ZipUtils.unzip(file, newInput);
    return newInput;
  }

  /**
   * Gets the new name base on other.
   *
   * @param otherName the other name
   * @param shouldUseMilliseconds the should use milliseconds
   * @return the new name base on other
   */
  private String getNewNameBaseOnOther (String otherName, boolean shouldUseMilliseconds) {
    String addition = shouldUseMilliseconds ? "-transformed-" + System.nanoTime() :"-transformed";
    int lastDotPosition = otherName.lastIndexOf(".");
    if (lastDotPosition > -1) {
      return otherName.substring(0, lastDotPosition) + addition + otherName.substring(lastDotPosition);
    } else {
      return otherName + addition;
    }
  }

  /**
   * A custom URI resolver to get the stylesheet file.
   * @author Ciber Cai
   * @since  17 June 2014
   */
  private static class CustomURIResolver implements URIResolver {

    /**  the root of stylesheet *. */
    private final File _root;

    /**
     * Instantiates a new custom URI resolver.
     *
     * @param r the folder of the stylesheet
     */
    public CustomURIResolver(File r) {
      this._root = r;
    }

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
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
   * The Class TransformResult.
   */
  private class TransformResult extends MultipleFilesResult<FileResultInfo> implements Result {

    /** The template. */
    private final File _template;
    private final boolean _displayOutputResult;

    /**
     * Instantiates a new transform result.
     *
     * @param model the model
     * @param data the data
     * @param info the info
     * @param output the output
     * @param fileResultInfos the file result infos
     * @param template the template
     */
    public TransformResult(@NonNull Model model, @NonNull PackageData data, @NonNull StepInfo info,
        @Nullable File output, @NonNull List<FileResultInfo> fileResultInfos, @NonNull File template) {
      super(model, data, info, output, fileResultInfos);
      this._template = template;
      this._displayOutputResult = "false".equals(super.info().getParameter("display-result")) ? false : true;
    }

    @Override
    protected void writeResultElements(XMLWriter xml) throws IOException {
      super.writeResultElements(xml);
      if (this._template != null) {
        xml.openElement("template");
        String path = data().getPath(this._template);
        //The template can come from data package or model. If its from model it only shows file name.
        xml.attribute("path", StringUtils.isBlank(path)? this._template.getName():path);
        xml.closeElement();
      }
    }

    /**
     * Parameters XML.
     *
     * @param xml the xml
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void writeFileResultInfo(XMLWriter xml, FileResultInfo fileResultInfo) {
      try {
        xml.openElement("result-file");
        xml.attribute("input", data().getPath(fileResultInfo.getInput()));
        xml.attribute("output", data().getPath(fileResultInfo.getOutput()));
        xml.attribute("status", fileResultInfo.getStatus().toString());
        // Include the generated content
        if (_displayOutputResult) {
          xml.openElement("content");
          xml.writeCDATA(new String(Files.readAllBytes(fileResultInfo.getOutput().toPath()), "UTF-8"));
          xml.closeElement();
        }
        xml.closeElement();//parameters
      } catch (IOException io) {
        LOGGER.error("Unable to generate file result info for {}-{}-{}", fileResultInfo.getInput(), fileResultInfo.getOutput(), fileResultInfo.getStatus());
      }
    }
  }
}
