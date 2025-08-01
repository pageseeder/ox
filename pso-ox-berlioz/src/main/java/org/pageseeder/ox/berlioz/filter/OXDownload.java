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
package org.pageseeder.ox.berlioz.filter;

import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.berlioz.servlet.OXGetFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>A Download {@link Filter} for download file from OX. </p>
 *
 * @author Ciber Cai
 * @version 24 June 2016
 * @deprecated Please use the {@link OXGetFile}
 * TODO Remove in the version 2.3.0
 */
@Deprecated
public final class OXDownload implements Filter {

  /**  the logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXDownload.class);

  /**  the buffer size. */
  private static final int BUFFER_SIZE = 2048;

  /**  the matching prefix pattern. */
  private String pattern;

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    this.pattern = config.getInitParameter("pattern") != null ? config.getInitParameter("pattern") : "";
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {}

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    doHttpFilter((HttpServletRequest) req, (HttpServletResponse) res, chain);
  }

  /**
   * Do http filter.
   *
   * @param req the req
   * @param res the res
   * @param chain the chain
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  private void doHttpFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

    String requestURI = req.getRequestURI();
    //Getting the string after the pattern
    String requestPath = requestURI.substring(requestURI.lastIndexOf(this.pattern) + this.pattern.length());
    LOGGER.debug("Pattern {}", pattern);
    LOGGER.debug("request PATH {}", requestPath);

    if (requestPath == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // get file
    File file = new File(OXConfig.getOXTempFolder(), requestPath);
    if (file != null && file.exists() && file.isFile()) {
      String mediaType = getMidiaType(file);

      res.setContentType("unknown".equals(mediaType) ? "application/octet-stream" : mediaType);
      res.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + '"');

      try (InputStream ins = new FileInputStream(file)) {
        // get output stream for servlet
        ServletOutputStream outs = res.getOutputStream();
        byte[] buff = new byte[BUFFER_SIZE];
        boolean reading = true;
        while (reading) {
          int i = ins.read(buff, 0, BUFFER_SIZE);
          if (i > 0) {
            outs.write(buff, 0, i);
          }
          if (i != BUFFER_SIZE) {
            reading = false;
          }
        }
      }
    }

    // cannot find the file
    else {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

  }

  /**
   * Gets the midia type.
   *
   * @param file the file
   * @return the mime type by filename
   */
  private static String getMidiaType(File file) {
    if (file != null && file.exists()) {
      Path path = file.toPath();
      try {
        return Files.probeContentType(path) != null ? Files.probeContentType(path) : "unknown";
      } catch (IOException ex) {
        LOGGER.warn("Cannot fine media type for {}", path);
        return "unknown";
      }
    }
    return "unknown";
  }
}
