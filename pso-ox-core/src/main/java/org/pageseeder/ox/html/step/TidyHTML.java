/* Copyright (c) 2018 Allette Systems pty. ltd. */
package org.pageseeder.ox.html.step;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.html.tidy.TidyFileResultInfo;
import org.pageseeder.ox.html.tidy.TidyOXMessageListener;
import org.pageseeder.ox.html.tidy.TidyVoidWriter;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.MultipleFilesResult;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;

/**
 * <p>A step to tidy an html.</p>
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>input</var> the html file(s) that needs to be tidy up.</li>
 *  <li><var>output</var> the output file, where is a relative path of package data (optional)</li>
 *  <li><var>input-extensions</var> If the input is a zip, the caller can specify which files it want to be transformed.</li>
 *  <li><var>display-result</var> whether to display the result xml into Result XML (default: false)</li>
 *  <li><var>charset</var> default utf-8</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult}.</p>
 * <p>Otherwise return {@link TidyHTMLResult}
 *
 *
 * @author Carlos cabral
 */
public final class TidyHTML implements Step {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TidyHTML.class);

  /**
   * The resource path to the builtin templates.
   */
  private static final String DEFAULT_TIDY_PROPERTIES = "org/pageseeder/ox/html/builtin/tidy.properties";
  
  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    Result result = null;
    try {
      //## Handle input file
      File input = getInputFile(model, data, info);
      if (input == null || !input.exists()) { 
        return new InvalidResult(model, data).error(new FileNotFoundException("Cannot find the input file.")); 
      }
      input.mkdirs();
      final boolean isInputAZip =  FileUtils.isZip(input); 
      File zipInput = null;
      if (isInputAZip) {
        zipInput = input;
        input = unzipFile(input);      
      }
      
      //## Handle Output file 
      File output = getOutputFile(data, info);
      final boolean isOutputAZip =  FileUtils.isZip(output);
      File zipOutput = null;
      if (isOutputAZip) {      
        zipOutput = output;
        //The the output need to be a folder
        output = data.getFile(data.id() + System.nanoTime());
      } else if (isInputAZip) {
        //Ouput is not a zip but the input is, then we have to have an output zip      
        zipOutput = data.getFile(getNewNameBaseOnOther(zipInput.getName(), true));
      }
      if (output.getName().indexOf(".") > -1) {
        //It is a file, therefore only creates the parent folder if necessary
        output.getParentFile().mkdir();
      } else {
        //It is a folder
        output.mkdirs();
      }
          
      // transform the result
      List <TidyFileResultInfo> fileResultInfos = new ArrayList<>();
      File originalInput = zipInput != null? zipInput : input;
      File downloadableOuput = zipOutput != null ? zipOutput : output;
      result = new TidyHTMLResult(model, data, info, originalInput, downloadableOuput, fileResultInfos);
      
      try {
        //Load all the possible inputs (if there are)
        List<File> inputs = getInputFiles(input, data, info);      
        Tidy tidy = newTidy(model);   
        for(File extraInput:inputs) {
          fileResultInfos.add(processFile(extraInput, output, tidy, data, info));
        }
        
        //If zip output is not null, then zip
        if (zipOutput != null) {
          ZipUtils.zip(output, zipOutput);
        }
        
        ((TidyHTMLResult)result).done();
      } catch (IOException ex) {
        LOGGER.error("Transform configuration exception: {}", ex.getMessage(), ex);
        ((TidyHTMLResult)result).setError(ex);
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
   * @param tidy the tidy
   * @return the file result info
   */
  private TidyFileResultInfo processFile(File input, File output, Tidy tidy, PackageData data, StepInfo info) {
    ResultStatus status = ResultStatus.OK;
    //Output cannot be a folder
    File finalOutput = output;
    if (output.isDirectory()) {
      finalOutput = new File(output, getNewNameBaseOnOther(input.getName(), false));
    }
    
    //Defining the message listener
    TidyOXMessageListener messageListener = new TidyOXMessageListener();
    tidy.setMessageListener(messageListener);

    // so we strip it out as the transformer doesn't recognise it
    String charset = data.getParameter("charset");
    if (StringUtils.isBlank(charset)) charset = info.getParameter("charset", "utf-8");
    try {
      String html = FileUtils.read(input, charset);
      //TODO why we need it
      html = html.replaceAll("<\\?xml(.*?)\\>", "");
  
      //TIDY HTML
      StringWriter buffer = new StringWriter();
      tidy.parse(new StringReader(html), buffer);      
      String xhtml = buffer.toString();
      //TODO try to udenrstand it
      // We must remove the XHTML DOCTYPE declaration since W3 has shutdown its servers for XHTML DTDs
      xhtml = xhtml.replaceAll("<\\!DOCTYPE(.*?)\\>\n?", "");
      
      // Save the file
      FileUtils.write(xhtml, finalOutput, charset);
    } catch (IOException ex) {
      LOGGER.error("Failed to trasnform the file {} because of '{}'", input.getAbsolutePath(), ex.getMessage());
      status = ResultStatus.ERROR;
    }
    
    return new TidyFileResultInfo(input, finalOutput, status, messageListener.getMessages());
  }
  
  /**
   * Return a new tidy configuration for the model.
   *
   * @param model the model
   * @return the Tidy
   * @throws IOException 
   */
  private Tidy newTidy(Model model) throws IOException {
    /** To completely ignore print messages (captured via callback messages anyway) */
    final PrintWriter VOID_PRINTER = new PrintWriter(new TidyVoidWriter());
    Tidy tidy = new Tidy();
    tidy.setXHTML(true);
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
    tidy.setWraplen(0);
    tidy.setNumEntities(true);
    tidy.setXmlOut(true);
    tidy.setOutputEncoding("utf-8");
    tidy.setDocType(null);
    tidy.setTidyMark(false);
    tidy.setErrout(VOID_PRINTER);

    // Try to load config from model or from classpath
    Properties p = getTidyProperties(model);
    if (p != null) {
      tidy.setConfigurationFromProps(p);
    }
    
    return tidy;
  }

  /**
   * Gets the tidy properties.
   *
   * @param model the model
   * @return the tidy properties
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Properties getTidyProperties (Model model) throws IOException {
    Properties properties = model.getProperties("tidy.properties");
    if (properties == null) {
      try (InputStream input = TidyHTML.class.getClassLoader().getResourceAsStream(DEFAULT_TIDY_PROPERTIES)) {
        if (input != null ) {
          properties = new Properties();
          properties.load(input);
        }
      }
    }
    return properties;
  }
  
  /**
   * Gets the input files.
   *
   * @param model the model
   * @param data the data
   * @param info the info
   * @return the input files
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private File getInputFile(Model model, PackageData data, StepInfo info) throws IOException {
    String inputParemeter = info.getParameter("input", info.input());
    File input = data.getFile(inputParemeter);

    if (input == null || !input.exists()) {
      input = model.getFile(inputParemeter);
    }
    return input; 
  }  
  
  /**
   * If the input is a folder or zip, it gets the input files inside the folder or zip.
   *
   * @param input the input
   * @param data the data
   * @param info the info
   * @return the input files
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private List<File> getInputFiles(File input, PackageData data, StepInfo info) throws IOException {
    List<File> inputFiles = new ArrayList<>();
    if (input.isDirectory()) {
      String extensionParameters = data.getParameter("input-extensions");
      if (StringUtils.isBlank(extensionParameters)) {
        extensionParameters = info.getParameter("input-extensions");
      }
      List<String> extensions = StringUtils.isBlank(extensionParameters) ? FileUtils.getXMLExtensions():StringUtils.convertToStringList(extensionParameters);
      FileFilter filter = FileUtils.filter(extensions, true);
      inputFiles.addAll(FileUtils.findFiles(input, filter));
    } else {
      if (FileUtils.isZip(input)) {
        File newInput = unzipFile(input);
        inputFiles.addAll(getInputFiles(newInput, data, info));
      } else {
        inputFiles.add(input);
      }
    }
    return inputFiles;
  } 
  
  /**
   * Gets the output file.
   *
   * @param data the data
   * @param info the info
   * @return the output file
   */
  private File getOutputFile(PackageData data, StepInfo info) {
    String outputParemeter = info.getParameter("output", info.output());
    if (StringUtils.isBlank(outputParemeter) || outputParemeter.equals(info.input())) {
      outputParemeter = data.id() + System.nanoTime();
    } 
    return data.getFile(outputParemeter);
  }
  
  /**
   * Unzip in a folder with the same of the zip.
   *
   * @param file the file
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
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
   * The Class TidyHTMLResult.
   */
  private class TidyHTMLResult extends MultipleFilesResult<TidyFileResultInfo> implements Result {

    /** The template. */
    private final boolean _displayOutputResult;
    
    /**
     * Instantiates a new transform result.
     *
     * @param model the model
     * @param data the data
     * @param info the info
     * @param input the input
     * @param output the output
     * @param fileResultInfos the file result infos
     */
    public TidyHTMLResult(@NonNull Model model, @NonNull PackageData data, @NonNull StepInfo info, @NonNull File input,
        @Nullable File output, @NonNull List<TidyFileResultInfo> fileResultInfos) {
      super(model, data, info, input, output, fileResultInfos);
      this._displayOutputResult = "true".equals(super.info().getParameter("display-result")) ? true : false;
    }
    
    /**
     * Parameters XML.
     *
     * @param xml the xml
     * @param fileResultInfo the file result info
     */
    protected void writeFileResultInfo(XMLWriter xml, TidyFileResultInfo fileResultInfo) {
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
        xml.openElement("messages");
        for (TidyMessage message:fileResultInfo.getMessages()) {
          xml.openElement("message");
          xml.attribute("text", message.getMessage());
          xml.attribute("line", message.getLine());
          xml.attribute("column", message.getColumn());
          xml.attribute("error-code", message.getErrorCode());
          xml.attribute("level", message.getLevel().toString());
          xml.closeElement();
        }
        xml.closeElement();//messages
        
        xml.closeElement();//parameters
      } catch (IOException io) {
        LOGGER.error("Unable to generate file result info for {}-{}-{}", fileResultInfo.getInput(), fileResultInfo.getOutput(), fileResultInfo.getStatus());
      }
    }
  }
}
