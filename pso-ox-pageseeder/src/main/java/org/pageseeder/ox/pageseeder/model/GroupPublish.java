/*
 * Copyright 2022 Allette Systems (Australia)
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

import net.pageseeder.app.simple.pageseeder.service.PublishService;

import java.util.HashMap;

/**
 * The type Group publish.
 *
 * @author asantos
 * @since 12 July 2022
 */
public class GroupPublish {

  /**
   * How to run for archived projects. There is one example that the group is 'archive-subscribers-subscriber09124104'
   * and project is just 'subscribers'
   */
  private String project;
  /**
   * the group name project-group or archive-project-group
   */
  private String group;
  private String member;
  private String target;
  private PublishService.Type type;
  private PublishService.LogLevel logLevel;
  private HashMap<String, String> parameters;

  /**
   * Instantiates a new Group publish.
   *
   * How to run for archived projects. There is one example that the group is 'archive-subscribers-subscriber09124104'
   * and project is just 'subscribers'
   *
   * @param project    the project
   * @param group      the group name project-group or archive-project-group
   * @param member     the member
   * @param target     the target
   * @param type       the type
   * @param logLevel   the log level
   * @param parameters the parameters
   */
  public GroupPublish(String project, String group, String member, String target, String type, String logLevel, HashMap<String, String> parameters) {
    this.project = project;
    this.group = group;
    this.member = member;
    this.target = target;
    this.type = PublishService.Type.valueOf(type.toUpperCase());
    this.logLevel = PublishService.LogLevel.valueOf(logLevel.toUpperCase());
    this.parameters = parameters;
  }

  /**
   * Gets project.
   *
   * @return the project
   */
  public String getProject() {
    return project;
  }

  /**
   * Gets group.
   *
   * @return the group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Gets member.
   *
   * @return the member
   */
  public String getMember() {
    return member;
  }

  /**
   * Gets target.
   *
   * @return the target
   */
  public String getTarget() {
    return target;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public PublishService.Type getType() { return type; }

  /**
   * Gets log level.
   *
   * @return the log level
   */
  public PublishService.LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Gets parameters.
   *
   * @return the parameters
   */
  public HashMap<String, String> getParameters() { return parameters; }

}
