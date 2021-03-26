/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ciber Cai
 * @since 18 Jul 2016
 */
public class DecompressTest {

  @Test
  public void test_process_no_params() {
    File file = new File("src/test/resources/models/m1/sample.zip");
    Model model = new Model("decompress");
    PackageData data = PackageData.newPackageData("Decompress", file);
    Map<String, String> params = new HashMap<>();
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "sample.zip", "sample", params);

    Decompression step = new Decompression();
    step.process(model, data, info);

    Assert.assertTrue(data.getFile("sample").exists());
    Assert.assertTrue(data.getFile("sample").listFiles().length == 1);

  }

  @Test
  public void test_process_params() {
    File file = new File("src/test/resources/models/m1/sample.zip");
    Model model = new Model("compress");
    PackageData data = PackageData.newPackageData("compress", file);
    Map<String, String> params = new HashMap<>();
    params.put("input", "sample.zip");
    params.put("output", "sample-2");

    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "sample.zip", "sample", params);

    Decompression step = new Decompression();
    step.process(model, data, info);

    Assert.assertTrue(data.getFile("sample-2").exists());
    Assert.assertTrue(data.getFile("sample-2").listFiles().length == 1);

  }
}
