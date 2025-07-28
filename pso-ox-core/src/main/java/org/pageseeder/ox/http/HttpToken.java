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

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Tokens can be used as valid credentials when making calls to api/url/service.
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
public class HttpToken implements HttpCredentials, Serializable {

  /** As required for Serializable */
  private static final long serialVersionUID = 20161016L;

  /**
   * Tokens normally use a base64 encoding with no padding.
   *
   * <p>We allow all possible Base64 characters as well as '.' and padding '=' for extensibility.
   *
   * <p>No assumption is made on the length, but we discard any token that is too short or too long.
   *
   * <p>It is based on Pageseeder token
   */
  protected static final Pattern VALID_TOKEN = Pattern.compile("[a-zA-Z0-9=.+/_-]{16,1024}");

  /**
   * The actual token.
   */
  private final String _token;

  /**
   * When this token expires.
   */
  private final long _expires;

  /**
   * Creates a new PageSeeder access token.
   *
   * @param token A new PageSeeder access token.
   * @throws NullPointerException     if the token is <code>null</code>
   * @throws IllegalArgumentException if the token is not valid.
   */
  public HttpToken(String token) {
    this(token, 0L);
  }

  /**
   * Creates a new PageSeeder access token.
   *
   * @param token   A new PageSeeder access token.
   * @param expires When the token expires.
   * @throws NullPointerException     if the token is <code>null</code>
   * @throws IllegalArgumentException if the token is not valid.
   */
  public HttpToken(String token, long expires) {
    Objects.requireNonNull(token, "Access token is null");
    if (!VALID_TOKEN.matcher(token).matches())
      throw new IllegalArgumentException("Access token is invalid");
    this._token = token;
    this._expires = expires;
  }

  /**
   * Token string.
   *
   * @return the actual access token.
   */
  public String token() {
    return this._token;
  }

  /**
   * Expires millis long.
   *
   * @return when the token expires in milliseconds since Epoch.
   */
  public long expiresMillis() {
    return this._expires;
  }

  /**
   * Has expired boolean.
   *
   * @return <code>true</code> if the token has expired;         <code>false</code> otherwise or if it is not known.
   */
  public boolean hasExpired() {
    return System.currentTimeMillis() - this._expires > 0;
  }

  @Override
  public HttpHeader toHeader() {
    return new HttpHeader("Authorization", "Bearer " + this.token());
  }

  @Override
  public boolean isSession() {
    return false;
  }
}
