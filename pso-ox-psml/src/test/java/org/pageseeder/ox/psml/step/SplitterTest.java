/*
 * Copyright 2024 Allette Systems (Australia)
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
package org.pageseeder.ox.psml.step;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.Pipeline;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.core.StepDefinition;
import org.pageseeder.ox.step.StepSimulator;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.xml.utils.FilesComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ccabral
 * @since 20 June 2024
 */
public class SplitterTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/psml/step/splitter/models");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void testSingle() {
    File input = new File("src/test/resources/org/pageseeder/ox/psml/step/splitter/input/split_source_single.psml");
    Model model = new Model("test");
    Pipeline pipeline = model.getPipeline("test-pipeline");
    Map<String, String> requestParameter = new HashMap<>();
    StepSimulator simulator = new StepSimulator(model.name(), input, requestParameter);
    StepDefinition stepDefinition = pipeline.getStep("splitter");
    DefaultResult result = (DefaultResult) simulator.process(stepDefinition.getStep(), null, null, stepDefinition.name(), stepDefinition.parameters());

    Assert.assertNotNull(result);
    Assert.assertEquals(ResultStatus.OK, result.status());

    File expectedResultBase = new File("src/test/resources/org/pageseeder/ox/psml/step/splitter/output/single");
    File resultBase = new File(simulator.getData().directory(), "myoutput");
    List<File> filesToIgnore = new ArrayList<>();
    FilesComparator comparator = new FilesComparator(expectedResultBase, resultBase, filesToIgnore);
    comparator.compare();

  }
}
