package org.pageseeder.ox.berlioz.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pageseeder.ox.OXConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pageseeder.internal.ox.util.StringUtils;

/**
 * <p>A Download {@link Filter} for download file from OX. </p>
 *
 * @author Ciber Cai
 * @version 24 June 2016
 */
public final class OXGetFile extends HttpServlet {
  /** the logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXGetFile.class);

  /** the buffer size */
  private static final int BUFFER_SIZE = 2048;
  
  /** the matching prefix pattern */
  private String pattern;

  /** get the allowed */
  private String allowed = "";

   @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//     XMLWriter xml = new XMLWriterImpl(resp.getWriter());

     allowed = getServletConfig().getInitParameter("allowed");
     pattern = getServletConfig().getInitParameter("pattern");
     
     String [] valuesAllowed = null;
     if (!StringUtils.isBlank(allowed)) {
       valuesAllowed = allowed.split(",");
     }

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
