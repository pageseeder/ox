/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.Pipeline;
import org.pageseeder.ox.core.PipelineJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file file handler to receive the data from user upload.
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class FileHandler {

  private static Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

  public static List<PipelineJob> toPipelineJobs(String model, List<PackageData> packs) {
    List<PipelineJob> jobs = new ArrayList<PipelineJob>();
    ensureConfigured();
    Model m = new Model(model);

    long slowSize = GlobalSettings.get("ox2.slow-mode.size", -1);

    for (PackageData pack : packs) {
      boolean isSlowMode = slowSize > 0 && pack.getOriginal().exists() && (pack.getOriginal().length() - slowSize * 1024 > 0);
      LOGGER.debug("slow mode {}", isSlowMode);
      String p = pack.getParameter("pipeline");
      if (p != null) {
        Pipeline pipeline = m.getPipeline(p);
        if (pipeline != null) {
          PipelineJob job = new PipelineJob(pipeline, pack);
          job.setSlowMode(isSlowMode);
          jobs.add(job);
        } else {
          LOGGER.warn("pipeline {} not found", p);
        }
      } else {
        for (int i = 0; i < m.size(); i++) {
          Pipeline pipeline = m.getPipeline(i);
          PipelineJob job = new PipelineJob(pipeline, pack);
          job.setSlowMode(isSlowMode);
          jobs.add(job);
        }
      }

    }

    return jobs;
  }

  /**
   * @param model the model name
   * @param req the ContentRequest
   * @return the list of PackageData
   * @throws IOException when I/O error occur.
   * @throws OXException
   */
  public static List<PackageData> receive(String model, HttpServletRequest req) throws IOException, OXException {
    List<PackageData> packs = new ArrayList<PackageData>();
    // parse the upload request
    UploadProcessor processor = null;
    try {
      UploadFactory factory = UploadFactory.getInstance();
      processor = factory.make(req);
    } catch (SizeLimitExceededException ex) {
      LOGGER.warn("File size exceeds upload limit please choose another file.", ex);
      throw new OXException("File size exceeds the limit of " + GlobalSettings.get("ox2.upload.max-size", 10) + "MB.", ex);
    } catch (InvalidContentTypeException ex) {
      LOGGER.warn("Invalid content type.", ex);
      throw new OXException("Invalid content type.", ex);
    } catch (FileUploadException ex) {
      throw new OXException("Cannot process the upload request.", ex);
    }

    // Check that we have a file upload request
    boolean isMultipart = processor.isMultipart();

    if (isMultipart) {
      List<FileItem> items = processor.getFileItemList();
      for (FileItem item : items) {
        if (!item.isFormField()) {
          PackageData pack = null;
          LOGGER.debug("item content type {}", item.getContentType());
          //if (!"application/octet-stream".equals(item.getContentType())) {
          pack = toPackageData(model, item, item.getName());
          LOGGER.debug("pack {}", pack != null ? pack.id() : "null");
          //}
          if (pack != null) {
            String filename = item.getName();
            LOGGER.debug("Original filename {}", filename);
            if (!StringUtils.isBlank(filename)) {
              //It is necessary because the Internet Explore and Edge send the full path of the file
              //the this method remove all unnecessary path and returns the file name.
              filename = FilenameUtils.getName(filename);
              LOGGER.debug("Cleaned filename {}", filename);
            }
            pack.setProperty("contenttype", item.getContentType());
            pack.setProperty("type", toType(filename));
            pack.setProperty("name", toName(filename));
            pack.saveProperties();
            packs.add(pack);
          }
        }
      }

      Map<String, String> parameters = processor.getParameters();
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
   * @param stream
   * @param file
   * @return
   * @throws IOException
   */
  private static final int copyTo(InputStream stream, File file) throws IOException {
    LOGGER.info("Writing file: {}", file.getAbsolutePath());
    FileOutputStream os = null;
    int copied = 0;
    try {
      os = new FileOutputStream(file);
      copied = IOUtils.copy(stream, os);
    } finally {
      IOUtils.closeQuietly(os);
    }
    return copied;
  }

  /**
   * @param filename the specified file
   * @return the type of specified file.
   */
  private static String toType(String filename) {
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
   * @return the name of file without extension.
   */
  private static String toName(String filename) {
    int dot = filename.lastIndexOf('.');
    return filename.substring(0, dot);
  }

  /**
   * Create a new package data from the specified file item if possible.
   *
   * @param item
   * @return
   * @throws IOException
   */
  private static PackageData toPackageData(String model, FileItem item, String filename) throws IOException {

    InputStream stream = item.getInputStream();
    File dir = File.createTempFile("ox.allette.berlioz", ".tmp").getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File file = new File(dir, item.isFormField() ? filename : item.getName());
    int copied = copyTo(stream, file);
    PackageData pack = PackageData.newPackageData(model, file);
    if (copied == 0) {
      FileUtils.deleteDirectory(dir);
      pack = null;
    }
    return pack;
  }

  /**
   * Ensure the configuration file is set
   */
  private static void ensureConfigured() {
    OXConfig config = OXConfig.get();
    File dir = config.getModelsDirectory();
    if (dir == null) {
      config.setModelsDirectory(new File(GlobalSettings.getRepository(), "model"));
    }
  }

}
