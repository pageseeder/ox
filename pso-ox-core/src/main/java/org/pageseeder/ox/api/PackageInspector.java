package org.pageseeder.ox.api;

import org.pageseeder.ox.core.InspectorService;
import org.pageseeder.ox.core.PackageData;

/**
 * This is a Service Provider Interface for inspector. which will use with {@link InspectorService}
 *
 * To determine which inspector to return which is based on the file mineType (file extension).
 * The convention of Inspector name is [minetype] + Inspector which is case intensive.
 * Eg:
 *     psml will try to find PSMLInspector.
 *     zip will try to find ZipInspector.
 *
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since  17 June 2015
 */
public interface PackageInspector {

  /**
   * The name of the {@link PackageInspector}
   * @return the name of the Inspector
   */
  String getName();

  /**
   * To indicate what media type it can support for this inspector.
   * @param mediatype the media type
   * @return whether the media type is support for the {@link PackageInspector}
   */
  boolean supportsMediaType(String mediatype);

  /**
   * The actual implementation of inspector
   * @param pack The packageData to inspect
   */
  void inspect(PackageData pack);

}
