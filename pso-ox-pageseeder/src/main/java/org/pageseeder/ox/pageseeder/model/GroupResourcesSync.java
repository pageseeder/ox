/*
 * Copyright 2024 Allette Systems (Australia)
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
package org.pageseeder.ox.pageseeder.model;

/**
 * The Group Resources Sync.
 *
 * @author ccabral
 * @since 26 November 2024
 */
public class GroupResourcesSync {

  /**
   * It is required and only accepts teh project name.
   */
  private final String fromProjectName;
  /**
   * It is optional. If empty it gets from the step.
   */
  private final String fromPSConfigName;
  /**
   * It is optional. The name of an existing perspective to export.
   */
  private final String fromPerspective;

  /**
   * It is required and only accepts teh project name.
   */
  private final String toProjectName;
  /**
   * It is optional. If empty it gets from the step.
   */
  private final String toPSConfigName;

  /**
   * Instantiates a new Group resources sync.
   *
   * @param fromProjectName  the from project name
   * @param fromPSConfigName the from ps config name
   * @param fromPerspective  the from perspective
   * @param toProjectName    the to project name
   * @param toPSConfigName   the to ps config name
   */
  public GroupResourcesSync(String fromProjectName, String fromPSConfigName, String fromPerspective, String toProjectName, String toPSConfigName) {
    this.fromProjectName = fromProjectName;
    this.fromPSConfigName = fromPSConfigName;
    this.fromPerspective = fromPerspective;
    this.toProjectName = toProjectName;
    this.toPSConfigName = toPSConfigName;
  }

  /**
   * Gets from project name.
   *
   * @return the from project name
   */
  public String getFromProjectName() {
    return fromProjectName;
  }

  /**
   * Gets from ps config name.
   *
   * @return the from ps config name
   */
  public String getFromPSConfigName() {
    return fromPSConfigName;
  }

  /**
   * Gets from perspective.
   *
   * @return the from perspective
   */
  public String getFromPerspective() {
    return fromPerspective;
  }

  /**
   * Gets to project name.
   *
   * @return the to project name
   */
  public String getToProjectName() {
    return toProjectName;
  }

  /**
   * Gets to ps config name.
   *
   * @return the to ps config name
   */
  public String getToPSConfigName() {
    return toPSConfigName;
  }
}
