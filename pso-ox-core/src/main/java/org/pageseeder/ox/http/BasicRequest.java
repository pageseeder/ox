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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Base class for HTTP requests to PageSeeder.
 *
 * @author Carlos Cabral
 * @since 2.2.69
 * @since 16 September 2021
 */
abstract class BasicRequest {

  /**
   * The "User-Agent" String sent from the Bridge.
   *
   * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.5.3">HTTP/1.1: Semantics and Content - User-Agent</a>
   */
  private static final HttpHeader USER_AGENT = new HttpHeader("User-Agent", getUserAgentString());

  /**
   * The "X-HTTP-Method-Override" header to tunnel a PATCH request via POST.
   */
  private static final HttpHeader HTTP_METHOD_OVERRIDE_PATCH = new HttpHeader("X-HTTP-Method-Override", "PATCH");

  /**
   * HTTP method
   */
  protected final HttpMethod _method;

  /**
   * Path to the resources
   */
  protected final String _url;

  /**
   * List of parameters.
   */
  protected final List<HttpParameter> _parameters = new ArrayList<>();

  /**
   * List of HTTP request headers.
   */
  protected List<HttpHeader> _headers = new ArrayList<>();

  /**
   * Any credentials used when making the request.
   */
  protected @Nullable HttpCredentials credentials;

  /**
   * The connect timeout on the request.
   */
  protected int timeout = -1;

  /**
   * Creates a new request to PageSeeder.
   *
   * @param method The HTTP method
   * @param url    The url (http://www.something.com?parameters=value)
   */
  public BasicRequest(HttpMethod method, String url) {
    this._method = Objects.requireNonNull(method, "the HTTP method is required");
    this._url = extractPath(url);
    String query = extractQuery(url);
    if (query.length() > 0) {
      addQueryToParameters(query, this._parameters);
    }
    if (method == HttpMethod.PATCH) {
      this._headers.add(HTTP_METHOD_OVERRIDE_PATCH);
    }
    this._headers.add(USER_AGENT);
  }

  // Setters (return Request)
  // --------------------------------------------------------------------------

  /**
   * Sets a request header.
   *
   * @param name  The name of the HTTP header
   * @param value The value of the HTTP header.
   * @return This request.
   */
  public abstract BasicRequest header(String name, String value);

  /**
   * Adds a request parameter.
   *
   * <p>When using HTTP method <code>POST</code>, these parameters will be encoded as
   * <code>application/x-www-form-urlencoded</code>.
   *
   * <p>For all other HTTP methods, these parameters will be added to the query part.
   *
   * @param name  The name of the HTTP parameters
   * @param value The value of the HTTP parameters.
   * @return This request.
   */
  public BasicRequest parameter(String name, String value) {
    this._parameters.add(new HttpParameter(name, value));
    return this;
  }

  /**
   * Specify which credentials to use with this request.
   *
   * <p>Only one set of credentials can be used a time, this method will replace
   * any credentials that may have been set priority.
   *
   * <p>This method will automatically update the "Authorization" header field.
   *
   * @param credentials The username/password, token or session to use as credentials
   * @return This request.
   */
  public BasicRequest using(HttpCredentials credentials) {
    this.credentials = credentials;
    if (this.credentials != null) {
      if (this.credentials.isSession()) {
        this.removeHeader("Authorization");
      } else {
        this.setHeader(credentials.toHeader());
      }
    }
    return this;
  }

  /**
   * Sets the time out
   *
   * @param timeout the time out
   * @return This request
   */
  public BasicRequest timeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  // Getters (return objects)
  // --------------------------------------------------------------------------

  /**
   * Returns the PageSeeder path that is the part after the site prefix as
   * entered in the constructor.
   *
   * @return The path AFTER the site prefix.
   */
  public String path() {
    return this._url;
  }

  /**
   * Returns the HTTP header the specified name.
   *
   * @param name The name of the HTTP header (case insensitive)
   * @return The value of the corresponding parameter or <code>null</code>
   */
  public @Nullable String header(String name) {
    for (HttpHeader h : this._headers) {
      if (h.name().equalsIgnoreCase(name)) return h.value();
    }
    return null;
  }

  /**
   * Returns the value of the first HTTP parameter matching the specified name.
   *
   * @param name The name of the parameter
   * @return The value of the corresponding parameter or <code>null</code>
   */
  public @Nullable String parameter(String name) {
    for (HttpParameter p : this._parameters) {
      if (p.name().equals(name)) return p.value();
    }
    return null;
  }

  /**
   * Returns the credentials in use.
   *
   * @return The credentials used by this request
   */
  public @Nullable HttpCredentials credentials() {
    return this.credentials;
  }

  /**
   * Returns the string to write the parameters sent via POST as <code>application/x-www-form-urlencoded</code>.
   *
   * @return the string to write the parameters sent via POST.
   */
  public String encodeParameters() {
    StringBuilder q = new StringBuilder();
    for (HttpParameter p : this._parameters) {
      if (q.length() > 0) {
        q.append("&");
      }
      p.append(q);
    }
    return q.toString();
  }

  /**
   * Returns the URL to access this resource.
   *
   * <p>If the user is specified, its details will be included in the URL so that the resource can
   * be accessed on his behalf.
   *
   * @return the URL to access this resource.
   * @throws MalformedURLException If the URL is not well-formed
   */
  public URL toURL() throws MalformedURLException {
    return new URL(toURLString());
  }

  /**
   * Returns the URL to access this resource.
   *
   * <p>If the user is specified, its details will be included in the URL so that the resource can
   * be accessed on his behalf.
   *
   * @return the URL to access this resource.
   */
  public String toURLString() {
    // Start building the URL
    StringBuilder url = new StringBuilder(this._url);

    // If the session ID is available
    if (this.credentials != null && this.credentials.isSession()) {
      // Use the specified user if available
      url.append(";jsessionid=").append(this.credentials);
    }

    // When not using the "application/x-www-form-urlencoded"
    if (this._method != HttpMethod.POST && this._method != HttpMethod.PATCH) {
      if (!this._parameters.isEmpty()) {
        url.append('?').append(encodeParameters());
      }
    }

    return url.toString();
  }

  /**
   * Timeout int.
   *
   * @return The connection timeout on the request
   */
  public int timeout() {
    return this.timeout;
  }

  /**
   * Implementations must generate the response object by connecting to PageSeeder
   * and returning an instantiated response that includes the status of the response.
   *
   * @return The response corresponding to this request.
   */
  public abstract Response response();

  // Convenience methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Compute the User Agent string as
   *
   * @return The "User-Agent" header string used by PageSeeder
   */
  public static String getUserAgentString() {
    Package p = Package.getPackage("org.pageseeder.ox");
    String version = p != null ? Objects.toString(p.getImplementationVersion(), "SNAPSHOT") : "SNAPSHOT";
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    String javaVersion = System.getProperty("java.version");
    String javaVendor = System.getProperty("java.vendor");
    return "OX/"+version+" ("+osName+"; "+osArch+") Java/"+javaVersion+" ("+javaVendor+")";
  }

  // Protected methods (for use by implementations)
  // ----------------------------------------------------------------------------------------------

  /**
   * Sets a request header.
   *
   * @param header The HTTP header
   */
  protected void setHeader(HttpHeader header) {
    if (header != null) {
      removeHeader(header.name());
      this._headers.add(header);
    }
  }

  /**
   * Sets a request header.
   *
   * @param name  The name of the HTTP header
   * @param value The value of the HTTP header.
   */
  protected void setHeader(String name, String value) {
    removeHeader(name);
    this._headers.add(new HttpHeader(name, value));
  }

  /**
   * Sets a request header.
   *
   * @param name  The name of the HTTP header
   * @param value The value of the HTTP header.
   */
  protected void setHeader(String name, int value) {
    removeHeader(name);
    this._headers.add(new HttpHeader(name, value));
  }

  /**
   * Removes the specified header.
   *
   * @param name the of the header to remove (not case sensitive)
   */
  protected void removeHeader(String name) {
    for (Iterator<HttpHeader> i = this._headers.iterator(); i.hasNext();) {
      @SuppressWarnings("null")
      HttpHeader h = i.next();
      if (h.name().equalsIgnoreCase(name)) {
        i.remove();
        break;
      }
    }
  }

  /**
   * Removes the specified header.
   *
   * @param name the of the header to remove (not case sensitive)
   * @return The header that was removed
   */
  protected @Nullable HttpHeader getHeader(String name) {
    for (HttpHeader h : this._headers) {
      if (h.name().equalsIgnoreCase(name)) return h;
    }
    return null;
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the path part of the specified URI.
   *
   * @param uri the path to the resource
   * @return the part before any '#' or '?'.
   */
  private static String extractPath(String uri) {
    // Remove the fragment part
    int f = uri.lastIndexOf('#');
    String p = f > 0? uri.substring(0, f) : uri;
    // Remove the query
    int q = p.indexOf('?');
    if (q >= 0) return p.substring(0, q);
    else return p;
  }

  /**
   * Returns the query part of the URL.
   *
   * @param uri the URI we extract the query to the resource from
   *
   * @return the part after and including '?' if it exists; otherwise "".
   */
  private static String extractQuery(String uri) {
    int q = uri.indexOf('?');
    int f = uri.indexOf('#');
    if (q < 0 || (f >= 0 && f < q)) return "";
    if (f > q) return uri.substring(q+1, f);
    else return uri.substring(q+1);
  }

  /**
   * Adds the parameters specified in the query to the parameters.
   *
   * @param query      The query part of the URL
   * @param parameters The parameters to update
   */
  private static void addQueryToParameters(String query, List<HttpParameter> parameters) {
    String[] pair = query.split("&");
    if (pair.length > 0) {
      for (String p : pair) {
        @SuppressWarnings("null")
        HttpParameter param = HttpParameter.newParameter(p);
        parameters.add(param);
      }
    }
  }

}
