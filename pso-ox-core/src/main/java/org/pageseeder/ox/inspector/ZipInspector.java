

package org.pageseeder.ox.inspector;

import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.CharsetDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A package inspector to check the zip data.
 * It set the charset and length to the property in PackageData.
 *
 * @author Ciber Cai
 * @since 05 November 2014
 */
public class ZipInspector implements PackageInspector {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipInspector.class);

  @Override
  public String getName() {
    return "ox-zip-inspector";
  }

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.PackageInspector#supportsMediaType(java.lang.String)
   */
  @Override
  public boolean supportsMediaType(String mediatype) {
    return "application/zip".equals(mediatype.trim());
  }

  @Override
  public void inspect(PackageData pack) {
    File zipFile = pack.getOriginal();

    if (zipFile != null && zipFile.exists() && zipFile.length() > 0) {
      try {
        // Detect the charset used from actual codes.
        Charset charset = CharsetDetector.getFromBOM(zipFile);
        if (charset == null) {
          charset = CharsetDetector.getFromContent(zipFile);
        }
        pack.setProperty("charset", charset.name());
        pack.setProperty("length", String.valueOf(zipFile.length()));

        // TODO CRC check to ensure the input file in valid and not corrupted.

      } catch (IOException ex) {
        LOGGER.warn("Cannot inspect the zip  ", ex);
      }
    }

  }

}
