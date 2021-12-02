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
package org.pageseeder.ox.callback;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class TransformTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/callback/transform");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void testGetOutputFile() {
    Transform transform = getTransform();

    PackageData data = PackageData.newPackageData("test", null);
    String outputName = "/output/sample.xml";

    Map<String, String> params = new HashMap<>();
    params.put("callback-output", outputName);
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", "sample.xml", params);
    try {
      File output = transform.getOutputFile(data, info);
      Assert.assertNotNull(output);
      Assert.assertTrue(output.exists());
    } catch (IOException ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testGetXSLFile() {
    Transform transform = getTransform();
    Model model = new Model("test");
    PackageData data = PackageData.newPackageData(model.name(), null);

    Map<String, String> params = new HashMap<>();
    params.put("callback-xsl", "sample.xsl");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", "sample.xml", params);

    File xslt = transform.getXSLFile(model, data, info);
    Assert.assertNotNull(xslt);
    Assert.assertTrue(xslt.exists());

  }


  private Transform getTransform () {
    return new Transform() {
      @Override
      public void process(Model model, PackageData data, Result result, StepInfo info) {
        //nothing
      }
    };
  }
}
