/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.html.step;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.ox.xml.utils.XMLComparator;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

/**
 * @author Carlos Cabral
 * @since 20 August 2018
 */
public class TidyHTMLTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/html/step/tidy");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void test_processDisplayFalse() throws IOException {
    String outputFileName = "tidy.html";
    File source = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common/source-1.html");
    Map<String, String> params = new HashMap<>();
    params.put("display-result", "false");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common/source-1-transformed.html");  
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/simple/result-display-false.xml");
    process(source, outputFileName, targetExpected, resultExpected, params);
  } 
  
  @Test
  public void test_processDisplayTrue() throws IOException {
    String outputFileName = "tidy.html";
    File source = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common/source-1.html");
    Map<String, String> params = new HashMap<>();
    params.put("display-result", "true");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common/source-1-transformed.html");  
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/simple/result-display-true.xml");
    process(source, outputFileName, targetExpected, resultExpected, params);
  } 

  @Test
  public void test_processInputZip() throws IOException {
    String outputFileName = "outcome.zip";
    File source = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/zip/source.zip");
    Map<String, String> params = new HashMap<>();
    params.put("display-result", "false");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common");  
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/zip/result-display-false.xml");
    process(source, outputFileName, targetExpected, resultExpected, params);
  } 

  @Test
  public void test_processInputZipWitOneFile() throws IOException {
    String outputFileName = "outcome.zip";
    File source = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/zip_one_file/source.zip");
    Map<String, String> params = new HashMap<>();
    params.put("display-result", "false");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common");  
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/zip_one_file/result-display-false.xml");
    process(source, outputFileName, targetExpected, resultExpected, params);
  } 

  @Test
  public void test_processInputGlobPattern() throws IOException {    
    PackageData data = PackageData.newPackageData("tidy-html", null);
    File source = data.getFile("source");
    source.mkdir();
    File source1 = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common/source-1.html");
    File source2 = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common/source-2.html");
    Files.copy(source1.toPath(), new File(source, source1.getName()).toPath());
    Files.copy(source2.toPath(), new File(source, source2.getName()).toPath());
    Map<String, String> params = new HashMap<>();
    params.put("display-result", "false");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/common");  
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/html/step/tidy/glob/result-display-false.xml");

    Model model = new Model("common");   
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "*source/*.html", "", params);

    TidyHTML step = new TidyHTML();
    Result result = step.process(model, data, info);
    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);
    xmlWriter.flush();
    xmlWriter.close();
    
    File targetCreated = data.getFile("*.zip");    
    String resultXML = xmlWriter.toString();
    
    // Validate Result
    List<String> attributesToIgnore = Arrays.asList("output","id", "time","path");
    validateResult(targetExpected, resultExpected, targetCreated, resultXML, attributesToIgnore);
  }
  
  /**
   * Process.
   *
   * @param source the source
   * @param outputFileName the output file name
   * @param targetExpected the target expected (If output is a zip, it should be the folder with the files)
   * @param resultExpected the result expected
   * @param params the params
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void process(File source, String outputFileName, File targetExpected, File resultExpected, @NonNull Map<String, String> params) throws IOException {
    final boolean isSourceZip = FileUtils.isZip(source);
    Model model = new Model("common");
    PackageData data = PackageData.newPackageData("tidy-html", source);
    
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", source.getName(), outputFileName, params);

    TidyHTML step = new TidyHTML();
    Result result = step.process(model, data, info);
    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);
    xmlWriter.flush();
    xmlWriter.close();
    
    File targetCreated = data.getFile(outputFileName);    
    String resultXML = xmlWriter.toString();
    List<String> attributesToIgnore = isSourceZip ? Arrays.asList("id", "time","output") : Arrays.asList("id", "time");
    
    //Validate Result
    validateResult(targetExpected, resultExpected, targetCreated, resultXML, attributesToIgnore);
  }  
  
  
  private void validateResult(File targetExpected, File resultExpected, File targetCreated, String resultXML, List<String> attributesToIgnore) throws IOException {
    String resultXMLExpected = FileUtils.read(resultExpected);
    System.out.println(resultXML);
    
    final boolean isOutputZip = FileUtils.isZip(targetCreated); 
    //Validate Output
    if (!isOutputZip) {
      XMLComparator.compareXMLFile(targetCreated, targetExpected);
    } else {
      File outputFolder = new File(targetCreated.getParentFile(), "test");
      outputFolder.mkdir();
      ZipUtils.unzip(targetCreated, outputFolder);
      for (File created:outputFolder.listFiles()) {
        File expected = new File(targetExpected, created.getName());
        Assert.assertNotNull("Target expected is null for " + (targetExpected.getAbsolutePath() + "/" + created.getName()), expected);
        XMLComparator.compareXMLFile(created, expected);
      }
    }
        
    //Validate Result
    XMLComparator.isSimilar(resultXMLExpected, resultXML, attributesToIgnore);
  }
}
