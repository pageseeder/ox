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
import org.pageseeder.ox.util.CharsetDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * A inspector for html.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  13 November 2013
 *
 */
public class HTMLInspector implements PackageInspector {

  /** the logger */
  private final static Logger LOGGER = LoggerFactory.getLogger(HTMLInspector.class);

  @Override
  public String getName() {
    return "ox-html-inspector";
  }

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.PackageInspector#supportsMediaType(java.lang.String)
   */
  @Override
  public boolean supportsMediaType(String mediatype) {
    return "text/html".equals(mediatype.trim());
  }

  @Override
  public void inspect(PackageData pack) {
    File html = pack.getOriginal();
    if (html != null && html.exists() && html.length() > 0) {
      try {

        // Detect the charset used from actual codes.
        Charset charset = CharsetDetector.getFromBOM(html);
        if (charset == null) charset = CharsetDetector.getFromContent(html);
        pack.setProperty("charset", charset.name());
        String code = new String(Files.readAllBytes(html.toPath()), "UTF-8");

        // Count the common HTML elements
        int paragraphs = count(code, "p");
        int headings = count(code, "h1", "h2", "h3", "h4", "h5", "h6");
        int tables = count(code, "table");
        int images = count(code, "img");
        int lists = count(code, "ul", "ol", "dl");

        pack.setProperty("html.paragraphs", Integer.toString(paragraphs));
        pack.setProperty("html.headings", Integer.toString(headings));
        pack.setProperty("html.tables", Integer.toString(tables));
        pack.setProperty("html.images", Integer.toString(images));
        pack.setProperty("html.lists", Integer.toString(lists));

      } catch (IOException ex) {
        LOGGER.info("Cannot inspect HTML {}", ex);
      }

    }
  }

  /**
   * Count the occurrences of the specified tags in the HTML code.
   *
   * @param code The HTML code
   * @param tag  THe tag name (for example "p")
   * @return the number of occurrences
   */
  private static int count(String code, String... tags) {
    int count = 0;
    for (String tag : tags) {
      count += count(code, tag);
    }
    return count;
  }

  /**
   * Count the occurrences of the specified tags in the HTML code.
   *
   * @param code The HTML code
   * @param tag  THe tag name (for example "p")
   * @return the number of occurrences
   */
  private static int count(String code, String tag) {
    int count = 0;
    int from = -1;
    final String startTag = '<' + tag;
    while ((from = code.indexOf(startTag, from + 1)) != -1) {
      if (code.charAt(from + tag.length() + 1) == ' ' || code.charAt(from + tag.length() + 1) == '>') count++;
    }
    return count;
  }

}
