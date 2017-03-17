/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.core;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;

public class ModelTest {

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

}
