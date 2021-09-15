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
