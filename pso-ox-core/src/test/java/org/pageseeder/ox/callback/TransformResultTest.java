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

import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;
import org.pageseeder.ox.tool.DefaultResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class TransformResultTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/callback/transform-result");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void testProcess() {
    Transform transform = new TransformResult();
    String outputName = "/output/sample.xml";
    Model model = new Model("test");
    PackageData data = PackageData.newPackageData(model.name(), null);

    Map<String, String> params = new HashMap<>();
    params.put("callback-output", outputName);
    params.put("callback-xsl", "sample.xsl");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "", "sample.xml", params);
    DefaultResult result = new DefaultResult(model, data, info, null);
    transform.process(model, data, result, info);

  }
}
