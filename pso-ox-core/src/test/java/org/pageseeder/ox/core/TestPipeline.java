/*
 * Copyright (c) 1999-2015 Allette systems pty. ltd.
 */
package org.pageseeder.ox.core;

import java.io.File;
import java.io.IOException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.SAXException;

/**
 * @author Ciber Cai
 * @since 18 October 2016
 */
public class TestPipeline {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
  }

  @Test
  public void toXML() throws IOException, XpathException, SAXException {
    Pipeline pipeline1 = new Pipeline("id","name", "type");
    Assert.assertEquals("id", pipeline1.id());
    Assert.assertEquals("name", pipeline1.name());
    Assert.assertEquals("type", pipeline1.accepts());
    Assert.assertEquals("name", pipeline1.description());
    Assert.assertEquals(false, pipeline1.isDefault());

    XMLWriter xml1 = new XMLStringWriter(NamespaceAware.No);
    pipeline1.toXML(xml1);
    xml1.flush();
    xml1.close();
    XMLAssert.assertXpathEvaluatesTo("id", "pipeline/@id", xml1.toString());
    XMLAssert.assertXpathEvaluatesTo("name", "pipeline/@name", xml1.toString());
    XMLAssert.assertXpathEvaluatesTo("name", "pipeline/@description", xml1.toString());
    XMLAssert.assertXpathEvaluatesTo("type", "pipeline/@accepts", xml1.toString());
    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/@default", xml1.toString());

    Pipeline pipeline2 = new Pipeline("id", "name", "type", "description", true);
    XMLAssert.assertXpathEvaluatesTo("id", "pipeline/@id", xml1.toString());
    Assert.assertEquals("id", pipeline2.id());
    Assert.assertEquals("name", pipeline2.name());
    Assert.assertEquals("type", pipeline2.accepts());
    Assert.assertEquals("description", pipeline2.description());
    Assert.assertEquals(true, pipeline2.isDefault());

    XMLWriter xml2 = new XMLStringWriter(NamespaceAware.No);
    pipeline2.toXML(xml2);
    xml2.flush();
    xml2.close();

    XMLAssert.assertXpathEvaluatesTo("id", "pipeline/@id", xml1.toString());
    XMLAssert.assertXpathEvaluatesTo("name", "pipeline/@name", xml2.toString());
    XMLAssert.assertXpathEvaluatesTo("description", "pipeline/@description", xml2.toString());
    XMLAssert.assertXpathEvaluatesTo("type", "pipeline/@accepts", xml2.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/@default", xml2.toString());

  }

  @Test
  public void pipelineHanlerSpecific() throws Exception {
    Model model = new Model("m1");
    Assert.assertNotNull(model);
    boolean loaded = model.load();
    Assert.assertTrue(loaded);

    Pipeline pipeline = model.getPipeline("sample-pipeline");
    Assert.assertNotNull(pipeline);
    Assert.assertEquals("sample-pipeline", pipeline.id());
    Assert.assertEquals("Sample Pipeline", pipeline.name());
    Assert.assertEquals("application/zip", pipeline.accepts());
    Assert.assertEquals("The sample pipeline will decompress the file uploaded", pipeline.description());
    Assert.assertEquals(false, pipeline.isDefault());

    XMLWriter xml = new XMLStringWriter(NamespaceAware.No);
    pipeline.toXML(xml);
    xml.flush();
    xml.close();
    
    XMLAssert.assertXpathEvaluatesTo("sample-pipeline", "pipeline/@id", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("Sample Pipeline", "pipeline/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("The sample pipeline will decompress the file uploaded", "pipeline/@description", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("application/zip", "pipeline/@accepts", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("false", "pipeline/@default", xml.toString());
  }

  @Test
  public void pipelineHanlerDefault() throws Exception {
    Model model = new Model("m1");
    Assert.assertNotNull(model);
    boolean loaded = model.load();
    Assert.assertTrue(loaded);

    Pipeline pipeline = model.getPipelineDefault();
    Assert.assertNotNull(pipeline);
    Assert.assertEquals("default-pipeline", pipeline.id());
    Assert.assertEquals("Default Pipeline", pipeline.name());
    Assert.assertEquals("application/zip", pipeline.accepts());
    Assert.assertEquals("The default pipeline will decompress the file uploaded", pipeline.description());
    Assert.assertEquals(true, pipeline.isDefault());

    XMLWriter xml = new XMLStringWriter(NamespaceAware.No);
    pipeline.toXML(xml);
    xml.flush();
    xml.close();
    
    XMLAssert.assertXpathEvaluatesTo("default-pipeline", "pipeline/@id", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("Default Pipeline", "pipeline/@name", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("The default pipeline will decompress the file uploaded", "pipeline/@description", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("application/zip", "pipeline/@accepts", xml.toString());
    XMLAssert.assertXpathEvaluatesTo("true", "pipeline/@default", xml.toString());
  }
  
}
