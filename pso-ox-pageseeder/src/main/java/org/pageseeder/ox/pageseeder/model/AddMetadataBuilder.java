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

import org.pageseeder.bridge.psml.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Add metadata.
 *
 * @author ccabral
 * @since 11 January 2023
 */
public class AddMetadataBuilder {
  private Long uriid;
  private String title;
  private String description;
  private List<String> labels;
  private List<Property> properties;

  /**
   * Uriid add metadata builder.
   *
   * @param uriid the uriid
   * @return the add metadata builder
   */
  public AddMetadataBuilder uriid(Long uriid) {
    this.uriid = uriid;
    return this;
  }

  /**
   * Title add metadata builder.
   *
   * @param title the title
   * @return the add metadata builder
   */
  public AddMetadataBuilder title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Description add metadata builder.
   *
   * @param description the description
   * @return the add metadata builder
   */
  public AddMetadataBuilder description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Labels add metadata builder.
   *
   * @param labels the labels
   * @return the add metadata builder
   */
  public AddMetadataBuilder labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  /**
   * Properties add metadata builder.
   *
   * @param properties the properties
   * @return the add metadata builder
   */
  public AddMetadataBuilder properties(List<Property> properties) {
    this.properties = properties;
    return this;
  }

  /**
   * Property add metadata builder.
   *
   * @param property the property
   * @return the add metadata builder
   */
  public AddMetadataBuilder property(Property property) {
    if (this.properties == null) {
      this.properties = new ArrayList<>();
    }
    this.properties.add(property);
    return this;
  }

  /**
   * Build add metadata.
   *
   * @return the add metadata
   */
  public AddMetadata build (){
    return new AddMetadata(uriid, title, description, labels, properties);
  }
}
