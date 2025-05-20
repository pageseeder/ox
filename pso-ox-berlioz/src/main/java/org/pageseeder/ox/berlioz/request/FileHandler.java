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
package org.pageseeder.ox.berlioz.request;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXErrorMessage;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.util.BerliozOXUtils;
import org.pageseeder.ox.berlioz.util.UploadFactory;
import org.pageseeder.ox.berlioz.util.UploadProcessor;
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
 * A file handler to receive the data from user upload.
 * This class should be used when a file is uploaded to start a pipeline.
 * If there is more than one file, this class will create a PackageData for each of them.
 * Except if it is in a zip.
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class FileHandler implements RequestHandler {

  /** The logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

  private final static FileHandler INSTANCE = new FileHandler();

  private FileHandler(){
    LOGGER.info("FileHandler created");
  }

  public static FileHandler getInstance() {
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
    // parse the upload request
    UploadProcessor processor = null;
    try {
      LOGGER.debug("Getting uploaded file.");
      UploadFactory factory = UploadFactory.getInstance();
      processor = factory.make(req);
      LOGGER.debug("Uploaded file loaded.");
    } catch (SizeLimitExceededException ex) {
      LOGGER.warn("File size exceeds upload limit please choose another file.", ex);
      throw new OXException("File size exceeds the limit of " + GlobalSettings.get("ox2.upload.max-size", 10) + "MB.", ex);
    } catch (InvalidContentTypeException ex) {
      LOGGER.warn("Invalid content type.", ex);
      throw new OXException("Invalid content type.", ex);
    } catch (FileUploadException ex) {
      LOGGER.error("File Upload Exception: {}", ex);
      throw new OXException("Cannot process the upload request.", ex);
    }

    // Check that we have a file upload request
    boolean isMultipart = processor.isMultipart();

    LOGGER.debug("Is it multipart? {}", isMultipart);

    String model = processor.getParameter("model", req.getParameter("model"));
    if (StringUtils.isBlank(model)) {
      model = (String) req.getAttribute("model");
      if (StringUtils.isBlank(model)) {
        throw new OXException("Model cannot be null or empty");
      }
    }

    if (isMultipart) {
      List<FileItem> items = processor.getFileItemList();
      for (FileItem item : items) {
        if (!item.isFormField()) {
          String filename = getFilename(item);
          LOGGER.debug("item content type {}", item.getContentType());
          LOGGER.debug("item filename {}", filename);
          //TODO add user logged to the package
          PackageData pack = toPackageData(item, filename, model);
          LOGGER.debug("pack {}", pack != null ? pack.id() : "null");
          if (pack != null) {
            pack.saveProperties();
            packs.add(pack);
          }
        }
      }

      LOGGER.debug("Adding parameters to package.");
      Map<String, String> formParameters = processor.getParameters();
      Map<String, String[]> urlParameters = req.getParameterMap();
      Map<String, String> parameters = mixParameters(formParameters, urlParameters);
      LOGGER.debug("Number of parameters {}", parameters.size());
      // Add the parameter to each pack
      for (PackageData pack : packs) {
        for (Entry<String, String> parameter : parameters.entrySet()) {
          pack.setParameter(parameter.getKey(), parameter.getValue());
          pack.saveProperties();
        }
      }
    }
    // Return package data
    return packs;
  }

  /**
   * Mix parameters.
   *
   * @param formParameters the form parameters
   * @param urlParameters the url parameters
   * @return the map
   */
  private Map<String, String> mixParameters (Map<String, String> formParameters, Map<String, String[]> urlParameters) {
    Map<String, String> parameters = new HashMap<>();

    if (formParameters != null) {
      parameters.putAll(formParameters);
    }

    return BerliozOXUtils.flattenParameters(urlParameters, parameters);
  }



  /**
   * Copy to.
   *
   * @param stream the stream
   * @param file the file
   * @return the int
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws OXException the OX exception
   */
  private int copyTo(InputStream stream, File file) throws IOException, OXException {
    LOGGER.debug("Writing file: {}", file != null ? file.getAbsolutePath() : "null");
    if (file == null || file.isDirectory()) throw new OXException(OXErrorMessage.FILE_NOT_SELECTED);
    int copied = 0;
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(file);
      copied = IOUtils.copy(stream, os);
    } finally {
      IOUtils.closeQuietly(os);
    }
    return copied;
  }

  /**
   * To type.
   *
   * @param filename the specified file
   * @return the type of specified file.
   */
  private String toType(String filename) {
    if (filename == null) { throw new NullPointerException("file name cannot be null"); }
    String lcfilename = filename.toLowerCase();
    if (lcfilename.endsWith("xml")) {
      return "xml";
    } else if (lcfilename.endsWith("docx")) {
      return "docx";
    } else if (lcfilename.endsWith("html")) {
      return "html";
    } else if (lcfilename.endsWith("htm")) {
      return "html";
    } else if (lcfilename.endsWith("psml")) {
      return "psml";
    } else if (lcfilename.endsWith("zip")) {
      return "zip";
    } else {
      return filename.substring(filename.lastIndexOf(".") + 1);
    }
  }

  /**
   * To name.
   *
   * @param filename the filename
   * @return the name of file without extension.
   */
  private String toName(String filename) {
    int dot = filename.lastIndexOf('.');
    return filename.substring(0, dot);
  }

  /**
   * Create a new package data from the specified file item if possible.
   *
   * @param item the item
   * @param filename the filename
   * @return the package data
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws OXException the OX exception
   */
  private PackageData toPackageData(FileItem item, String filename, String model) throws IOException, OXException {
    if (StringUtils.isBlank(filename)) throw new OXException(OXErrorMessage.FILE_NOT_SELECTED);
    LOGGER.debug("Starts toPackageData {}/{}", model, filename);
    InputStream stream = item.getInputStream();

    File dir = getTempUploadDirectory();
    LOGGER.debug("Temp directory: {}", dir.getAbsolutePath());
    if (!dir.exists()) {
      dir.mkdirs();
    }
    LOGGER.debug("Is form field: {}", item.isFormField());
    File file = new File(dir, filename);
    LOGGER.debug("Temp file: {}", file.getAbsolutePath());
    int copied = copyTo(stream, file);
    //TODO Add session
    PackageData pack = PackageData.newPackageData(model, file);
    //TODO This property is used to create the package data (change this logic).
    pack.setProperty("contenttype", item.getContentType());
    pack.setProperty("type", toType(filename));
    pack.setProperty("name", toName(filename));
    LOGGER.debug("Filename {}.", filename);
    if (copied == 0) {
      LOGGER.debug("Deleting file {}.", dir.getAbsolutePath());
      FileUtils.deleteDirectory(dir);
      throw new OXException(OXErrorMessage.FILE_IS_EMPTY);
    }
    LOGGER.debug("Ends toPackageData {}/{}", model, filename);
    return pack;
  }

  /**
   * Gets the filename.
   *
   * @param item the item
   * @return the filename
   * @throws OXException the OX exception
   */
  private String getFilename(FileItem item) throws OXException {
    String filename = item.getName();
    LOGGER.debug("Original filename {}", filename);
    if (!StringUtils.isBlank(filename)) {
      //It is necessary because the Internet Explore and Edge send the full path of the file
      //the this method remove all unnecessary path and returns the file name.
      filename = FilenameUtils.getName(filename);
      LOGGER.debug("Cleaned filename {}", filename);
    } else {
      LOGGER.debug("The uploaded file name is empty it may be because any file has been selected.");
      throw new OXException(OXErrorMessage.FILE_NOT_SELECTED);
    }
    return filename;
  }

  /**
   * Gets the temp upload directory.
   *
   * @return the temp upload directory
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private File getTempUploadDirectory() throws IOException {
    File tempUploadOX = OXConfig.getOXTempUploadFolder();
    File tempDirectory = Files.createTempDirectory(tempUploadOX.toPath(), "upload").toFile();
    LOGGER.debug("Temporary upload directory {}", tempDirectory.getAbsolutePath());
    return tempDirectory;
  }
}
