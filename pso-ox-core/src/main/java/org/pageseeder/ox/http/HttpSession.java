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

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * A simple object to represent a PageSeeder session.
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
public final class HttpSession implements HttpCredentials, Serializable {

  /**
   * As per recommendation for the {@link Serializable} interface.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The session ID in PageSeeder.
   */
  private final String _jsessionid;

  /**
   * Indicates when the user was last successfully connected to PageSeeder.
   *
   * <p>This time stamp is used to determine whether the session is still likely to be valid.
   */
  private long timestamp;

  /**
   * Create a new session with the specified ID.
   *
   * @param session The session ID.
   */
  public HttpSession(String session) {
    this._jsessionid = session;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Create a new session with the specified ID.
   *
   * @param session   The session ID.
   * @param timestamp When the session was initially created.
   */
  public HttpSession(String session, long timestamp) {
    this._jsessionid = session;
    this.timestamp = timestamp;
  }

  /**
   * Gets j session id.
   *
   * @return the jsessionid
   */
  public String getJSessionId() {
    return this._jsessionid;
  }

  /**
   * Update the timestamp for this session.
   */
  public void update() {
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Returns the age of this session.
   *
   * @return The current time minus the timestamp.
   */
  public long age() {
    return System.currentTimeMillis() - this.timestamp;
  }

  /**
   * Returns the age of this session.
   *
   * @return The current time minus the timestamp.
   */
  public long timestamp() {
    return this.timestamp;
  }

  @Override
  public String toString() {
    return this._jsessionid;
  }

  /**
   * Generate the session from the <code>Set-Cookie</code> HTTP response header.
   *
   * <p>The expected header value should be:
   * <pre>
   *  JSESSIONID=F354C484E7368C6EB08E83A6E913FEA4; Path=/ps; Secure
   * </pre>
   *
   * @param cookie The cookie value
   * @return the corresponding instance or <code>null</code> if the cookie could be parsed
   */
  public static @Nullable HttpSession parseSetCookieHeader(@Nullable String cookie) {
    String name = "JSESSIONID=";
    if (cookie != null && cookie.length() > name.length()) {
      int from = cookie.indexOf(name);
      int to = cookie.indexOf(';', from+name.length());
      if (from != -1 && to != -1) return new HttpSession(cookie.substring(from+name.length(), to));
    }
    // no sessionid it seems.
    return null;
  }

  @Override
  public HttpHeader toHeader() {
    return null;
  }

  @Override
  public boolean isSession() {
    return true;
  }
}
