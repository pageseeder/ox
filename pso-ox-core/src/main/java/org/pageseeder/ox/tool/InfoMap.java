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
package org.pageseeder.ox.tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Info type map. The value contains a map, each item of this map is separated by semicolon (';'),
 * the key and the value is separated by two points (':') and value also can contains a comma separated list.
 *
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoMap extends Info {

  private final Map<String, List<String>> _mapValues;

  /**
   * Instantiates a new Info map.
   *
   * @param name            the name
   * @param extraAttributes the extra attributes
   * @param valuesMap       the values map
   */
  public InfoMap(String name, Map<String, String> extraAttributes, Map<String, List<String>> valuesMap) {
    super(name, InfoType.map, extraAttributes);
    Objects.requireNonNull(valuesMap);
    this._mapValues = valuesMap;
  }

  /**
   * Instantiates a new Info map.
   *
   * @param name      the name
   * @param valuesMap the values map
   */
  public InfoMap(String name, Map<String, List<String>> valuesMap) {
    super(name, InfoType.map);
    Objects.requireNonNull(valuesMap);
    this._mapValues = valuesMap;
  }

  @Override
  public String getValue() {
    return this._mapValues.entrySet()
        .stream()
        .map(e -> e.getKey() + ":" + String.join(",", e.getValue()))
        .collect(Collectors.joining(";"));
  }
}
