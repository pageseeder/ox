/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.inspector;

import java.io.File;

import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.FileSizeFormat;
import org.pageseeder.ox.util.ISO8601;

/**
 * @author Ciber Cai
 * @since 19 July 2016
 */
public class FileInspector implements PackageInspector {

  private final static String PREFIX = "file.";

  @Override
  public String getName() {
    return "ox-file-inspector";
  }

  @Override
  public boolean supportsMediaType(String mediatype) {
    // support everything
    return true;
  }

  @Override
  public void inspect(PackageData pack) {
    File original = pack.getOriginal();

    setFileProperties(original, pack);
  }

  private static void setFileProperties(File file, PackageData pack) {
    FileSizeFormat fmt = new FileSizeFormat();
    if (file.isFile()) {
      pack.setProperty(PREFIX + "length [" + file.getName() + "]", fmt.format(file.length()));
      pack.setProperty(PREFIX + "lastModified [" + file.getName() + "]", ISO8601.format(file.lastModified(), ISO8601.DATETIME));
    } else {
      for (File f : file.listFiles()) {
        setFileProperties(f, pack);
      }

    }
  }

}
