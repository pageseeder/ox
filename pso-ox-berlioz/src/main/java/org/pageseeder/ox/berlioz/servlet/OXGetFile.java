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
package org.pageseeder.ox.berlioz.servlet;

import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Get a file inside in the ox structure (Packages). </p>
 *
 * @author Adriano Akaishi
 * @version 15 May 2017
 */
public final class OXGetFile extends HttpServlet {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -7694484458213953462L;

  /**  the logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXGetFile.class);

  /**  the buffer size. */
  private static final int BUFFER_SIZE = 2048;

  /**  the matching prefix pattern. */
  private String pattern;

  /** Indicate which file s extensions are allowed. */
  private String extesionsAllowed = "";

  /**  If true this file will be downloadable, otherwise no. */
  private String downloadable = "";

   @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

     this.extesionsAllowed = getServletConfig().getInitParameter("extesions-allowed");
     this.pattern = getServletConfig().getInitParameter("pattern");
     this.downloadable = getServletConfig().getInitParameter("downloadable");
     String [] extesionsAllowed = null;
     if (!StringUtils.isBlank(this.extesionsAllowed)) {
       extesionsAllowed = this.extesionsAllowed.split(",");
     }

     String requestURI = req.getRequestURI();
     //Getting the string after the pattern
     String requestPath = requestURI.substring(requestURI.lastIndexOf(this.pattern) + this.pattern.length());
     LOGGER.debug("Pattern {}", pattern);
     LOGGER.debug("request PATH {}", requestPath);
     LOGGER.debug("downloadable {}", downloadable);


     if (requestPath == null) {
       res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
       return;
     }

     if (isFileAllowed(extesionsAllowed, requestPath)) {
       // get file
       File file = new File(OXConfig.getOXTempFolder(), requestPath);
       if (file != null && file.exists() && file.isFile()) {
         String mediaType = getMediaType(file);
         LOGGER.debug("Content type {}.", mediaType);
         res.setContentType("unknown".equals(mediaType) ? "application/octet-stream" : mediaType);

         String contentDisposition = "true".equals(this.downloadable) ? "attachment" : "inline";
         LOGGER.debug("Content Disposition {}.", contentDisposition);
         res.setHeader("Content-Disposition", contentDisposition + "; filename=\"" + file.getName() + '"');

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
     // File not allowed
     else {
       res.setStatus(HttpServletResponse.SC_FORBIDDEN);
       return;
     }
   }


   /**
    * Checks if is file allowed.
    *
    * @param extesionsAllowed the extesions allowed
    * @param file the file
    * @return true, if is file allowed
    */
   private boolean isFileAllowed(String [] extesionsAllowed, String file) {
     boolean isAllowed = false;
     if (extesionsAllowed == null || extesionsAllowed.length == 0) {
       isAllowed = true;
     } else {
       for (String extension:extesionsAllowed) {
         if (file.endsWith(extension)) {
           isAllowed = true;
           break;
         }
       }
     }


     return isAllowed;
   }

   /**
    * Gets the midia type.
    *
    * @param file the file
    * @return the mime type by filename
    */
   private static String getMediaType(File file) {
     return FileUtils.getMimeType(file);
   }
}
