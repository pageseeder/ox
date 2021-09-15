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

/**
 *
 * Implement this interface to {@link Step} if the custom are able to return the percentage of its process.
 *
 * @author Carlos Cabral
 * @since  17 June 2014
 */
public interface Measurable {

  /**
   * If the value returned is -1, it means unknown.
   *
   * @return The percentage completed in a process/step/etc.
   */
  public int percentage();

}
