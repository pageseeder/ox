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
package org.pageseeder.ox.berlioz.generator;

import org.pageseeder.berlioz.content.ContentGenerator;

/**
 * A basic generator for LIXI
 * @author Ciber Cai
 * @version 19 February 2014
 */
public abstract class BasicGenerator implements ContentGenerator {

  protected static boolean validType(String name) {
    if ("schema".equals(name) || "xsl".equals(name)) {
      return true;
    } else {
      return false;
    }
  }

}
