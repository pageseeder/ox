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

import java.util.Map;
import java.util.Objects;

/**
 * The type Info string.
 *
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoString extends Info {

  private final String _value;

  /**
   * Instantiates a new Info string.
   *
   * @param name            the name
   * @param extraAttributes the extra attributes
   * @param value           the value
   */
  public InfoString(String name,  Map<String, String> extraAttributes, String value) {
    super(name, InfoType.string, extraAttributes);
    Objects.requireNonNull(value);
    this._value = value;
  }

  /**
   * Instantiates a new Info string.
   *
   * @param name  the name
   * @param value the value
   */
  public InfoString(String name,  String value) {
    super(name, InfoType.string);
    Objects.requireNonNull(value);
    this._value = value;
  }

  @Override
  public String getValue() {
    return this._value;
  }
}
