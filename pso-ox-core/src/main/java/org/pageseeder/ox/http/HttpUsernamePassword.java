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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A username and password pair to authenticate a user on PageSeeder.
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
public final class HttpUsernamePassword implements HttpCredentials {

  /**
   * The user ID.
   */
  private final String _username;

  /**
   * The password.
   */
  private final String _password;

  /**
   * Creates a new username and password set of credentials.
   *
   * @param username The username
   * @param password The password
   * @throws NullPointerException     if either argument is <code>null</code>
   * @throws IllegalArgumentException if either argument is considered invalid.
   */
  public HttpUsernamePassword(String username, String password) {
    if (username == null || password == null) throw new NullPointerException();
    this._username = username;
    this._password = password;
  }

  /**
   * Username string.
   *
   * @return The username (cannot be <code>null</code>).
   */
  public String username() {
    return this._username;
  }

  /**
   * Password string.
   *
   * @return The password (cannot be <code>null</code>).
   */
  public String password() {
    return this._password;
  }

  /**
   * To basic authorization string.
   *
   * @return The basic authorization string
   */
  public String toBasicAuthorization() {
    byte[] bc = (this._username+":"+this._password).getBytes(StandardCharsets.UTF_8);
    return "Basic "+ Base64.getEncoder().encodeToString(bc);
  }

  @Override
  public HttpHeader toHeader() {
    return new HttpHeader("Authorization", this.toBasicAuthorization());
  }

  @Override
  public boolean isSession() {
    return false;
  }
}
