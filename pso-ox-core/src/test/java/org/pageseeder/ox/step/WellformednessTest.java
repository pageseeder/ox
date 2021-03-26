/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class WellformednessTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void test_process() throws IOException {
    File file = new File("src/test/resources/models/m1/sample.xml");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("Wellformness", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "sample.xml", "myoutput.xml", params);

    Wellformedness step = new Wellformedness();
    Result result = step.process(model, data, info);
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xml);

    xml.flush();
    xml.close();

    System.out.println(xml.toString());
    Assert.assertEquals(ResultStatus.OK, result.status());

  }
}
