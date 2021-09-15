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

/**
 * <p>A configuration of OX.</p>
 *
 * @author Christophe Lauret
 * @since  8 November 2013
 */
public class OXConfig {

  /** The folder name where the files will be uploaded. */
  public final static String TEMP_UPLOAD_FOLDER_NAME = "upload";
  /**
   * Singleton instance.
   */
  private final static OXConfig SINGLETON = new OXConfig();

  /**
   *
   */
  private OXConfig() {}

  /**
   * The directory containing the models
   */
  private File models = null;

  /**
   *
   * @return The directory containing the models
   */
  public File getModelsDirectory() {
    return this.models;
  }

  /**
   * @param models The directory containing the models
   */
  public void setModelsDirectory(File models) {
    this.models = models;
  }

  /**
   * @return the singleton
   */
  public static OXConfig get() {
    return SINGLETON;
  }

  /**
   * @return the Ox template folder
   */
  public static File getOXTempFolder() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File temp = new File(tempDir, "/OX/allette.app.tmp");
    if (!temp.exists()) {
      temp.mkdirs();
    }
    return temp;
  }

  /**
   * @return the Ox template folder
   */
  public static File getOXTempUploadFolder() {
    File tempDir = getOXTempFolder();
    File temp = new File(tempDir, TEMP_UPLOAD_FOLDER_NAME);
    if (!temp.exists()) {
      temp.mkdirs();
    }
    return temp;
  }
}
