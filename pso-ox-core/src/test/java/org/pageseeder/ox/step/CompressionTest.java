/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;

/**
 * @author Ciber Cai
 * @since 18 Jul 2016
 */
public class CompressionTest {

  @Test
  public void test_compress_process_no_params() {
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("compress");
    PackageData data = PackageData.newPackageData("Compress", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "output.zip", params);

    Compression step = new Compression();
    step.process(model, data, info);

    Assert.assertTrue(data.getFile("output.zip").exists());
    Assert.assertTrue(data.getFile("output.zip").length() > 1);

  }

  @Test
  public void test_compress_process_params() {
    File file = new File("src/test/resources/models/m1/Sample.docx");
    Model model = new Model("compress");
    PackageData data = PackageData.newPackageData("compress", file);
    Map<String, String> params = new HashMap<>();
    params.put("input", "Sample.docx");
    params.put("output", "Sample.zip");

    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "Sample.docx", "output.zip", params);

    Compression step = new Compression();
    step.process(model, data, info);

    Assert.assertTrue(data.getFile("Sample.zip").exists());
    Assert.assertTrue(data.getFile("Sample.zip").length() > 1);

  }
}
