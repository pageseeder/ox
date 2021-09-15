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
package org.pageseeder.ox.inspector;

import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.FileSizeFormat;
import org.pageseeder.ox.util.ISO8601;

import java.io.File;

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
