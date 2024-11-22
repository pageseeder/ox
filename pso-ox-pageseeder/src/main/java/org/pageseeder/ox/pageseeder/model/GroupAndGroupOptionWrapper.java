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

import net.pageseeder.app.simple.pageseeder.model.GroupOptions;
import org.pageseeder.bridge.model.PSGroup;

/**
 * The type Group and group option wrapper.
 *
 * @author ccabral
 * @since 21 November 2024
 */
public class GroupAndGroupOptionWrapper {

  private final PSGroup group;
  private final GroupOptions groupOptions;

  /**
   * Instantiates a new Group and group option wrapper.
   *
   * @param group        the group
   * @param groupOptions the group options
   */
  public GroupAndGroupOptionWrapper(PSGroup group, GroupOptions groupOptions) {
    this.group = group;
    this.groupOptions = groupOptions;
  }

  /**
   * Gets group.
   *
   * @return the group
   */
  public PSGroup getGroup() {
    return this.group;
  }

  /**
   * Gets group options.
   *
   * @return the group options
   */
  public GroupOptions getGroupOptions() {
    return this.groupOptions;
  }
}
