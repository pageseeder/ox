/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TestModel {

  @Before
  public void init() {
    File modelDir = new File("src/test/resources/models");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
  }

  @Test
  public void test_NotNull_For_DefaultModel() {
    Model model = Model.getDefault();
    Assert.assertNotNull(model);
  }

  @Test
  public void test_NotNull_For_SpecifiedModel() {
    Model model = new Model("m1");
    Assert.assertNotNull(model);
  }

  @Test
  public void test_Not_Null_For_PipeLine() throws Exception {
    Model model = new Model("m1");
    Pipeline p = model.getPipeline("sample-pipeline");
    Assert.assertNotNull(p);
  }

  @Test
  public void test_NotNull_For_Step() throws Exception {
    Model model = new Model("m1");
    Pipeline p = model.getPipeline("sample-pipeline");
    Assert.assertNotNull(p);
    StepDefinition step = p.getStep(1);
    Assert.assertNotNull(step);
  }

  @Test
  public void test_duplicated_pipeline() throws Exception {
    try {
      Model model = new Model("duplicatedpipeline");
      model.getPipeline("duplicated-pipeline-id");
      Assert.fail("It should return an IllegarArgumentException.");
    } catch (IllegalArgumentException ex) {
      Assert.assertTrue(ex.getMessage(), true);
    }
  }

  @Test
  public void test_duplicated_step() throws Exception {
    try {
      Model model = new Model("duplicatedstep");
      model.getPipeline("duplicated-step-id");
      Assert.fail("It should return an IllegarArgumentException.");
    } catch (IllegalArgumentException ex) {
      Assert.assertTrue(ex.getMessage(), true);
    }
  }

  @Test
  public void test_listModels() {
    List<Model> models = Model.listModels();
    Assert.assertNotNull(models);
    boolean hasExpectedModels = models.stream()
        .filter(model -> model.name().equals("duplicatedpipeline")
            || model.name().equals("duplicatedstep")
            || model.name().equals("m1")).count() == 3;
    boolean hasNotFakeModelDirectory = models.stream().filter(model -> model.name().equals("fake-model-directory")).count() == 0;
    Assert.assertTrue(hasExpectedModels);
    Assert.assertTrue("Fake model directory should not be listed as it is not a valid model directory.", hasNotFakeModelDirectory);

  }

  @Test
  public void test_extraAttributes(){
    try {
      Model model = new Model("m1");
      model.load();
      Map<String, String> extraAttributes = model.extraAttributes();
      Assert.assertNotNull(extraAttributes);
      Assert.assertTrue(extraAttributes.size() >= 2);
      Assert.assertEquals("word", extraAttributes.get("icon"));
      Assert.assertEquals("another extra", extraAttributes.get("extra-01"));
    } catch (IllegalArgumentException ex) {
      Assert.assertTrue(ex.getMessage(), true);
    }
  }
  @Test
  public void test_extraAttributes_empty(){
    try {
      Model model = new Model("duplicatedstep");
      model.load();
      Map<String, String> extraAttributes = model.extraAttributes();
      Assert.assertNotNull(extraAttributes);
      Assert.assertTrue(extraAttributes.size() == 1);
      //XML Version
      Assert.assertEquals("version", extraAttributes.get("1.0"));
    } catch (IllegalArgumentException ex) {
      Assert.assertTrue(ex.getMessage(), true);
    }
  }
}
