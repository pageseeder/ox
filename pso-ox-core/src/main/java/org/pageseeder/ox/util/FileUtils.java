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
package org.pageseeder.ox.util;

import org.jetbrains.annotations.NotNull;
import org.pageseeder.ox.core.PackageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class FileUtils.
 *
 * @author Adriano Akaishi
 * @since 01 /05/2017
 */
public class FileUtils {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

  /**
   * Copy.
   *
   * @param source the source file or directory.
   * @param target the target file or directory.
   * @throws IOException when IO error occur.
   */
  public static void copy(final File source, final File target) throws IOException {
    if (source == null) throw new NullPointerException("source directory is null.");
    if (target == null) throw new NullPointerException("target directory is null.");

    if (source.isFile()) {
      File parentFolder = target.getParentFile();
      if (parentFolder != null) {
        parentFolder.mkdirs();
      }
      Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    } else if (source.isDirectory()) {
      Files.walkFileTree(source.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
          Files.createDirectories(target.toPath().resolve(source.toPath().relativize(dir)));
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          Files.copy(file, target.toPath().resolve(source.toPath().relativize(file)), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  /**
   * Delete.
   *
   * @param target the file or directory needs to delete
   * @throws IOException when IO error occur.
   */
  public static void delete(final File target) throws IOException {
    // remove the file if exists.
    if (target.exists() && target.isFile()) {
      LOGGER.debug("Deleting file {}.", target.getAbsolutePath());
      target.delete();
    } else if (target.exists() && target.isDirectory()) {
      //if it's directory
      Files.walkFileTree(target.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          LOGGER.debug("Deleting file {}.", file.getFileName());
          file.toFile().delete();
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          LOGGER.debug("Deleting file {}.", dir.getFileName());
          dir.toFile().delete();
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  /**
   * Returns the file extension.
   *
   * @param file the file
   * @return the file extension
   */
  public static String getFileExtension (final File file) {
    String extension = "";
    if (file != null) {
      int lastIndexOf = file.getName().lastIndexOf(".");
      if (lastIndexOf>0) {
        extension = file.getName().substring(lastIndexOf + 1);
      }
    }
    return extension;
  }

  /**
   * Returns the MIME type according file extension.
   *
   * @param file the file
   * @return the mime type
   */
  public static String getMimeType (final File file){
    String mimeType = "unknown";
    if (file != null && file.exists()) {
      String extension = FileUtils.getFileExtension(file);
      switch (extension.toLowerCase()) {
      case "css":
        mimeType = "text/css";
        break;
      case "csv":
        mimeType = "text/csv";
        break;
      case "docx":
        mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        break;
      case "epub":
        mimeType = "application/epub+zip";
        break;
      case "html":
        mimeType = "text/html";
        break;
      case "jpg":
      case "jpeg":
        mimeType = "image/jpeg";
        break;
      case "js":
        mimeType = "application/javascript";
        break;
      case "pdf":
        mimeType = "application/pdf";
        break;
      case "png":
        mimeType = "image/png";
        break;
      case "psml":
        mimeType= "application/vnd.pageseeder.psml+xml";
        break;
      case "svg":
        mimeType = "image/svg+xml";
        break;
      case "xlsx":
        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        break;
      case "xml":
        mimeType = "application/xml";
        break;
      case "zip":
        mimeType = "application/zip";
        break;
      default:
        mimeType = "unknown";
      }
    }
    return mimeType;
  }

  /**
   * Gets the file by extension.
   *
   * @param data       the data
   * @param fromFolder the from folder
   * @param extensions the extensions
   * @return the file by extension
   */
  public static String getFileByExtension(PackageData data, String fromFolder, String ... extensions){
    if (extensions != null && extensions.length > 0) {
      File folder = data.getFile(fromFolder);
      if (folder.isDirectory()) {
        String currentExtension = "";
        String lowerCaseFileName = "";
        for (String filenane : folder.list()){
          lowerCaseFileName = filenane.toLowerCase();
          for (int i = 0; i < extensions.length; i++) {
            currentExtension = extensions[i];
            if(!StringUtils.isBlank(currentExtension) && lowerCaseFileName.endsWith(currentExtension.toLowerCase())) {
              fromFolder += "/" + filenane;
              break;
            }
          }
        }
        currentExtension = null;
        lowerCaseFileName = null;
      }
    }
    return fromFolder;
  }

  /**
   * write the file content into the target.
   *
   * @param fileContent the file content
   * @param target      the target
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void write(String fileContent, File target) throws IOException {
    write(fileContent, target, "UTF-8");
  }


  /**
   * write the file content into the target.
   *
   * @param fileContent the file content
   * @param target      the target
   * @param charset     the charset
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void write(String fileContent, File target, String charset) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(target);
         OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
         Writer writer = new BufferedWriter(osw)) {
      writer.write(fileContent);
    }
  }

  /**
   * Returns the content of the source file.
   *
   * @param source the source
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String read(File source) throws IOException {
    return read(source, "UTF-8");
  }

  /**
   * Returns the content of the source file.
   *
   * @param source  the source
   * @param charset the charset
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String read(File source, String charset) throws IOException {
    Charset encoding = Charset.forName(charset);
    byte[] encoded = Files.readAllBytes(source.toPath());
    return new String(encoded, encoding);
  }

  /**
   * Compute the relative path between two files:
   * root             descendant            returned
   * /a/b/c           /a/b/c/d.txt          d.txt
   * /a/b/c           /a/b/c/d/e.txt        d/e.txt
   * /a/b/c           /a/b/d.txt            ''
   *
   * @param descendant the descendant file
   * @param root       the root folder
   * @return the relative path if files match, empty string otherwise
   */
  public static String relativePath(File descendant, File root) {
    try {
      String rootPath = root.getCanonicalPath().replace('\\', '/');
      String descendantPath = descendant.getCanonicalPath().replace('\\', '/');
      if (descendantPath.startsWith(rootPath)) return descendantPath.substring(rootPath.length()+1);
      return "";
    } catch (IOException ex) {
      return "";
    }
  }

  // ------------------------------------------------------------------
  // find files methods
  // ------------------------------------------------------------------

  /**
   * Find files.
   *
   * @param root   the root
   * @param filter the filter
   * @return the list
   */
  public static @NotNull List<File> findFiles(File root, FileFilter filter) {
    if (!root.exists()) return Collections.emptyList();
    if (root.isFile()) return Collections.singletonList(root);
    List<File> all = new ArrayList<>();
    for (File f : root.listFiles(filter)) {
      if (f.isDirectory()) all.addAll(findFiles(f, filter));
      else all.add(f);
    }
    return all;
  }

  /**
   * Find files.
   *
   * @param root      the root
   * @param extension the extension
   * @return the list
   */
  public static List<File> findFiles(File root, String extension) {
    return findFiles(root, filter(extension));
  }

  /**
   * Filter.
   *
   * @param ext the ext
   * @return the file filter
   */
  private static FileFilter filter(String ext) {
    if (ext == null) return null;
    final String extension = ext.charAt(0) == '.' ? ext.toLowerCase() : '.' + ext.toLowerCase();
    return f -> f.isDirectory() || f.getName().toLowerCase().endsWith(extension);
  }

  /**
   * Filter.
   *
   * @param extensionsAllowed  the extensions allowed
   * @param isDirectoryAllowed the is directory allowed
   * @return the file filter
   */
  public static FileFilter filter(@NotNull List<String> extensionsAllowed, boolean isDirectoryAllowed) {
    if (extensionsAllowed.isEmpty()) return null;
    return f -> {
      boolean isAllowed = false;
      if (f.isDirectory() && isDirectoryAllowed) {
        isAllowed = true;
      } else {
        for (String extension:extensionsAllowed) {
          if (!StringUtils.isBlank(extension) && f.getName().toLowerCase().endsWith(extension.toLowerCase())) {
            isAllowed = true;
            break;
          }
        }
      }
      return isAllowed;
    };
  }

  /**
   * Checks if is zip (extension == zip).
   *
   * @param candidate the candidate
   * @return true, if is zip
   */
  public static boolean isZip(File candidate) {
    return candidate.getName().toLowerCase().endsWith(".zip");
  }

  /**
   * Gets the XML extensions.
   *
   * @return the XML extensions
   */
  public static List<String> getXMLExtensions() {
    List<String> extensions = new ArrayList<>();
    extensions.add("xml");
    extensions.add("html");
    extensions.add("htm");
    extensions.add("psml");
    return extensions;
  }


  /**
   * Gets the name without extension.
   *
   * @param file the file
   * @return the name without extension
   */
  public static  String getNameWithoutExtension(File file){
    String fileName = file.getName();
    return getNameWithoutExtension(fileName);
  }

  /**
   * Gets the name without extension.
   *
   * @param fileNameAndExtension the file name and extension
   * @return the name without extension
   */
  public static String getNameWithoutExtension(String fileNameAndExtension ){
    String fileName = "";

    if(fileNameAndExtension.lastIndexOf(".") != -1 && fileNameAndExtension.lastIndexOf(".") != 0){
        fileName = fileNameAndExtension.substring(0, fileNameAndExtension.lastIndexOf("."));
    }

    return fileName;
  }
}
