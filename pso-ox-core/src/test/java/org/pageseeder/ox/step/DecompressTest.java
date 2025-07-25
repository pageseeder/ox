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

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Decompress test.
 *
 * @author Ciber Cai
 * @since 18 Jul 2016
 */
public class DecompressTest {

  /**
   * Test process no params.
   */
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

  /**
   * Test process params.
   */
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
