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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * A HTTP parameter
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
public final class HttpParameter {

  /**
   * The parameter name (not URL encoded)
   */
  private final String _name;

  /**
   * The parameter value (not URL encoded)
   */
  private final String _value;

  /**
   * Create a new parameter
   *
   * @param name  The parameter name (not URL encoded)
   * @param value The parameter value (not URL encoded)
   */
  public HttpParameter(String name, String value) {
    this._name = Objects.requireNonNull(name, "Parameter name must not be null");
    this._value = Objects.requireNonNull(value, "Parameter value must not be null");
  }

  /**
   * Name string.
   *
   * @return The parameter name (not URL encoded)
   */
  public String name() {
    return this._name;
  }

  /**
   * Value string.
   *
   * @return The parameter value (not URL encoded)
   */
  public String value() {
    return this._value;
  }

  /**
   * Append.
   *
   * @param query the query
   */
  public void append(StringBuilder query) {
    try {
      query.append(URLEncoder.encode(this._name, "utf-8"));
      query.append("=").append(URLEncoder.encode(this._value, "utf-8"));
    } catch (UnsupportedEncodingException ex) {
      // Should never happen as UTF-8 is supported
      throw new RuntimeException();
    }
  }

  @Override
  public String toString() {
    StringBuilder q = new StringBuilder();
    append(q);
    return q.toString();
  }


  /**
   * New parameter http parameter.
   *
   * @param parameter the parameter
   * @return the http parameter
   */
  public static HttpParameter newParameter(String parameter) {
    try {
      int e = parameter.indexOf('=');
      if (e < 0) {
        String name = URLDecoder.decode(parameter.substring(0, e), "utf-8");
        return new HttpParameter(name, "");
      } else {
        String name = URLDecoder.decode(parameter.substring(0, e), "utf-8");
        String value = URLDecoder.decode(parameter.substring(e+1), "utf-8");
        return new HttpParameter(name, value);
      }
    } catch (UnsupportedEncodingException ex) {
      // Should never happen as UTF-8 is always supported
      throw new RuntimeException();
    }
  }

}
