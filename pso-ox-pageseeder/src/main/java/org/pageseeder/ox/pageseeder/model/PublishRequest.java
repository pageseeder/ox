/*
 * Copyright 2026 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.pageseeder.model;

import net.pageseeder.app.simple.pageseeder.service.PublishService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates parameters for PageSeeder Group and URI publish operations.
 *
 * @author Carlos Cabral
 * @since 1 October 2026
 */
public final class PublishRequest {

  /**
   * How to run for archived projects. There is one example that the group is 'archive-subscribers-subscriber09124104'
   * and project is just 'subscribers'
   */
  private final String project;
  /**
   * the group name project-group or archive-project-group
   */
  private final String group;
  private final String member;
  private final String target;
  private final PublishService.Type type;
  private final PublishService.LogLevel logLevel;
  private final String uriId;
  private final String documentType;
  private final Map<String, String> parameters;

  /**
   * Instantiates a new Group publish.
   *
   * How to run for archived projects. There is one example that the group is 'archive-subscribers-subscriber09124104'
   * and project is just 'subscribers'
   **/
  private PublishRequest(Builder builder) {
    this.project = builder.project;
    this.group = builder.group;
    this.member = builder.member;
    this.target = builder.target;
    this.type = builder.type;
    this.logLevel = builder.logLevel;
    this.uriId = builder.uriId;
    this.documentType = builder.documentType;
    this.parameters = Collections.unmodifiableMap(new HashMap<>(builder.parameters));
  }

  /**
   * Gets the PageSeeder project name.
   *
   * @return the project name
   */
  public String getProject() {
    return this.project;
  }

  /**
   * Gets the group name (e.g., 'project-group' or 'archive-project-group').
   *
   * @return the group name
   */
  public String getGroup() {
    return this.group;
  }

  /**
   * Gets the member username override (optional).
   *
   * @return the member username, or empty string if not provided
   */
  public String getMember() {
    return this.member;
  }

  /**
   * Gets the publish target target folder/script.
   *
   * @return the publish target
   */
  public String getTarget() {
    return this.target;
  }

  /**
   * Gets the publish job execution type.
   *
   * @return the publish type
   */
  public PublishService.Type getType() {
    return this.type;
  }

  /**
   * Gets the publishing log level.
   *
   * @return the log level, or {@code null} if default
   */
  public PublishService.LogLevel getLogLevel() {
    return this.logLevel;
  }

  /**
   * Gets the URI ID for URI publishing.
   *
   * @return the URI ID, or empty string if group publishing
   */
  public String getUriId() {
    return this.uriId;
  }

  /**
   * Gets the document type for URI publishing.
   *
   * @return the document type, or empty string if not applicable
   */
  public String getDocumentType() {
    return this.documentType;
  }

  /**
   * Gets custom script parameters.
   *
   * @return an unmodifiable map of parameters
   */
  public Map<String, String> getParameters() {
    return this.parameters;
  }

  /**
   * Builder pattern for constructing {@link PublishRequest} instances.
   */
  public static class Builder {
    private String project = "";
    private String group = "";
    private String member = "";
    private String target = "";
    private PublishService.Type type = PublishService.Type.PROCESS;
    private PublishService.LogLevel logLevel = PublishService.LogLevel.INFO;
    private String uriId = "";
    private String documentType = "";
    private Map<String, String> parameters = new HashMap<>();

    public Builder project(String project) {
      this.project = Objects.requireNonNullElse(project, "");
      return this;
    }

    public Builder group(String group) {
      this.group = Objects.requireNonNullElse(group, "");
      return this;
    }

    public Builder member(String member) {
      this.member = Objects.requireNonNullElse(member, "");
      return this;
    }

    public Builder target(String target) {
      this.target = Objects.requireNonNullElse(target, "");
      return this;
    }

    public Builder type(PublishService.Type type) {
      if (type != null) this.type = type;
      return this;
    }

    public Builder type(String typeStr) {
      if (typeStr != null && !typeStr.isBlank()) {
        try {
          this.type = PublishService.Type.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
      }
      return this;
    }

    public Builder logLevel(PublishService.LogLevel logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    public Builder logLevel(String levelStr) {
      if (levelStr != null && !levelStr.isBlank()) {
        try {
          this.logLevel = PublishService.LogLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
      }
      return this;
    }

    public Builder uriId(String uriId) {
      this.uriId = Objects.requireNonNullElse(uriId, "");
      return this;
    }

    public Builder documentType(String documentType) {
      this.documentType = Objects.requireNonNullElse(documentType, "");
      return this;
    }

    public Builder parameters(Map<String, String> parameters) {
      if (parameters != null) {
        this.parameters.putAll(parameters);
      }
      return this;
    }

    public PublishRequest build() {
      return new PublishRequest(this);
    }
  }
}