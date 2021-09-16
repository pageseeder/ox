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
package org.pageseeder.ox.api;

import org.pageseeder.ox.core.InspectorService;
import org.pageseeder.ox.core.PackageData;

/**
 * This is a Service Provider Interface for inspector. which will use with {@link InspectorService}
 *
 * To determine which inspector to return which is based on the file mineType (file extension).
 * The convention of Inspector name is [minetype] + Inspector which is case intensive.
 * Eg:
 *     psml will try to find PSMLInspector.
 *     zip will try to find ZipInspector.
 *
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  17 June 2015
 */
public interface PackageInspector {

  /**
   * The name of the {@link PackageInspector}
   * @return the name of the Inspector
   */
  String getName();

  /**
   * To indicate what media type it can support for this inspector.
   * @param mediatype the media type
   * @return whether the media type is support for the {@link PackageInspector}
   */
  boolean supportsMediaType(String mediatype);

  /**
   * The actual implementation of inspector
   * @param pack The packageData to inspect
   */
  void inspect(PackageData pack);

}
