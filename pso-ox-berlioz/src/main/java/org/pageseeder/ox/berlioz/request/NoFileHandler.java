/*
 * Copyright 2025 Allette Systems (Australia)
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
package org.pageseeder.ox.berlioz.request;

import org.apache.commons.io.IOUtils;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXErrorMessage;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.util.BerliozOXUtils;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This can be used when a pipeline can start without a file.
 * To use it, the parameter {@link RequestHandler#HANDLER_TYPE_PARAMETER} should have the value
 * {@link RequestHandlerType#NOFILE}.
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class NoFileHandler implements RequestHandler {

  /** The logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(NoFileHandler.class);

  private final static NoFileHandler INSTANCE = new NoFileHandler();

  private NoFileHandler(){
    LOGGER.info("NoFileHandler created");
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static NoFileHandler getInstance() {
    return INSTANCE;
  }

  /**
   * Receive.
   *
   * If there is any parameter in the url like /model/{model}/pipeline/{pipeline}.html . they will not be in the
   * HttpServletRequest.
   * Then before calling this method, you will need to set them in the attribute of HttpServletRequest, like:
   * req.setAttribute("model", model);
   * req.setAttribute("pipeline", pipeline);
   *
   * @param req the ContentRequest
   * @return the list of PackageData
   * @throws IOException when I/O error occur.
   * @throws OXException the OX exception
   */
  public List<PackageData> receive(HttpServletRequest req) throws IOException, OXException {
    List<PackageData> packs = new ArrayList<PackageData>();

    // Check that we have a file upload request
//    boolean isMultipart = false;
//    LOGGER.debug("Is it multipart? {}", isMultipart);

    String model = req.getParameter("model");
    if (StringUtils.isBlank(model)) {
      model = (String) req.getAttribute("model");
      if (StringUtils.isBlank(model)) {
        throw new OXException("Model cannot be null or empty");
      }
    }

      PackageData pack = toPackageData(model, toParameters(req.getParameterMap()));
      LOGGER.debug("pack {}", pack.id());
      pack.saveProperties();
      packs.add(pack);

//      LOGGER.debug("Adding parameters to package.");
//      Map<String, String[]> urlParameters = req.getParameterMap();
//      Map<String, String> parameters = toParameters(urlParameters);
//      LOGGER.debug("Number of parameters {}", parameters.size());

    // Return package data
    return packs;
  }

  /**
   * Create a new package data from the specified file item if possible.
   *
   * @param model the model
   * @param parameters the parameters
   * @return the package data
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws OXException the OX exception
   */
  private PackageData toPackageData(String model, Map<String, String> parameters) throws IOException, OXException {
    LOGGER.debug("Starts toPackageData {}", model);

    PackageData pack = PackageData.newPackageData(model, null);

    for (Entry<String, String> parameter : parameters.entrySet()) {
      pack.setParameter(parameter.getKey(), parameter.getValue());
      pack.saveProperties();
    }

    LOGGER.debug("Ends toPackageData {}/{}", model, pack.id());
    return pack;
  }

  /**
   * To parameters.
   *
   * @param urlParameters the url parameters
   * @return the map
   */
  private Map<String, String> toParameters (Map<String, String[]> urlParameters) {
    return BerliozOXUtils.flattenParameters(urlParameters);
  }



//  /**
//   * Copy to.
//   *
//   * @param stream the stream
//   * @param file the file
//   * @return the int
//   * @throws IOException Signals that an I/O exception has occurred.
//   * @throws OXException the OX exception
//   */
//  private static final int copyTo(InputStream stream, File file) throws IOException, OXException {
//    LOGGER.debug("Writing file: {}", file != null ? file.getAbsolutePath() : "null");
//    if (file == null || file.isDirectory()) throw new OXException(OXErrorMessage.FILE_NOT_SELECTED);
//    int copied = 0;
//    FileOutputStream os = null;
//    try {
//      os = new FileOutputStream(file);
//      copied = IOUtils.copy(stream, os);
//    } finally {
//      IOUtils.closeQuietly(os);
//    }
//    return copied;
//  }
//
//  /**
//   * To type.
//   *
//   * @param filename the specified file
//   * @return the type of specified file.
//   */
//  private static String toType(String filename) {
//    if (filename == null) { throw new NullPointerException("file name cannot be null"); }
//    String lcfilename = filename.toLowerCase();
//    if (lcfilename.endsWith("xml")) {
//      return "xml";
//    } else if (lcfilename.endsWith("docx")) {
//      return "docx";
//    } else if (lcfilename.endsWith("html")) {
//      return "html";
//    } else if (lcfilename.endsWith("htm")) {
//      return "html";
//    } else if (lcfilename.endsWith("psml")) {
//      return "psml";
//    } else if (lcfilename.endsWith("zip")) {
//      return "zip";
//    } else {
//      return filename.substring(filename.lastIndexOf(".") + 1);
//    }
//  }
//
//  /**
//   * To name.
//   *
//   * @param filename the filename
//   * @return the name of file without extension.
//   */
//  private static String toName(String filename) {
//    int dot = filename.lastIndexOf('.');
//    return filename.substring(0, dot);
//  }
//  /**
//   * Gets the filename.
//   *
//   * @param item the item
//   * @return the filename
//   * @throws OXException the OX exception
//   */
//  private static String getFilename(FileItem item) throws OXException {
//    String filename = item.getName();
//    LOGGER.debug("Original filename {}", filename);
//    if (!StringUtils.isBlank(filename)) {
//      //It is necessary because the Internet Explore and Edge send the full path of the file
//      //the this method remove all unnecessary path and returns the file name.
//      filename = FilenameUtils.getName(filename);
//      LOGGER.debug("Cleaned filename {}", filename);
//    } else {
//      LOGGER.debug("The uploaded file name is empty it may be because any file has been selected.");
//      throw new OXException(OXErrorMessage.FILE_NOT_SELECTED);
//    }
//    return filename;
//  }
//
//  /**
//   * Gets the temp upload directory.
//   *
//   * @return the temp upload directory
//   * @throws IOException Signals that an I/O exception has occurred.
//   */
//  private static File getTempUploadDirectory() throws IOException {
//    File tempUploadOX = OXConfig.getOXTempUploadFolder();
//    File tempDirectory = Files.createTempDirectory(tempUploadOX.toPath(), "upload").toFile();
//    LOGGER.debug("Temporary upload directory {}", tempDirectory.getAbsolutePath());
//    return tempDirectory;
//  }
}
