/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.core.StepInfoImpl;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.ox.xml.utils.XMLComparator;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

/**
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class TransformationTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/step/transformation");
    OXConfig.get().setModelsDirectory(modelDir);
  }

//  @Test
//  public void test() throws IOException {
//      File root = new File("C:\\Users\\ccabral\\Downloads\\2018-09-01");
//      File outputFolder = new File("C:\\Users\\ccabral\\Downloads\\2018-09-01\\output");
//      FileFilter filter = FileUtils.filter(Arrays.asList("psml"), true);
//      List<File> inputs = FileUtils.findFiles(root, filter);
//      File xsl = new File("C:\\Users\\ccabral\\Downloads\\2018-08-01\\xslt-sample.xsl");
//
//      inputs.forEach(input -> {
////        File source = new File("C:\\Users\\ccabral\\Downloads\\2018-08-01\\down-mps-2018-08-01-r1-b1.xml_add-bioeq_GE_9311C_83314011000036106_FK.psml");
////        File output = new File("C:\\Users\\ccabral\\Downloads\\oxford\\results-transformed.xml");
//        File output = new File(outputFolder, input.getName());
//        try {
//          Templates templates = XSLT.getTemplates(xsl);
//          Transformer transformer = templates.newTransformer();
//          transformer.transform(new StreamSource(input), new StreamResult(output));
//        } catch (TransformerException|IOException ex) {
//          // TODO Auto-generated catch block
//          ex.printStackTrace();
//        }
//
//      });
//  }
//

  @Test
  public void test_process() throws IOException {
    File source = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/sample.xml");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/sample-indented.xml");
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/simple/result-display-true.xml");
    String outputName = "/output/sample.xml";

    Model model = new Model("common");
    PackageData data = PackageData.newPackageData("Transformation", source);
    Map<String, String> params = new HashMap<>();
    params.put("input", "sample.xml");
    params.put("output", outputName);
    params.put("xsl", "xslt-sample.xsl");
    params.put("_xslt-indent", "yes");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", "sample.xml", params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);
    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);

    xmlWriter.flush();
    xmlWriter.close();
    Assert.assertEquals(ResultStatus.OK, result.status());
    File targetCreated = data.getFile(outputName);
    String resultXML = xmlWriter.toString();


    Assert.assertEquals(ResultStatus.OK, result.status());
    List<String> attributesToIgnore = Arrays.asList("id", "time");
    validateResult(targetExpected, resultExpected, targetCreated, resultXML, attributesToIgnore);
  }

  @Test
  public void test_processGlobPattern() throws IOException {
    File source = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/sample.xml");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/sample.xml");
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/glob/result-display-true.xml");
    File xsl = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/xslt-sample.xsl");
    String outputName = "/output/sample.xml";
    Model model = new Model("common");
    PackageData data = PackageData.newPackageData("Transformation", null);

    //Copy source to data package
    Files.copy(source.toPath(), new File(data.directory(), source.getName()).toPath());

    //Copy xsl to data package
    Files.copy(xsl.toPath(), new File(data.directory(), xsl.getName()).toPath());

    Map<String, String> params = new HashMap<>();
    params.put("input", "*.xml");
    params.put("output", outputName);
    params.put("xsl", "xslt-sample.xsl");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", outputName, params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);
    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);

    xmlWriter.flush();
    xmlWriter.close();

    File targetCreated = data.getFile(outputName);
    String resultXML = xmlWriter.toString();

    Assert.assertEquals(ResultStatus.OK, result.status());
    List<String> attributesToIgnore = Arrays.asList("id", "time");
    validateResult(targetExpected, resultExpected, targetCreated, resultXML, attributesToIgnore);
  }

  @Test
  public void test_processInputZipWithoutXSL() throws IOException {
    File source = new File("src/test/resources/org/pageseeder/ox/step/transformation/zip_without_xsl/multiple-xmls-without-xsl.zip");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/");
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/zip_without_xsl/result-display-false.xml");
    File xsl = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/xslt-sample.xsl");

    Model model = new Model("common");
    PackageData data = PackageData.newPackageData("Transformation", source);

    //Copy xsl to data package
    Files.copy(xsl.toPath(), new File(data.directory(), xsl.getName()).toPath());

    Map<String, String> params = new HashMap<>();
    params.put("input", "multiple-xmls-without-xsl.zip");
    params.put("xsl", "xslt-sample.xsl");
    params.put("display-result", "false");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", "", params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);

    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);
    xmlWriter.flush();
    xmlWriter.close();

    Assert.assertEquals(ResultStatus.OK, result.status());
    File targetCreated = data.getFile("output*.zip");
    String resultXML = xmlWriter.toString();

    List<String> attributesToIgnore = Arrays.asList("output","id", "time","path");
    validateResult(targetExpected, resultExpected, targetCreated, resultXML, attributesToIgnore);
  }

  @Test
  public void test_processInputZipWithXSL() throws IOException {
    File source = new File("src/test/resources/org/pageseeder/ox/step/transformation/zip_with_xsl/multiple-xmls-with-xsl.zip");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/");
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/zip_with_xsl/result-display-true.xml");

    Model model = new Model("common");
    PackageData data = PackageData.newPackageData("Transformation", source);
    Map<String, String> params = new HashMap<>();
    params.put("input", "multiple-xmls-with-xsl.zip");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", "", params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);

    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);
    xmlWriter.flush();
    xmlWriter.close();

    Assert.assertEquals(ResultStatus.OK, result.status());
    File targetCreated = data.getFile("output*.zip");
    String resultXML = xmlWriter.toString();

    List<String> attributesToIgnore = Arrays.asList("output","id", "time","path");
    validateResult(targetExpected, resultExpected, targetCreated, resultXML, attributesToIgnore);
  }

  @Test
  public void test_processInputZipWithXSLOneFile() throws IOException {
    File source = new File("src/test/resources/org/pageseeder/ox/step/transformation/zip_with_xsl_one_file/input.zip");
    File targetExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/common/");
    File resultExpected = new File("src/test/resources/org/pageseeder/ox/step/transformation/zip_with_xsl_one_file/result.xml");

    Model model = new Model("common");
    PackageData data = PackageData.newPackageData("Transformation", source);
    Map<String, String> params = new HashMap<>();
    params.put("display-result", "false");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "input.zip", "", params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);

    XMLStringWriter xmlWriter = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xmlWriter);
    xmlWriter.flush();
    xmlWriter.close();

    Assert.assertEquals(ResultStatus.OK, result.status());
    File targetCreated = data.getFile("output*.zip");
    String resultXML = xmlWriter.toString();

    List<String> attributesToIgnore = Arrays.asList("output","id", "time","path");
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
        System.out.println(created.getAbsolutePath());
        System.out.println(expected.getAbsolutePath());
        XMLComparator.compareXMLFile(created, expected);
      }
    }

    //Validate Result
    XMLComparator.isSimilar(resultXMLExpected, resultXML, attributesToIgnore);
  }
}
