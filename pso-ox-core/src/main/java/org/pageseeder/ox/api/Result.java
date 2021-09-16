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

import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.xmlwriter.XMLWritable;

/**
 * The result of a command.
 *
 * <p>It should have an XML representation and wrap the information about the result of a command.
 *
 * @author Christophe Lauret
 * @since  28 October 2013
 */
public interface Result extends XMLWritable {

  /**
   * @return The time it took to execute the command in milliseconds.
   */
  long time();

  /**
   * @return The status of that result.
   */
  ResultStatus status();

  /**
   * @return Any error that may have occurred and caused the command to fail.
   */
  Exception error();

}
