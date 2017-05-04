/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;

/**
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class CopyTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
  }
  
  @Test
  public void test_copy_file() {
    File file = new File("src/test/resources/models/m1/Sample.html");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("TEst", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "Sample.docx.copy", params);

    Copy step1 = new Copy();
    step1.process(model, data, info);

    Assert.assertTrue(data.getFile("Sample.docx.copy").exists());
    Assert.assertTrue(data.getFile("Sample.docx.copy").length() > 1);

    params.put("to", "sample-copy.docx");
    info = new StepInfoImpl("step-id", "step name", "Sample.docx", "Sample.docx.copy", params);

    Copy step2 = new Copy();
    step2.process(model, data, info);

    Assert.assertTrue(data.getFile("sample-copy.docx").exists());
    Assert.assertTrue(data.getFile("sample-copy.docx").length() > 1);

  }

  @Test
  public void test_copy_directory() {
    File file = new File("src/test/resources/models/m1");
    Model model = new Model("compress");
    PackageData data = PackageData.newPackageData("copy", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "m1", "m2", params);

    Copy step1 = new Copy();
    step1.process(model, data, info);

    Assert.assertTrue(data.getFile("m2").exists());
    Assert.assertTrue(data.getFile("m2").listFiles().length > 1);

    params.put("to", "m3");
    info = new StepInfoImpl("step-id", "step name", "Sample.docx", "Sample.docx.copy", params);

    Copy step2 = new Copy();
    step2.process(model, data, info);

    Assert.assertTrue(data.getFile("m2").exists());
    Assert.assertTrue(data.getFile("m2").listFiles().length > 1);

  }
}
