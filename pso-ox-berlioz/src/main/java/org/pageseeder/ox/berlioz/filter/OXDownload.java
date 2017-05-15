package org.pageseeder.ox.berlioz.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pageseeder.ox.OXConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A Download {@link Filter} for download file from OX. </p>
 *
 * @author Ciber Cai
 * @version 24 June 2016
 * @deprecated Please use the {@link OXGetFile}
 * TODO Remove in the version 2.3.0
 */
public final class OXDownload implements Filter {

  /** the logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXDownload.class);

  /** the buffer size */
  private static final int BUFFER_SIZE = 2048;

  /** the matching prefix pattern */
  private String pattern;

  @Override
  public void init(FilterConfig config) throws ServletException {
    this.pattern = config.getInitParameter("pattern") != null ? config.getInitParameter("pattern") : "";
  }

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    doHttpFilter((HttpServletRequest) req, (HttpServletResponse) res, chain);
  }

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
