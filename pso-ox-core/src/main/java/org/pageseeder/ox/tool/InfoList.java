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

/**
 * info type list. The value has semicolon (';') separated list
 *
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoList extends Info {

  private final List<String> _values;

  public InfoList(String name, Map<String, String> extraAttributes, List<String> values) {
    super(name, InfoType.list, extraAttributes);
    Objects.requireNonNull(values);
    this._values = values;
  }

  public InfoList(String name, List<String> values) {
    super(name, InfoType.list);
    Objects.requireNonNull(values);
    this._values = values;
  }

  @Override
  public String getValue() {
    return String.join(";", this._values);
  }
}
