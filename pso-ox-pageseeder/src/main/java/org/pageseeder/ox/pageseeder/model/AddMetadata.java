/*
 * Copyright 2023 Allette Systems (Australia)
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

import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.psml.Property;

import java.util.Collections;
import java.util.List;

/**
 * The type Add metadata.
 *
 * @author ccabral
 * @since 11 January 2023
 */
public class AddMetadata {
  private final Long uriid;
  private final String title;
  private final String description;
  private final List<String> labels;
  private final List<Property> properties;

  /**
   * Instantiates a new Add metadata.
   *
   * @param uriid       the uriid
   * @param title       the title
   * @param description the description
   * @param labels      the labels
   * @param properties  the properties
   */
  public AddMetadata(Long uriid, String title, String description, List<String> labels, List<Property> properties) {
    this.uriid = uriid;
    this.title = title;
    this.description = description;
    this.labels = labels;
    this.properties = properties;
  }

  /**
   * Gets uriid.
   *
   * @return the uriid
   */
  public Long getUriid() {
    return uriid;
  }

  /**
   * Gets title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets labels.
   *
   * @return the labels
   */
  public List<String> getLabels() {
    return Collections.unmodifiableList(labels);
  }

  /**
   * Gets properties.
   *
   * @return the properties
   */
  public List<Property> getProperties() {
    return Collections.unmodifiableList(properties);
  }
}
