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
package org.pageseeder.ox.client;

import org.pageseeder.berlioz.GlobalSettings;

import java.util.Properties;

/**
 * Provide all necessary information for OX Server.
 *
 * <pre>{@code
 *   <ox-server url="http://[ox server]" process="/process" status="/status.xml" model="model-name"
 *              username="berlioz" password="OB1:[obfuscated password]" />}
 * </pre>
 *
 *
 * @author Carlos Cabral
 * @since 2 December 2015
 */
public class OxProperties {

  /** The properties. */
  private static Properties PROPERTIES = null;

  /**
   * Instantiates a new ox properties.
   */
  public OxProperties() {
    if (PROPERTIES == null || PROPERTIES.size()==0) {
      PROPERTIES = GlobalSettings.getNode("ox-server");
    }
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public String getURL() {
    return PROPERTIES.getProperty("url");
  }

  /**
   * Gets the status url.
   *
   * @param jobId the job id
   * @return the complete status service url
   */
  public String getStatusURL(String jobId) {
    return getURL() + PROPERTIES.getProperty("status") + "?id=" + jobId;
  }

  /**
   * Gets the process url.
   *
   * @return the complete process url
   */
  public String getProcessURL() {
    return getProcessURL(getModel());
  }

  /**
   * Gets the process url.
   *
   * @param model the model
   * @return the complete process url
   */
  public String getProcessURL(String model) {
    return getURL() + PROPERTIES.getProperty("process") + "?model=" + model ;
  }

  /**
   * Gets the download url.
   *
   * @param filePath the file path
   * @return the complete download url
   */
  public String getDownloadURL(String filePath) {
    return getURL() + PROPERTIES.getProperty("download") + "/" + filePath;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return PROPERTIES.getProperty("username");
  }

  /**
   * Gets the password.
   *
   * @return the password
   */
  public String getPassword() {
    return PROPERTIES.getProperty("password");
  }

  /**
   * Gets the model.
   *
   * @return the model
   */
  public String getModel() {
    return PROPERTIES.getProperty("model");
  }


  /**
   * Checks if is configured.
   *
   * @return true, if is configured
   */
  public boolean isConfigured() {
    return PROPERTIES.size() > 0 && getURL()!=null && !getURL().isEmpty();
  }

}
