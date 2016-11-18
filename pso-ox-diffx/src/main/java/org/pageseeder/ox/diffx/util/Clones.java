/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.diffx.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Lauret
 * @since 01 November 2013
 *
 */
public final class Clones {

  private static Logger LOGGER = LoggerFactory.getLogger(Clones.class);

  private static final FileFilter CLONER = new FileFilter() {

    @Override
    public boolean accept(File f) {
      if (f.isDirectory()) {
        Clones.clone(f);
      } else if (!f.getName().endsWith(".clone")) {
        try {
          FileUtils.copyFile(f, new File(f.getParentFile(), f.getName() + ".clone"));
        } catch (IOException ex) {
          LOGGER.error("Cannot clone a file {}", f.getName(), ex);
        }
      }
      return false;
    }
  };

  private static FileFilter CLEANER = new FileFilter() {

    @Override
    public boolean accept(File f) {
      if (f.isDirectory()) {
        clean(f);
      } else if (f.getName().endsWith(".clone")) {
        f.delete();
      }
      return false;
    }
  };

  /**
   *
   */
  private Clones() {}

  public static void clone(File dir) {
    dir.listFiles(CLONER);
  }

  public static void clean(File dir) {
    dir.listFiles(CLEANER);
  }

}
