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
package org.pageseeder.ox.berlioz;

import org.pageseeder.ox.ErrorMessage;

/**
 * The Enum OXBerliozErrorMessage.
 *
 * @author Carlos Cabral
 * @since 15 May 2018
 */
public enum OXBerliozErrorMessage implements ErrorMessage {

  /** The request is not multipart. */
  REQUEST_IS_NOT_MULTIPART ("OX-BZ-0001", "The specified request is not a multipart request. It should be multipart/form-data or multipart/mixed stream.");

  /** The message. */
  private final String message;

  /** The code. */
  private final String code;

  /**
   * Instantiates a new OX berlioz error message.
   *
   * @param message the message
   * @param code the code
   */
  private OXBerliozErrorMessage(String code, String message) {
    this.message = message;
    this.code = code;
  }

  /* (non-Javadoc)
   * @see org.pageseeder.ox.ErrorMessage#getMessage()
   */
  public String getMessage() {
    return message;
  }

  /* (non-Javadoc)
   * @see org.pageseeder.ox.ErrorMessage#getCode()
   */
  public String getCode() {
    return code;
  }
}
