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
package org.pageseeder.ox.diffx.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

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
