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
public class SchemaValidationTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig.get().setModelsDirectory(modelDir);
  }

  @Test
  public void test_process() throws IOException {
    File file = new File("src/test/resources/models/m1/sample.xml");
    Model model = new Model("m1");
    PackageData data = PackageData.newPackageData("SchemaValidation", file);
    Map<String, String> params = new HashMap<>();
    params.put("input", "sample.xml");
    params.put("schema", "schema-sample.xsd");
    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "model.xml", "model.xml", params);

    SchemaValidation step = new SchemaValidation();
    Result result = step.process(model, data, info);
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    result.toXML(xml);

    xml.flush();
    xml.close();

    System.out.println(xml.toString());
    Assert.assertEquals(ResultStatus.OK, result.status());

  }
}
