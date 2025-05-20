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
package org.pageseeder.ox.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.util.*;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

/**
 * Represents an abstract data package
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since 29 October
 */
public final class PackageData implements XMLWritable, Serializable {

  private static final long serialVersionUID = -5023724404877761495L;

  /** the logger */
  private final static Logger LOGGER = LoggerFactory.getLogger(PackageData.class);

  /**
   * The name of the properties file containing all the information about this package.
   */
  private final static String PROPERTIES_FILENAME = "info.properties";

  /**
   * The property name of original file.
   */
  public final static String ORIGINAL_PROPERTY = "_original_file";

  /**
   * When the package was created
   */
  private final long _created;

  /**
   * ID of the package
   */
  private final String _id;

  /**
   * the original file
   */
  private final File _orig;

  /**
   * The mime type of the package
   */
  private final String _mediaType;

  /**
   * The directory containing the package
   */
  private final File _dir;

  /**
   * Properties for the package.
   */
  private final Properties _properties = new Properties();

  /**
   * @param created the package date to create.
   * @param id the id of package
   * @param file the original input file.
   */
  private PackageData(long created, String id, File file) {
    this._created = created;
    this._id = id;
    this._mediaType = getMediaType(file);
    this._dir = getPackageDirectory(this._id);
    this._orig = file != null ? store(file, this._dir) : null;
    if (this._orig != null) {
      this._properties.setProperty(ORIGINAL_PROPERTY, this._orig.getName());
    }
  }

  /**
   * Created date.
   *
   * @return the created {@link Date }
   */
  public Date created() {
    return new Date(this._created);
  }

  /**
   * Id string.
   *
   * @return The package id
   */
  public String id() {
    return this._id;
  }

  /**
   * Directory file.
   *
   * @return The directory containing the package
   */
  public File directory() {
    return this._dir;
  }

  /**
   * Gets file.
   *
   * @param path the relative path in the package
   * @return the File.
   */
  @Nullable
  public File getFile(String path) {
    List<File> files = getFiles(path);
    File file = null;
    if (!files.isEmpty()) {
      file = files.get(0);
    }
    return file;
  }

  /**
   * Gets files.
   *
   * @param path the relative path in the package
   * @return the File.
   */
  @NonNull
  public List<File> getFiles(String path) {
    List<File> files = new ArrayList<>();
    if (!StringUtils.isBlank(path)) {
      // check if single file first (as some filenames can have ',' or '*' in them
      if (new File(this._dir, path).exists()) {
        files.add(new File(this._dir, path));
      } else if (StringUtils.isCommaSeparateFileList(path)) {
        //It is a list of files separated by comma
        for (String eachInput:path.split(",")) {
          if (!StringUtils.isBlank(eachInput)) {
            files.add(new File(this._dir, eachInput));
          }
        }
      } else if (GlobPatternUtils.isGlobPattern(path)) {
        //The path has Glob pattern
        FilesFinder finder = new FilesFinder(path, this._dir);
        files = finder.getFiles();
      } else {
        // add it anyway and let the caller decide what to do when it doesn't exist
        files.add(new File(this._dir, path));
      }
    }
    return files;
  }


  /**
   * Gets path.
   *
   * @param file the file in {@link PackageData}
   * @return the path in {@link PackageData}
   */
  public String getPath(File file) {

    if (file != null && file.getAbsolutePath().startsWith(this._dir.getAbsolutePath())) {
      return sanity(file.getAbsolutePath().substring(this._dir.getAbsolutePath().length()));
    }
    return null;
  }

  /**
   * Find by extension file.
   *
   * @param extension the file extension.
   * @return the first file by using the extension.
   */
  public File findByExtension(String extension) {
    File[] files = this._dir.listFiles(new FilterByExtension(extension));
    return files != null && files.length > 0 ? files[0] : null;
  }

  /**
   * List by extension list.
   *
   * @param extension the file extension
   * @return the list of files by using this extension.
   */
  public List<File> listByExtension(String extension) {
    File[] files = this._dir.listFiles(new FilterByExtension(extension));
    return Arrays.asList(files);
  }

  /**
   * Gets original.
   *
   * @return the Original file
   */
  public File getOriginal() {
    return this._orig;
  }

  /**
   * Indicates whether the package contains an unpacked directory.
   *
   * @return <code>true</code> the package contains an unpacked directory;         <code>false</code> otherwise.
   */
  public boolean isUnpacked() {
    return new File(this._dir, "unpacked").exists();
  }

  // Parameters
  // ----------------------------------------------------------------------------------------------

  /**
   * Sets parameter.
   *
   * @param name  the name of parameter
   * @param value the value of parameter
   */
  public void setParameter(String name, String value) {
    this._properties.setProperty("parameter-" + name, value);
  }

  /**
   * Gets parameter.
   *
   * @param name the name of parameter
   * @return the value of parameter
   */
  public String getParameter(String name) {
    return this._properties.getProperty("parameter-" + name);
  }

  /**
   * Gets parameters.
   *
   * @return the parameters in map
   */
  public Map<String, String> getParameters() {
    Map<String, String> parameters = new HashMap<>();
    for (Object key : this._properties.keySet()) {
      String property = key.toString();
      if (property.startsWith("parameter-")) {
        String name = property.substring("parameter-".length());
        String value = this._properties.getProperty(property);
        parameters.put(name, value);
      }
    }
    return parameters;
  }

  // Properties
  // ----------------------------------------------------------------------------------------------

  /**
   * Gets properties.
   *
   * @return the properties maps.
   */
  public Map<String, String> getProperties() {
    Map<String, String> properties = new HashMap<String, String>();
    for (Entry<Object, Object> entry : this._properties.entrySet()) {
      String key = (String) entry.getKey();
      if (!key.startsWith("parameter-")) {
        properties.put(key, (String) entry.getValue());
      }
    }
    return properties;
  }

  /**
   * Gets property.
   *
   * @param name the name of the property
   * @return the value of the specified property
   */
  public String getProperty(String name) {
    return this._properties.getProperty(name);
  }

  /**
   * Gets property.
   *
   * @param name     the name of the property
   * @param fallback the default value
   * @return the value of the specified property
   */
  public String getProperty(String name, String fallback) {
    return this._properties.getProperty(name, fallback);
  }

  /**
   * Sets property.
   *
   * @param name  the name of the property
   * @param value the value of the property
   */
  public void setProperty(String name, String value) {
    this._properties.setProperty(name, value);
  }

  /**
   * Load properties boolean.
   *
   * @return the status whether the properties has loaded.
   */
  public boolean loadProperties() {
    File pfile = new File(directory(), PROPERTIES_FILENAME);
    if (pfile.exists()) {
      try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(pfile.toPath()), StandardCharsets.UTF_8)) {
        this._properties.load(reader);
      } catch (IOException ex) {
        LOGGER.error("Cannot load properties from file {}.", pfile, ex);
        return false;
      }
    }
    return true;
  }

  /**
   * Saves the properties of this packages data in the file system.
   *
   * @return the status of save properties .
   */
  public boolean saveProperties() {
    File pfile = new File(directory(), PROPERTIES_FILENAME);
    LOGGER.info("saving properties of {}", this.id());
    try (OutputStreamWriter reader = new OutputStreamWriter(Files.newOutputStream(pfile.toPath()), StandardCharsets.UTF_8)) {
      this._properties.store(reader, "");
    } catch (IOException ex) {
      LOGGER.error("Cannot save properties.");
      return false;
    }
    return true;
  }

  /**
   * Inspect the package
   *
   * @return the status of inspector
   * @throws IOException when io error occur
   */
  public boolean inspect() throws IOException {
    loadProperties();
    LOGGER.info("The media type for this data is {}", this._mediaType);
    List<PackageInspector> inspectors = InspectorService.getInstance().getInspectors(this._mediaType);
    // do the inspect
    if (inspectors!= null && !inspectors.isEmpty()) {
      for (PackageInspector inspector : inspectors) {
        inspector.inspect(this);
      }
    }
    return true;
  }

  /**
   * Find the DOCX document and unpack its content if necessary
   *
   * @return the status of unpacking.
   * @throws IOException the io exception
   */
  public boolean unpack() throws IOException {
    File docx = findByExtension(".docx");
    if (docx != null && !isUnpacked()) {
      ZipUtils.unzip(docx, new File(this._dir, "unpacked"));
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("package-data");
    xml.attribute("created", ISO8601.DATETIME.format(this._created));
    xml.attribute("id", this._id);
    xml.attribute("type", this._properties.getProperty("type", "unknown"));

    // Display package properties
    xml.openElement("properties");
    for (Entry<Object, Object> e : this._properties.entrySet()) {
      xml.openElement("property");
      xml.attribute("name", e.getKey().toString());
      xml.attribute("value", e.getValue().toString());
      xml.closeElement();
    }
    xml.closeElement();

    File original = getOriginal();
    if (original != null) {
      xml.openElement("file");
      xml.attribute("name", original.getName());
      xml.attribute("size", Long.toString(original.length()));
      xml.closeElement();
    }
    xml.closeElement();
  }

  /**
   * Gets download dir.
   *
   * @param download the download
   * @return the download dir
   */
  public File getDownloadDir(File download) {
    File dir = new File(download, this.id());
    dir.mkdirs();
    return dir;
  }

  /**
   * Gets package data.
   *
   * @param id the id of package data
   * @return the {@link PackageData} by specified id
   */
  public static PackageData getPackageData(String id) {
    if (id == null) { throw new NullPointerException("package id cannot be null"); }
    File tmp = OXConfig.getOXTempFolder();
    File dir = new File(tmp, id);
    if (!dir.exists()) { return null; }

    // try to load the original file
    File pfile = new File(dir, PROPERTIES_FILENAME);
    Properties prop = new Properties();
    if (pfile.exists()) {
      try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(pfile.toPath()), StandardCharsets.UTF_8)) {
        prop.load(reader);
      } catch (IOException ex) {
        LOGGER.error("Cannot load properties from file {}.", pfile, ex);
      }
    }
    File orgin = prop.getProperty(ORIGINAL_PROPERTY) != null ? new File(dir, prop.getProperty(ORIGINAL_PROPERTY)) : null;

    PackageData data = new PackageData(dir.lastModified(), id, orgin);
    data.loadProperties();
    return data;
  }

  // Static helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * New package data package data.
   *
   * @param model the name of model.
   * @param file  the original file.
   * @return the {@link PackageData}
   */
  public static PackageData newPackageData(String model, File file) {
    String id = generateID(model);
    LOGGER.debug("Generating a new package data: {}", id);
    PackageData data = new PackageData(System.currentTimeMillis(), id, file);
    data.saveProperties();
    LOGGER.debug("The new package data {} was created.", id);
    return data;
  }

  /**
   * @param source the file needs to copy.
   * @param target The folder to store the file
   * @return the target file.
   */
  private static File store(final File source, final File target) {
    if (source == null) throw new NullPointerException("Source file cannot be null.");
    if (!source.exists()) throw new NullPointerException("Source file does not exist.");
    if (target == null) throw new NullPointerException("Target folder cannot be null.");
    if (!target.isDirectory()) throw new IllegalArgumentException("Target is not a directory.");

    try {
      File targetFile = new File(target, source.getName());
      FileUtils.copy(source, targetFile);
      return targetFile;
    } catch (IOException ex) {
      LOGGER.error("Cannot copy file to target.", ex);
    }
    return null;
  }

  private static File getPackageDirectory(String id) {
    File tmp = OXConfig.getOXTempFolder();
    File dir = new File(tmp, id);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  /**
   * Generate a unique ID for the package
   *
   * @return a unique ID for the package
   */
  private static String generateID(String model) {
    return model.toUpperCase() + "-" + (Long.toHexString(System.currentTimeMillis() % 0xffffff) + '-' + Long.toHexString(Math.round(Math.random() * 0xffff))).toUpperCase();
  }

  /**
   * @return the mime type by filename
   */
  private String getMediaType(File file) {
    String mediaType = "unknown";
    // if it's directory FIXME this could be wrong.
    if (file != null && file.isDirectory()) { mediaType = "text/directory"; }

    if (file != null && file.exists() && file.isFile()) {
      // psml
      if (file.getName().endsWith(".psml")) { mediaType = "application/vnd.pageseeder.psml+xml"; }

      Path path = file.toPath();
      try {
        LOGGER.debug("Path: {}", path);
        String probeContentType = Files.probeContentType(path);

        LOGGER.debug("ProbeContentType: {}", probeContentType);
        if (StringUtils.isBlank(probeContentType)) {
          //TODO The contenttype will always be null because this method is called before the properties are defined.
          LOGGER.debug("Content Type: {}", this.getProperty("contenttype", "unknown"));
          mediaType = this.getProperty("contenttype", FileUtils.getFileExtension(file));
          LOGGER.debug("mediaType: {}", mediaType);
        } else {
          mediaType = probeContentType;
        }
      } catch (IOException ex) {
        LOGGER.warn("Cannot fine media type for {}", path);
      }
    }
    return mediaType;
  }

  private static String sanity(String path) {
    if (path != null) return path.replace("\\", "/");
    return path;
  }

  /**
   * Filters by extension.
   */
  private static final class FilterByExtension implements FileFilter {

    private final String _ext;

    private FilterByExtension(String ext) {
      this._ext = ext;
    }

    @Override
    public boolean accept(File pathname) {
      return pathname.getName().toLowerCase().endsWith(this._ext);
    }
  };
}
