/*
 * Copyright 2025 Allette Systems (Australia)
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
package org.pageseeder.ox.inspector;

import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos
 * @since 21 May 2025
 */
public class NoFileInspector implements PackageInspector {

  private final static Logger LOGGER = LoggerFactory.getLogger(NoFileInspector.class);

  @Override
  public String getName() {
    return "ox-no-file-inspector";
  }

  @Override
  public boolean supportsMediaType(String mediatype) {
    // support everything
    return "no-file".equalsIgnoreCase(mediatype);
  }

  @Override
  public void inspect(PackageData pack) {
    LOGGER.debug("No file inspector");
    if (pack.getOriginal() != null) {
      throw new IllegalArgumentException("In this case it is not allowed to have a file.");
    }
  }
}
