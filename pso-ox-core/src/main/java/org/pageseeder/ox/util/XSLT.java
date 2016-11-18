/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Lauret
 * @since  28 October 2013
 */
public class XSLT {

  /** Logger for this class */
  private static final Logger LOGGER = LoggerFactory.getLogger(XSLT.class);

  /**
   * Maps XSLT templates to their URL as a string for easy retrieval.
   */
  private static final BasicCache<Templates> CACHE = new BasicCache<Templates>();

  /** Utility class. */
  private XSLT() {}

  /**
   * Return the XSLT templates from the given style.
   *
   * @param url A URL to a template.
   *
   * @return the corresponding XSLT templates object or <code>null</code> if the URL was <code>null</code>.
   *
   * @throws TransformerConfigurationException If XSLT templates could not be loaded from the specified URL.
   * @throws IOException If an IO error occur while reading the template
   */
  public static Templates getTemplates(URL url) throws TransformerConfigurationException, IOException {
    if (url == null) return null;
    // load the templates from the source file
    Templates templates = null;
    try (InputStream in = url.openStream()) {
      LOGGER.debug("Loading templates from URL: {}", url);
      Source source = new StreamSource(in);
      source.setSystemId(url.toString());
      TransformerFactory factory = TransformerFactory.newInstance();
      templates = factory.newTemplates(source);
    }
    return templates;
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * @param file A file to a template.
   *
   * @return the corresponding XSLT templates object or <code>null</code> if the URL was <code>null</code>.
   *
   * @throws TransformerConfigurationException If XSLT templates could not be loaded from the specified URL.
   * @throws IOException If an IO error occur while reading the template
   */
  public static Templates getTemplates(File file) throws TransformerConfigurationException, IOException {
    URI uri = file.toURI();
    long lastModified = file.lastModified();
    Templates templates = CACHE.get(uri.toString(), lastModified);
    if (templates == null) {
      templates = getTemplates(uri.toURL());
      CACHE.put(uri.toString(), templates);
    }
    return templates;
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * <p>This method will firt try to load the resource using the class loader used for this class.
   *
   * <p>Use this class to load XSLT from the system.
   *
   * @param resource The path to a resource.
   *
   * @return the corresponding XSLT templates object;
   *         or <code>null</code> if the resource could not be found.
   *
   * @throws TransformerConfigurationException If XSLT templates could not be loaded from the specified resource.
   * @throws IOException If an IO error occur while reading the template
   */
  public static Templates getTemplatesFromResource(String resource) throws TransformerConfigurationException, IOException {
    ClassLoader loader = XSLT.class.getClassLoader();
    URL url = loader.getResource(resource);
    Templates templates = CACHE.get(url.toString(), 0);
    if (templates == null) {
      templates = getTemplates(url);
      CACHE.put(url.toString(), templates);
    }
    return templates;
  }

  /**
   * Clears the internal XSLT cache.
   */
  public void clearCache() {
    LOGGER.debug("Clearing XSLT cache.");
    CACHE.clear();
  }

}
