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
package org.pageseeder.ox.client;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Carlos Cabral
 * @since 26 August 2016
 */
public final class OXDownloadServlet extends HttpServlet implements Servlet {

  /** As per requirement */
  private static final long serialVersionUID = 20160624L;
  private static final int BUFFER_SIZE = 2048;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void destroy() {
    super.destroy();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {


    String pathInfo = req.getPathInfo();
    try {

      // We must have some credentials
//      Optional<PSCredentials> credentials = App.getCredentials(req);
//      if (!credentials.isPresent()) {
//        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//        return;
//      }
      // We only accept the URI ID possibly starting with a '/'
      String jobId = pathInfo.replaceAll("/", "");
      String fileName = req.getParameter("filename");

      if (StringUtils.isBlank(jobId)) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "The job id was no send.");
        return;
      }

      if (StringUtils.isBlank(fileName)) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "The file was no send.");
        return;
      }

      if (!fileName.matches("[a-zA-Z0-9()_ \\.-]+")) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "The file is invalid.");
        return;
      }


      File temp = OXConfig.getOXTempFolder();
      File destination = new File(temp, fileName);

      OxGetFileProcessor processor = new OxGetFileProcessor(destination, jobId);

      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      processor.process(xml);

      toHttpResponse(res, destination);

    } catch (BerliozException ex) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
      return;
    }
  }

  /**
   * Create the HTTP response for the outcome.
   *
   * @param res     The res is a {@link HttpServletResponse}.
   * @param source  The file need to send to download.
   * @throws IOException
   */
  private static void toHttpResponse(HttpServletResponse res, File file) throws IOException {
    List<File> files = new ArrayList<File>();
    files.add(file);
    toHttpResponse(res, files, FileUtils.getFileExtension(file));
  }

  /**
   * Create the HTTP response for the outcome.
   *
   * @param res     The res is a {@link HttpServletResponse}.
   * @param source  The file need to send to download.
   * @throws IOException
   */
  private static void toHttpResponse(HttpServletResponse res, List<File> files, String format) throws IOException {
    // set content type
    res.setContentType(getContentType(format));
    switch (format) {
    case "html":
      toHTMLResponse(res, files);
      break;
    case "xml":
      toXMLResponse(res, files);
      break;
    case "docx":
      toDOCXResponse(res, files);
      break;
    default:
      throw new UnsupportedOperationException("Format of " + format + " haven't implemented yet.");
    }
  }

  /**
   * return the html snippet in a full html content.
   * @param res the HttpServletResponse
   * @param files the list of files
   * @throws IOException
   */
  private static void toDOCXResponse(HttpServletResponse res, List<File> files) throws IOException {
    if (files == null || files.size() == 0) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else if (files.size() == 1) {
      File file = files.get(0);
      res.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + '"');
      ServletOutputStream out = res.getOutputStream();
      toResponse(out, file);
    } else {
      //files.size() > 1
      throw new UnsupportedOperationException("The return for more than one docx haven't implemented yet.");
    }
  }

  /**
   * return the html snippet in a full html content.
   * @param res the HttpServletResponse
   * @param files the list of files
   * @throws IOException
   */
  private static void toHTMLResponse(HttpServletResponse res, List<File> files) throws IOException {
    ServletOutputStream out = res.getOutputStream();
    out.write("<html>".getBytes("UTF-8"));
    out.write("<body>".getBytes("UTF-8"));
    for (File f : files) {
      toResponse(out, f);
    }
    out.write("</body>".getBytes("UTF-8"));
    out.write("</html>".getBytes("UTF-8"));
  }

  /**
   * return the xml snippet in a full xml content.
   * @param res the HttpServletResponse
   * @param files the list of files
   * @throws IOException
   */
  private static void toXMLResponse(HttpServletResponse res, List<File> files) throws IOException {
    ServletOutputStream out = res.getOutputStream();
    out.write("<sources>".getBytes("UTF-8"));
    for (File f : files) {
      toResponse(out, f);
    }
    out.write("</sources>".getBytes("UTF-8"));
  }

  /**
   * @param out the OutputStream
   * @param file the file need to write it out.
   * @throws IOException
   */
  private static void toResponse(OutputStream out, File file) throws IOException {

    // get file
    try (InputStream ins = new BufferedInputStream(new FileInputStream(file))) {
      // get output stream for response
      byte[] buff = new byte[BUFFER_SIZE];
      boolean reading = true;
      while (reading) {
        int i = ins.read(buff, 0, BUFFER_SIZE);
        if (i > 0) {
          out.write(buff, 0, i);
        }
        if (i != BUFFER_SIZE) {
          reading = false;
        }
      }
    }
  }

  /**
   * Return the content type for the HTTP response.
   *
   * @param format     The content type format.
   */
  private static String getContentType(String format) {
    switch (format) {
    case "docx":
      return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    case "pdf":
      return "application/pdf";
    case "html":
      return "text/html";
    case "xml":
      return "application/xml";
    case "zip":
      return "application/octet-stream";
    default:
      throw new IllegalArgumentException("Unknown type.");
    }
  }
}
