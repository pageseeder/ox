/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;

/**
 * @author Ciber Cai
 * @since 17 Jun 2016
 */
public class OXConfigTest {

  @Test
  public void test_object() {
    Assert.assertNotNull(OXConfig.get());
  }

  @Test
  public void test_ox_temp_folder() {
    Assert.assertNotNull(OXConfig.getOXTempFolder());
    Assert.assertTrue(OXConfig.getOXTempFolder().exists());
    Assert.assertTrue(OXConfig.getOXTempFolder().isDirectory());
  }

  @Test
  public void test_model_directory() {
    File modelDir = new File("test/model");
    OXConfig.get().setModelsDirectory(modelDir);
    Assert.assertNotNull(OXConfig.get().getModelsDirectory());
    Assert.assertEquals(modelDir, OXConfig.get().getModelsDirectory());

  }

}
