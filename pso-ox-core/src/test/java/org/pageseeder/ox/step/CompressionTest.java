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
 * The type Compression test.
 *
 * @author Ciber Cai
 * @since 18 Jul 2016
 */
public class CompressionTest {

  /**
   * Test compress process no params.
   */
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

  /**
   * Test compress process params.
   */
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
