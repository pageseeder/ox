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
package org.pageseeder.ox.util;

import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Map;

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
   * Transform the XML to result.
   *
   * @param input defines the input source
   * @param output defines the output.
   * @param transformer the transformer.
   * @throws IOException the IO error occur.
   * @throws TransformerException the transformation error occur.
   */
  public static void transform(org.pageseeder.ox.api.Result input, File output, Transformer transformer) throws IOException, TransformerException {
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    input.toXML(writer);
    transform (new StreamSource(new StringReader(writer.toString())), new StreamResult(output), transformer);
  }

  /**
   * Transform the XML to result.
   *
   * @param input defines the input source
   * @param output defines the output.
   * @param transformer the transformer.
   * @throws IOException the IO error occur.
   * @throws TransformerException the transformation error occur.
   */
  public static void transform(File input, File output, Transformer transformer) throws IOException, TransformerException {
    transform (new StreamSource(input), new StreamResult(output), transformer);
  }

  /**
   * Transform the XML to result.
   *
   * @param source defines the input source
   * @param output defines the output.
   * @param transformer the transformer.
   * @throws IOException the IO error occur.
   * @throws TransformerException the transformation error occur.
   */
  public static void transform(Source source, Result output, Transformer transformer) throws IOException, TransformerException {
    transformer.transform(source, output);
  }

  /**
   * Builds the XSLT transformer.
   *
   * @param xsl the xsl
   * @param data the data
   * @param info the info
   * @return the transformer
   * @throws TransformerConfigurationException the transformer configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Transformer buildXSLTTransformer (File xsl, PackageData data, StepInfo info) throws TransformerConfigurationException, IOException {
    URIResolver resolver = new CustomURIResolver(xsl.getParentFile());
    Templates templates = XSLT.getTemplates(xsl);
    Transformer transformer = templates.newTransformer();
    transformer.setURIResolver(resolver);

    // Add the parameters from post request
    for (Map.Entry<String, String> p : data.getParameters().entrySet()) {
      transformer.setParameter(p.getKey(), p.getValue());
    }

    String originalFileName = data.getProperty(PackageData.ORIGINAL_PROPERTY);
    if (originalFileName != null) {
      transformer.setParameter("original_file", originalFileName);
    }
    transformer.setParameter("data-id", data.id());
    transformer.setParameter("data-repository", data.directory().getAbsolutePath().replace('\\', '/'));

    // Add the parameters from step definition in model.xml
    // these parameters should use the prefix _xslt-
    for (Map.Entry<String, String> p :info.parameters().entrySet()) {
      if (p.getKey().startsWith("_xslt-")) {
        String newValue = StepUtils.applyDynamicParameterLogic(data, info, p.getValue());
        transformer.setParameter(p.getKey().replaceAll("_xslt-", ""), newValue);
      }
    }

    String indent = !StringUtils.isBlank(info.parameters().get("_xslt-indent")) ? info.parameters().get("_xslt-indent") : data.getParameter("_xslt-indent");
    if (!StringUtils.isBlank(indent)) {
      transformer.setOutputProperty(OutputKeys.INDENT, indent.equalsIgnoreCase("yes")?"yes":"no");
    }

    return transformer;
  }

  /**
   * A custom URI resolver to get the stylesheet file.
   * @author Ciber Cai
   * @since  17 June 2014
   */
  private static class CustomURIResolver implements URIResolver {

    /**  the root of stylesheet *. */
    private final File _root;

    /**
     * Instantiates a new custom URI resolver.
     *
     * @param r the folder of the stylesheet
     */
    public CustomURIResolver(File r) {
      this._root = r;
    }

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
      File xsl = new File(this._root, href);
      Source source = null;
      try {
        if (xsl != null && xsl.exists()) {
          source = new StreamSource(new FileInputStream(xsl));
        }
      } catch (FileNotFoundException e) {
        LOGGER.warn("Cannot find the stylesheet file {}", href);
      }
      return source;
    }
  }

  /**
   * Clears the internal XSLT cache.
   */
  public void clearCache() {
    LOGGER.debug("Clearing XSLT cache.");
    CACHE.clear();
  }

}
