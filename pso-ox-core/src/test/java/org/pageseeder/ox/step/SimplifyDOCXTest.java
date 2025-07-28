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

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.core.StepInfoImpl;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Simplify docx test.
 *
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class SimplifyDOCXTest {

  /**
   * Init.
   */
  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  /**
   * Test process.
   *
   * @throws IOException the io exception
   */
  @Test
  public void test_process() throws IOException {
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Simplify", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "", params);

    SimplifyDOCX step = new SimplifyDOCX();
    Result result = step.process(model, data, info);
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xml);

    xml.flush();
    xml.close();

    System.out.println(data.directory());
    System.out.println(xml.toString());
    Assert.assertEquals(ResultStatus.OK, result.status());

  }

  /**
   * Test get input docx.
   */
  @Test
  public void test_getInput_docx(){
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Simplify", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "Sample-simplified.docx", params);

    SimplifyDOCX step = new SimplifyDOCX();
    Result result = step.process(model, data, info);
    Assert.assertEquals(ResultStatus.OK, result.status());
    File unpacked = new File (data.directory(),"Sample-unpacked");
    Assert.assertTrue(unpacked.exists());
  }

  /**
   * Test get input directory.
   */
  @Test
  public void test_getInput_directory(){
    try {
      File file = new File("src/test/resources/models/m1/Sample.docx");
      Model model = new Model("m1");
      PackageData data = PackageData.newPackageData("Simplify", file);
      File unpacked = new File(data.directory(), "unpacked");
      ZipUtils.unzip(file, unpacked);

      Map<String, String> params = new HashMap<>();
      StepInfoImpl info = new StepInfoImpl("step-id", "step name", "unpacked", "Sample-simplified.docx", params);

      SimplifyDOCX step = new SimplifyDOCX();
      Result result = step.process(model, data, info);
      Assert.assertEquals(ResultStatus.OK, result.status());
      Assert.assertTrue(unpacked.exists());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  /**
   * Test get output docx.
   */
  @Test
  public void test_getOutput_docx(){
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Simplify", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "Sample-simplified.docx", params);

    SimplifyDOCX step = new SimplifyDOCX();
    Result result = step.process(model, data, info);
    Assert.assertEquals(ResultStatus.OK, result.status());
    File output = new File (data.directory(),"Sample-simplified.docx");
    Assert.assertTrue(output.exists());
  }

  /**
   * Test get output directory.
   */
  @Test
  public void test_getOutput_directory(){
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Simplify", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "simplified", params);

    SimplifyDOCX step = new SimplifyDOCX();
    Result result = step.process(model, data, info);

    Assert.assertEquals(ResultStatus.OK, result.status());
    File output = new File (data.directory(),"simplified/"+ data.id() + "-simplified.docx");
    Assert.assertTrue(output.exists());
  }

  /**
   * Test get output empty.
   */
  @Test
  public void test_getOutput_empty(){
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Simplify", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "", params);

    SimplifyDOCX step = new SimplifyDOCX();
    Result result = step.process(model, data, info);
    Assert.assertEquals(ResultStatus.OK, result.status());
    File output = new File (data.directory(), data.id() + "-simplified.docx");
    Assert.assertTrue(output.exists());
  }

  /**
   * Test process with extra parameters.
   *
   * @throws IOException    the io exception
   * @throws XpathException the xpath exception
   * @throws SAXException   the sax exception
   */
  @Test
  public void test_processWithExtraParameters() throws IOException, XpathException, SAXException {
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Simplify", file);
    String extraParameterValue = "true";
    Map<String, String> params = getExtraParameterMap(extraParameterValue);
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "", params);

    SimplifyDOCX step = new SimplifyDOCX();
    Result result = step.process(model, data, info);
    Assert.assertEquals(ResultStatus.OK, result.status());

    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xml);
    xml.flush();
    xml.close();
    String xmlResult = xml.toString();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      String xpath = "/result/parameters/parameter[@name='" + entry.getKey() + "']/@value";
      XMLAssert.assertXpathEvaluatesTo(extraParameterValue, xpath, xmlResult);
      //XMLUnit.a
      //assertThat(xmlResult).valueByXPath(xpath).isEqualTo(extraParameterValue);
    }
  }

  private Map<String, String> getExtraParameterMap( String value) {
    Map<String, String> params = new HashMap<>();
    params.put("remove-smart-tags", value);
    params.put("remove-content-controls", value);
    params.put("remove-rsid-info", value);
    params.put("remove-permissions", value);
    params.put("remove-proof", value);
    params.put("remove-soft-hyphens", value);
    params.put("remove-last-rendered-page-break", value);
    params.put("remove-bookmarks", value);
    params.put("remove-goback-bookmarks", value);
    params.put("remove-web-hidden", value);
    params.put("remove-language-info", value);
    params.put("remove-comments", value);
    params.put("remove-end-and-foot-notes", value);
    params.put("remove-field-codes", value);
    params.put("replace-nobreak-hyphens", value);
    params.put("replace-tabs", value);
    params.put("remove-font-info", value);
    params.put("remove-paragraph-properties", value);
    return params;
  }
}
