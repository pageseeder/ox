/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.util;

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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXErrorMessage;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.Pipeline;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.core.StepJob;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file file handler to receive the data from user upload.
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class FileHandler {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

  /**
   * To pipeline jobs.
   *
   * @param packs the packs
   * @return the list
   */
  public static List<PipelineJob> toPipelineJobs(String modelName, List<PackageData> packs) {
    ensureConfigured();
    List<PipelineJob> jobs = new ArrayList<PipelineJob>();
    Model model = new Model(modelName);
    LOGGER.debug("Model {} ", model.name());
    long slowSize = GlobalSettings.get("ox2.slow-mode.size", -1);
    long maxInactiveTimeAllowed = Long.parseLong(GlobalSettings.get("ox2.max-inactive-time-ms",
        String.valueOf(StepJob.DEFAULT_MAX_INACTIVE_TIME_MS)));
    LOGGER.debug("Started creating the Pipeline Jobs");
    for (PackageData pack : packs) {
      boolean isSlowMode = slowSize > 0 && pack.getOriginal().exists() && (pack.getOriginal().length() - slowSize * 1024 > 0);
      LOGGER.debug("slow mode {}", isSlowMode);
      String p = pack.getParameter("pipeline");
      if (p != null) {
        Pipeline pipeline = model.getPipeline(p);
        if (pipeline != null) {
          PipelineJob job = new PipelineJob(pipeline, pack);
          job.setSlowMode(isSlowMode);
          job.setMaxInactiveTimeAllowed(maxInactiveTimeAllowed);
          jobs.add(job);
        } else {
          LOGGER.warn("pipeline {} not found", p);
        }
      } else {
        Pipeline pipeline = model.getPipelineDefault();
        PipelineJob job = new PipelineJob(pipeline, pack);
        job.setSlowMode(isSlowMode);
        jobs.add(job);
      }
    }
    LOGGER.debug("Ended creating the Pipeline Jobs");

    return jobs;
  }

  /**
   * Receive.
   *
   * @param req the ContentRequest
   * @return the list of PackageData
   * @throws IOException when I/O error occur.
   * @throws OXException the OX exception
   */
  public static List<PackageData> receive(String model, HttpServletRequest req) throws IOException, OXException {
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
  private static Map<String, String> mixParameters (Map<String, String> formParameters, Map<String, String[]> urlParameters) {
    Map<String, String> parameters = new HashMap<String, String>();

    if (formParameters != null) {
      parameters.putAll(formParameters);
    }

    if (urlParameters != null) {
      for(Entry<String, String[]> param:urlParameters.entrySet()) {
        if (!parameters.containsKey(param.getKey())) {
          parameters.put(param.getKey(), StringUtils.convertToString(param.getValue(), ","));
        }
      }
    }

    return parameters;
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
  private static final int copyTo(InputStream stream, File file) throws IOException, OXException {
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
   * To name.
   *
   * @param filename the filename
   * @return the name of file without extension.
   */
  private static String toName(String filename) {
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
  private static PackageData toPackageData(FileItem item, String filename, String model) throws IOException, OXException {
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
   * Ensure the configuration file is set.
   */
  private static void ensureConfigured() {
    OXConfig config = OXConfig.get();
    File dir = config.getModelsDirectory();
    LOGGER.debug("Model Directory is null {}", dir == null);
    if (dir == null) {
      LOGGER.debug("Global Settings {}", GlobalSettings.getAppData());
      config.setModelsDirectory(new File(GlobalSettings.getAppData(), "model"));
    }
  }

  /**
   * Gets the filename.
   *
   * @param item the item
   * @return the filename
   * @throws OXException the OX exception
   */
  private static String getFilename(FileItem item) throws OXException {
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
  private static File getTempUploadDirectory() throws IOException {
    File tempUploadOX = OXConfig.getOXTempUploadFolder();
    File tempDirectory = Files.createTempDirectory(tempUploadOX.toPath(), "upload").toFile();
    LOGGER.debug("Temporary upload directory {}", tempDirectory.getAbsolutePath());
    return tempDirectory;
  }
}
