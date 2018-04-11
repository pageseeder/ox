/*
 *  Copyright (c) 2018 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPPost {

  private static final Logger LOGGER = LoggerFactory.getLogger(HTTPPost.class);

  /**
   * The HTTP connection to the Pageseeder Servlet
   */
  private HttpURLConnection connection;

  /**
   * The outputstream used to write the data.
   */
  private DataOutputStream out = null;

  /**
   * The boundary String, used when uploading as multipart
   */
  private static final String BOUNDARY = "-----------7d32a512502e0";

  /**
   * The user ID.
   */
  private String userid;

  /**
   * The password.
   */
  private String password;


  /**
   * HTTP parameters to supply to the servlet.
   */
  private final Map<String, String> parameters = new HashMap<String, String>();

  private final String url;


  /**
   * Constructor.
   *
   * @param url      The URL to connect to
   */
  public HTTPPost(String url) {
    this.url = url;
  }

  /**
   * Constructor with BASIC authentication.
   *
   * @param url      The URL to connect to
   * @param userid   The user id
   * @param password The password
   */
  public HTTPPost(String url, String userid, String password) {
    this.url = url;
    this.userid = userid;
    this.password = password;
  }

  /**
   * @return The basic authorization string
   */
  public String getBasicAuthorization() {
    byte[] bc = (this.userid+":"+new String(this.password)).getBytes(StandardCharsets.UTF_8);
    return "Basic "+DatatypeConverter.printBase64Binary(bc);
  }
  /**
   * Add a parameter to send to the server.
   *
   * @param name  The name of the parameter to add.
   * @param value The value of the parameter to add.
   */
  public void addParameter(String name, String value) {
    if (this.out != null)
      throw new IllegalStateException("Already connected!");
    if (name != null && value != null)
      this.parameters.put(name, value);
  }

  /**
   * Attempt to connect to the server.
   *
   * @throws IOException Should any error occur while connecting to the server.
   */
  public void connect() throws IOException {
    if (this.out != null)
      throw new IllegalStateException("Already connected!");
    String uriString = this.url;
    if (this.parameters != null) {
      char separator = this.url.indexOf('?') == -1 ? '?' : '&';
      for (String pname : this.parameters.keySet()) {
        String value = this.parameters.get(pname);
        try {
          uriString += separator + pname + "=" + URLEncoder.encode(value.toString(), "UTF-8");
        } catch (IOException ex) {
          LOGGER.error("Failed to encode parameter {}={}", pname, value);
        }
        separator = '&';
      }
    }
    // connect
    this.connection = (HttpURLConnection) new URL(uriString).openConnection();
    this.connection.setDoInput(true);
    this.connection.setDoOutput(true);
    // set the content type to multipart/form-data.
    this.connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
    this.connection.setRequestMethod("POST");
    if (this.userid != null && this.password != null) {
      // Use Basic Auth (5.6+)
      connection.addRequestProperty("Authorization", getBasicAuthorization());
    }

    // then connect
    this.connection.connect();
    // get the outputstream
    try {
      this.out = new DataOutputStream(this.connection.getOutputStream());
      LOGGER.debug("Connected to {}", uriString);
    } catch (Exception ex) {
      LOGGER.error("Failed to connect to {}", uriString, ex);
    }
  }

  /**
   * Add a part to the request (write the contents directly to the stream).
   *
   * @param content     the contents of the Part
   * @param encoding    the encoding to specify in the Part's header
   * @param contentType the content-type to specify in the Part's header
   * @throws IOException if anything goes wrong
   */
  public void addPart(byte[] content, String contentType, String name, String filename) throws IOException {
    if (this.out == null)
      throw new IllegalStateException("Connection failed or closed, cannot send file");
    // start with boundary
    writeBytes("--" + BOUNDARY + "\r\n");
    writeBytes("Content-Type: " + contentType + "\r\n");
    String fn = filename == null ? "" : ("; filename=\"" + filename + "\"");
    writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + fn + "\r\n\r\n");
    // write date
    this.out.write(content);

    // end with new line
    writeBytes("\r\n");

    // flush
    this.out.flush();
  }

  /**
   * Close the this.connection and read the response from the Server.
   *
   * @return String the reponse from the server.
   * @throws IOException if the disconnection failed or an error occurred.
   */
  public String getResponse() throws IOException {
    if (this.out == null)
      throw new IllegalStateException("Connection failed or closed, cannot get response");
    // close outputstream
    writeBytes("--" + BOUNDARY + "--\r\n");
    this.out.flush();
    this.out.close();
    this.out = null;

    StringBuilder response = new StringBuilder();
    try {
      // check status
      int status = this.connection.getResponseCode();
      String message = this.connection.getResponseMessage();

      // read response
      String encoding = this.connection.getContentEncoding();
      if (encoding == null) encoding = "UTF-8";
      InputStream responseStream = status != HttpURLConnection.HTTP_OK ? this.connection.getErrorStream() : this.connection.getInputStream();
      try {
        byte[] buff = new byte[1024];
        int read;
        while ((read = responseStream.read(buff)) != -1)
          response.append(new String(buff, 0, read, encoding));
      } finally {
        IOUtils.closeQuietly(responseStream);
      }
      // error?
      if (status != HttpURLConnection.HTTP_OK) {
        LOGGER.error("Connection failed with {}, response was {}", status, response);
        LOGGER.error("Connection failed with {}, message was {}", status, message);
        throw new HTTPException(status);
      }

    } finally {
      this.connection.disconnect();
      this.connection = null;
    }
    return response.toString();
  }

  /*
   * write Bytes using UTF-8 encoding
   */
  private void writeBytes(String s) throws UnsupportedEncodingException, IOException {
    if (this.out == null)
      throw new IllegalStateException("Connection failed or closed, cannot write bytes");
    this.out.write(s.getBytes("UTF-8"));
  }

}
