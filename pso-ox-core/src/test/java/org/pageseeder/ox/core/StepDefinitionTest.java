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
package org.pageseeder.ox.core;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.File;

/**
 * @author Ciber Cai
 * @since 18 Jul 2016
 */
public class StepDefinitionTest {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
  }

  @Test
  public void testHandler() throws Exception {
    Model model = new Model("m1");
    Assert.assertNotNull(model);
    boolean loaded = model.load();
    Assert.assertTrue(loaded);

    Pipeline pipeline = model.getPipeline("sample-pipeline");
    Assert.assertNotNull(pipeline);
    Assert.assertEquals("sample-pipeline", pipeline.id());

    XMLWriter xml = new XMLStringWriter(NamespaceAware.No);
    pipeline.toXML(xml);
    xml.flush();
    xml.close();
    System.out.println(xml.toString());
    XMLAssert.assertXpathEvaluatesTo("sample-pipeline", "pipeline/@id", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("decompress-file", "pipeline/step[1]/@id", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("decompress file", "pipeline/step[1]/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("org.pageseeder.ox.step.Decompression", "pipeline/step[1]/@class", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/step[1]/@async", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/step[1]/@downloadable", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/step[1]/@viewable", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/step[1]/@fail-on-error", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/step[1]/@wait", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("file-step1", "pipeline/step[1]/input[1]/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("text-step1", "pipeline/step[1]/input[1]/text()", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("config-step1", "pipeline/step[1]/input[2]/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("config-step1.txt", "pipeline/step[1]/input[2]/@default-value", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("text2-step1", "pipeline/step[1]/input[2]/text()", xml.toString());

    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/step[2]/@async", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/step[2]/@downloadable", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/step[2]/@viewable", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/step[2]/@fail-on-error", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/step[2]/@wait", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("file-step2", "pipeline/step[2]/input[1]/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("text-step2", "pipeline/step[2]/input[1]/text()", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("config-step2", "pipeline/step[2]/input[2]/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("config-step2.txt", "pipeline/step[2]/input[2]/@default-value", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("text2-step2", "pipeline/step[2]/input[2]/text()", xml.toString());
  }


}
