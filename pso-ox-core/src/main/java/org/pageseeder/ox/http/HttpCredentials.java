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
package org.pageseeder.ox.http;

/**
 * Objects implementing this interface can be used as credentials in a connection.
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
public interface HttpCredentials {

  /**
   * To header http header.
   *
   * @return the http header
   */
  public HttpHeader toHeader();

  /**
   * Is session boolean.
   *
   * @return the boolean
   */
  public boolean isSession();
}
