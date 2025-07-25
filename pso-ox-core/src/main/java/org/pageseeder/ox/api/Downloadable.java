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

import java.io.File;

/**
 * Implement this interface to {@link Result} if the output intend to download (expose) from public.
 *
 * @author Ciber Cai
 * @since 17 June 2014
 * @deprecated There is not a need for this class as we can check if there an output (Check {@link org.pageseeder.ox.tool.DefaultResult})
 */
public interface Downloadable {

  /**
   * Download path.
   *
   * @return The downloadable file.
   */
  public File downloadPath();

}
