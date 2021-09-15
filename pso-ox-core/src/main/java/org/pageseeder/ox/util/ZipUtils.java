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

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A utility class for common Zip functions.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  18 December 2013
 */
public final class ZipUtils {

  /**
   * Size of internal buffer.
   */
  private static final int BUFFER = 2048;

  /** Utility class. */
  private ZipUtils() {}

  /**
   * Unzip the the file at the specified location.
   *
   * @param src  The file to unzip
   * @param dest The destination folder
   * @throws IOException when IO error occur.
   */
  public static void unzip(File src, File dest) throws IOException {
    dest.mkdirs();
    BufferedOutputStream out = null;
    BufferedInputStream is = null;
    try {
      ZipEntry entry;
      ZipFile zip = new ZipFile(src);
      for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
        entry = e.nextElement();
        String name = entry.getName();
        // Ensure that the folder exists
        if (name.indexOf('/') > 0) {
          String folder = name.substring(0, name.lastIndexOf('/'));
          File dir = new File(dest, folder);
          if (!dir.exists()) {
            dir.mkdirs();
          }
        }
        // Only process files
        if (!entry.isDirectory()) {
          is = new BufferedInputStream(zip.getInputStream(entry));
          int count;
          byte[] data = new byte[BUFFER];
          File f = new File(dest, name);
          try (FileOutputStream fos = new FileOutputStream(f)) {
            out = new BufferedOutputStream(fos, BUFFER);
            while ((count = is.read(data, 0, BUFFER)) != -1) {
              out.write(data, 0, count);
            }
            out.flush();
          }
          is.close();
        }
      }
      zip.close();
    } finally {
      // If the ZIP is empty these may be null
      if (is != null) {
        is.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Zip the specified file or folder.
   *
   * @param src  The folder to zip
   * @param dest The destination zip
   * @throws IOException when IO error occur.
   */
  public static void zip(File src, File dest) throws IOException {
    ZipOutputStream out = null;
    try {
      FileOutputStream zip = new FileOutputStream(dest);
      out = new ZipOutputStream(new BufferedOutputStream(zip));
      if (src.isFile()) {
        // Source is a single file
        addToZip(src, out, null);

      } else {
        // Source is directory
        for (File f : src.listFiles()) {
          addToZip(f, out, null);
        }
      }

    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Zip the a set of specified file or folder.
   *
   * @param dest The destination zip
   * @param sources  The list of files
   * @throws IOException when IO error occur.
   */
  public static void zipFilesTo(File dest, File... sources) throws IOException {
    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)))){
      for (File src : sources) {
        if (src.isFile()) {
          // Source is a single file
          addToZip(src, out, null);
        } else {
          // Source is directory
          for (File f : src.listFiles()) {
            addToZip(f, out, null);
          }
        }
      }
    }
  }

  /**
   * Zip the specified file or folder.
   *
   * @param file   The file or folder to zip
   * @param out    The destination zip stream
   * @param folder The current folder
   *
   * @throws IOException If an IO error occurs.
   */
  private static void addToZip(File file, ZipOutputStream out, String folder) throws IOException {
    // Directory
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) {
          addToZip(f, out, (folder != null ? folder + file.getName() : file.getName()) + "/");
        }
      }

      // File
    } else {
      byte[] data = new byte[BUFFER];
      BufferedInputStream origin = null;
      try {
        FileInputStream fi = new FileInputStream(file);
        origin = new BufferedInputStream(fi, BUFFER);
        ZipEntry entry = new ZipEntry(folder != null ? folder + file.getName() : file.getName());
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
          out.write(data, 0, count);
        }
      } finally {
        if (origin != null) {
          origin.close();
        }
      }
    }
  }

}
