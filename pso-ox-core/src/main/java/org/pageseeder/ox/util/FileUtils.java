/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.pageseeder.ox.core.PackageData;

/**
 * @author Adriano Akaishi
 * @since 01/05/2017
 */
public class FileUtils {

  /**
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
   * @param target the file or directory needs to delete
   * @throws IOException when IO error occur.
   */
  public static void delete(final File target) throws IOException {
    // remove the file if exists.
    if (target.exists() && target.isFile()) {
      target.delete();
    } else if (target.exists() && target.isDirectory()) {
      //if it's directory
      Files.walkFileTree(target.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          file.toFile().delete();
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          dir.toFile().delete();
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }
  
  /**
   * 
   * @param data: package of the data
   * @param fromFolder: directory that It will put the data
   * @param extensions: extensions that it will verified
   * @return
   */
  public static String getFileByExtension(PackageData data, String fromFolder, String ... extensions){
    if (extensions != null && extensions.length > 0) {
      File folder = data.getFile(fromFolder);      
      if (folder.isDirectory()) {
        for (String filenane:folder.list()){
          for (int i = 0; i < extensions.length; i++) {
            if(filenane.endsWith(extensions[i])) {
              fromFolder += "/" + filenane;
              break;
            }
          }
        }
      }
    }
    return fromFolder;
  }


}
