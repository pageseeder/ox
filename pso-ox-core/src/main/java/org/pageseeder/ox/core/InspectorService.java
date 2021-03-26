/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.core;

import org.pageseeder.ox.api.PackageInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The SPI for {@link PackageInspector}.
 *
 * To Register {@link PackageInspector} service, you must specify the service in
 * :CLASPATH/META-INF/services/org.pageseeder.ox.api.PackageInspector
 *
 *
 * @author Ciber Cai
 * @since 15 June 2016
 */
public final class InspectorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(InspectorService.class);

  /** The inspector service */
  private static InspectorService SERVICE;

  /** The service loader */
  private final ServiceLoader<PackageInspector> loader;

  private InspectorService() {
    this.loader = ServiceLoader.load(PackageInspector.class);
  }

  /**
   * @return the {@link InspectorService} instance.
   */
  public static InspectorService getInstance() {
    synchronized (InspectorService.class) {
      if (SERVICE == null) {
        SERVICE = new InspectorService();
      }
    }
    return SERVICE;
  }

  /**
   * reload the Service Loader.
   */
  public void reload() {
    this.loader.reload();
  }

  /**
   * Return the list of {@link PackageInspector}s base on the media type.
   * @param mediaType the request media type
   * @return the List of {@link PackageInspector}s
   */
  public List<PackageInspector> getInspectors(String mediaType) {
    if (mediaType == null) {
      LOGGER.warn("No media type is supplied.");
      return null;
    }

    LOGGER.debug("The request mime type {}", mediaType);
    List<PackageInspector> inspectors = new ArrayList<>();

    try {
      Iterator<PackageInspector> iter = this.loader.iterator();
      while (iter.hasNext()) {
        PackageInspector inspector = iter.next();
        LOGGER.debug("Inspector name {} for {}", inspector.getName(), inspector.getClass().getName());
        if (inspector.supportsMediaType(mediaType) && !inspectors.contains(inspector)) {
          inspectors.add(inspector);
        }
      }
    } catch (ServiceConfigurationError ex) {
      LOGGER.error("Service configuration error when try to get inspector.", ex);
    }

    Collections.unmodifiableCollection(inspectors);
    return inspectors;
  }
}
