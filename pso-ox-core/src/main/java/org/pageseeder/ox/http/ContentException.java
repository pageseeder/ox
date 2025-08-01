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
 * Class of exceptions thrown while attempting to process the content of a response.
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
public final class ContentException extends RuntimeException {

  /** As per requirement for serializable. */
  private static final long serialVersionUID = 20160623;

  /**
   * Constructs a new content exception with the specified message.
   *
   * @param message Message explaining the error
   */
  public ContentException(String message) {
    super(message);
  }

  /**
   * Constructs a new content exception with the specified message and
   * cause.
   *
   * @param message Message explaining the error.
   * @param cause   The original cause
   */
  public ContentException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new content exception with the specified message and
   * cause.
   *
   * @param cause The original cause
   */
  public ContentException(Throwable cause) {
    super(cause);
  }

}
