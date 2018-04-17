/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

/**
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class TransformationTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void test_process() throws IOException {
    File file = new File("src/test/resources/models/m1/sample.xml");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Transformation", file);
    Map<String, String> params = new HashMap<>();
    params.put("input", "sample.xml");
    params.put("xsl", "xslt-sample.xsl");
    params.put("_xslt-indent", "no");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "myinput.xml", "myoutput.xml", params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xml);

    xml.flush();
    xml.close();

    System.out.println(xml.toString());
    Assert.assertEquals(ResultStatus.OK, result.status());
  }
  
  @Test
  public void test_processGlobPattern() throws IOException {
    File file = new File("src/test/resources/models/m1/sample.xml");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Transformation", file);
    Map<String, String> params = new HashMap<>();
    params.put("input", "*.xml");
    params.put("xsl", "xslt-sample.xsl");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "myinput.xml", "myoutput.xml", params);

    Transformation step = new Transformation();
    Result result = step.process(model, data, info);
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xml);

    xml.flush();
    xml.close();

    System.out.println(xml.toString());
    Assert.assertEquals(ResultStatus.OK, result.status());
  }
}
