/*
 * Copyright 2025 Allette Systems (Australia)
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
import org.pageseeder.ox.core.*;
import org.pageseeder.ox.psml.validation.ValidationStepResult;
import org.pageseeder.ox.step.StepSimulator;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.xml.utils.FilesComparator;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ccabral
 * @since 21 July 2025
 */
public class ValidateTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/psml/step/validate/models");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void testSingle() {
    try {
      File input = new File("src/test/resources/org/pageseeder/ox/psml/step/validate/input");
      Model model = new Model("test");
      Pipeline pipeline = model.getPipeline("test-pipeline");
      Map<String, String> requestParameter = new HashMap<>();
      StepSimulator simulator = new StepSimulator(model.name(), input, requestParameter);

      StepDefinition stepDefinition = pipeline.getStep("validate-char");
      ValidationStepResult result = (ValidationStepResult) simulator.process(stepDefinition.getStep(), null, null, stepDefinition.name(), stepDefinition.parameters());
      Assert.assertNotNull(result);
      Assert.assertEquals(ResultStatus.WARNING, result.status());

      stepDefinition = pipeline.getStep("validate-wf");
      result = (ValidationStepResult) simulator.process(stepDefinition.getStep(), null, null, stepDefinition.name(), stepDefinition.parameters());
      Assert.assertNotNull(result);
      Assert.assertEquals(ResultStatus.OK, result.status());

      stepDefinition = pipeline.getStep("validate-xsd");
      result = (ValidationStepResult) simulator.process(stepDefinition.getStep(), null, null, stepDefinition.name(), stepDefinition.parameters());
      Assert.assertNotNull(result);
      Assert.assertEquals(ResultStatus.ERROR, result.status());

      stepDefinition = pipeline.getStep("validate-sch");
      result = (ValidationStepResult) simulator.process(stepDefinition.getStep(), null, null, stepDefinition.name(), stepDefinition.parameters());
      Assert.assertNotNull(result);
      Assert.assertEquals(ResultStatus.OK, result.status());

    } catch (Exception e) {
      System.out.println(e.getMessage());
      Assert.fail(e.getMessage());
    }

  }
}
