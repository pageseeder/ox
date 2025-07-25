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
package org.pageseeder.ox.berlioz.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.OXBerliozErrorMessage;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>The upload manager for file upload.</p>
 *
 * @author Ciber Cai
 * @version 04 March 2013
 */
public final class UploadProcessor {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(UploadProcessor.class);

  /** The Constant ONE_MB. */
  private static final int ONE_MB = 1024 * 1024; // 1MB

  /** The accept extension. */
  private final String acceptExtension;

  /** The progress listener. */
  private final FileUploadListener progressListener;

  /** The items. */
  private final List<FileItem> items;

  /** The is multipart. */
  private final boolean isMultipart;

  /**
   * A default constructor for UploadProcessor.
   *
   * @param req the ContentRequest
   * @throws FileUploadException when FilUpload occurs
   * @throws OXException         the ox exception
   */
  protected UploadProcessor(HttpServletRequest req) throws FileUploadException, OXException {
    LOGGER.debug("Instantiate Upload Processor");
    String contentType = req.getContentType();
    LOGGER.debug("Request Content Type '{}'", contentType);
    if (StringUtils.isBlank(contentType) || !contentType.startsWith(FileUploadBase.MULTIPART))
      throw new OXException(OXBerliozErrorMessage.REQUEST_IS_NOT_MULTIPART);

    int thresholdSize = GlobalSettings.get("ox2.upload.max-size", 10) * ONE_MB;
    long maxFileSize = GlobalSettings.get("ox2.upload.max-size", 10) * ONE_MB; // Sets the maximum allowed size of a single uploaded file
    long requestSize = GlobalSettings.get("ox2.upload.max-size", 10) * ONE_MB; // Sets the maximum allowed size of a complete request
    this.acceptExtension = GlobalSettings.get("upload.file.accept-extension", "all");
    LOGGER.debug("Instantiate Upload Processor thresholdSize/maxFileSize/requestSize/acceptExtension: {}/{}/{}/{}", thresholdSize, maxFileSize, requestSize, this.acceptExtension);

    // Create a factory for disk-based file items
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(thresholdSize);
    factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
    LOGGER.debug("Disk File Item factory repository {}", factory.getRepository().getAbsolutePath());
    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setFileSizeMax(maxFileSize);
    upload.setSizeMax(requestSize);
    // Create a progress listener
    this.progressListener = new FileUploadListener();
    upload.setProgressListener(this.progressListener);

    // parse the request to get the FileItem
    this.items = upload.parseRequest(req);
    LOGGER.debug("Number of items found {}", this.items != null? items.size():0);

    // check whether is multipart content
    this.isMultipart = ServletFileUpload.isMultipartContent(req);
  }

  /**
   * *.
   *
   * @return the list of FileItem.
   */
  public List<FileItem> getFileItemList() {
    return this.items;
  }

  /**
   * *.
   *
   * @return the ProgressListener.
   */
  public FileUploadListener getProgressListener() {
    return this.progressListener;
  }

  /**
   * Progress.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void progress(XMLWriter xml) throws IOException {
    xml.openElement("uplad");
    xml.attribute("id", "jobid");
    xml.attribute("precentage", String.valueOf(this.progressListener.percentage()));
    xml.closeElement();
  }

  /**
   * Checks if is multipart.
   *
   * @return true, if is multipart
   */
  public boolean isMultipart() {
    return this.isMultipart;
  }

  /**
   * *.
   *
   * @param filename the filename
   * @return the status whether it allows to upload.
   */
  public boolean isAllow(String filename) {
    if ("all".equals(this.acceptExtension) || this.acceptExtension == null || this.acceptExtension.isEmpty()) {
      return true;
    } else {
      for (String ext : this.acceptExtension.split(",")) {
        if (filename.endsWith("." + ext)) { return true; }
      }
    }
    return false;
  }

  /**
   * Checks if is supported type.
   *
   * @return true, if is supported type
   */
  public boolean isSupportedType() {
    Iterator<FileItem> iter = this.items.iterator();
    if (iter != null) {
      while (iter.hasNext()) {
        FileItem item = iter.next();
        if (!item.isFormField()) {
          String fileName = new File(item.getName()).getName();
          LOGGER.debug("File name {}, Cotent Type {} ", fileName, item.getContentType());
          if (!isAllow(fileName)) { return false; }
        }
      }
    }
    return true;
  }

  /**
   * Gets the parameters.
   *
   * @return the parameters
   */
  public Map<String, String> getParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    if (this.items != null) {
      Iterator<FileItem> iter = this.items.iterator();
      while (iter.hasNext()) {
        FileItem item = iter.next();
        if (item.isFormField()) {
          String n = item.getFieldName();
          String v = item.getString();
          parameters.put(n, v);
        }
      }
    }
    return parameters;
  }

  /**
   * *
   * Return the parameter from FileItem.
   *
   * @param name the name of the parameter.
   * @param def  the default value of the parameter.
   * @return the specified parameter value.
   */
  public String getParameter(String name, String def) {
    // Process the uploaded items
    String value = null;
    if (this.items != null) {
      Iterator<FileItem> iter = this.items.iterator();
      while (iter.hasNext()) {
        FileItem item = iter.next();
        if (item.isFormField()) {
          String n = item.getFieldName();
          if (n.equals(name)) {
            value = item.getString();
            break;
          }
        }
      }
    }

    if (value == null) {
      return def;
    } else {
      return value;
    }
  }

  /**
   * *
   * Return the parameter from FileItem.
   *
   * @param name the name of the parameter.
   * @return the specified parameter value.
   */
  public String getParameter(String name) {
    return getParameter(name, null);
  }

  /**
   * Gets the total upload size.
   *
   * @return the upload file size
   */
  public long getTotalUploadSize() {
    long total = 0;
    if (this.items != null) {
      Iterator<FileItem> iter = this.items.iterator();
      while (iter.hasNext()) {
        FileItem item = iter.next();
        if (!item.isFormField()) {
          total += item.getSize();
        }
      }
    }
    return total;
  }

}
