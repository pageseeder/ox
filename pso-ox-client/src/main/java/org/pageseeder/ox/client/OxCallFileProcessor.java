/*
 *  Copyright (c) 2018 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.client;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;

import org.pageseeder.bastille.security.Obfuscator;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.xml.XMLUtils;
import org.pageseeder.ox.client.handler.JobIDLoader;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Call the OX server to convert or to transform the source and get the job id of this transformation.
 *
 *
 * @author Carlos Cabral
 * @since  26 August 2016
 */
public final class OxCallFileProcessor {
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OxCallFileProcessor.class);

  /** It is the zip file with the data to be converted or transformed. **/
  private final File _source;
  /** The pipeline to be used to convert/transform the source. **/
  private final String _pipeline;

  /** stores the job id of the file conversion or transformation. **/
  private JobIDLoader jobidLoader;

  /**
   * Instantiates a new ox call file processor.
   *
   * @param source the source (normally a zip file)
   * @param format the format
   */
  public OxCallFileProcessor(File source, String pipeline) {
    if (source == null) { throw new NullPointerException("Source is null"); }
    if (pipeline == null) { throw new NullPointerException("pipeline is null"); }
    this._source = source;
    this._pipeline = pipeline;
  }

  /**
   * Process.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws BerliozException the berlioz exception
   */
  public void process(XMLWriter xml) throws IOException, BerliozException {

    // load document server properties
      OxProperties oxProperties = new OxProperties();
      process(xml, oxProperties);

  }

  /**
   * Dry process.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws BerliozException the berlioz exception
   */
  private void process(XMLWriter xml, OxProperties oxProperties) throws IOException, BerliozException {
    LOGGER.debug("Processing...");
    long begining = System.nanoTime();
    try {
      //Is ox configured
      if (!oxProperties.isConfigured()) {
        LOGGER.error("The OX Config is empty.");
        throw new BerliozException("The OX Config is empty.");
      }

      //The OX pipeline, it will be format.
      String pipeline = this._pipeline;

      String url = oxProperties.getProcessURL();
      String username  = oxProperties.getUserName();
      String password  = oxProperties.getPassword();

      // de-obfuscate
      if (password != null && password.startsWith("OB1:")) {
        password = Obfuscator.clear(password.substring(4));
      }

      // ok send to doc server
      LOGGER.debug("Connecting to {}", url);
      HTTPPost post = new HTTPPost(url, username, password);
      post.connect();
      LOGGER.debug("Connected");

      // add pipeline
      post.addPart(pipeline.getBytes("UTF-8"), "text/plain", "pipeline", null);

      // add zip
      String filename = this._source.getName();
      post.addPart(Files.readAllBytes(this._source.toPath()), "application/zip", filename, filename);
      LOGGER.debug("ZIP sent");

      // read response
      String response = post.getResponse();
      LOGGER.debug("Got response {}", response);
      // parse and find the jobid
      setJobidLoader(new JobIDLoader());
      XMLUtils.parse(getJobidLoader(), new StringReader(response), false);

      // redirect
      if (jobidLoader.getJobid() == null) {
        LOGGER.error("Failed to load job ID from response");
        throw new BerliozException("Failed to load job ID from response.");
      }
      LOGGER.debug("Found jobid {}", jobidLoader.getJobid());
      xml.openElement("ox-file-processor");
      xml.attribute("job-id", getJobidLoader().getJobid());
      xml.closeElement();
    } finally {
      LOGGER.debug("Process time [ {} ] ms", (System.nanoTime() - begining) / 1000);
    }
  }

  /**
   * Gets the jobid loader.
   *
   * @return the jobid loader
   */
  public JobIDLoader getJobidLoader() {
    return jobidLoader;
  }

  /**
   * Sets the jobid loader.
   *
   * @param jobidLoader the new jobid loader
   */
  private void setJobidLoader(JobIDLoader jobidLoader) {
    this.jobidLoader = jobidLoader;
  }

//
//
//  /**
//   * Load the images and xrefs from the PSML source and the service from the berlioz header.
//   */
//  private static class ReferenceLoader extends DefaultHandler {
//
//    /** Images to load (uriid, path). */
//    private final Map<String, String> images;
//
//    /** The folder path for the PSML document relative to the berlioz psml folder. */
//    private String base;
//
//    /**
//     * Alternate constructor.
//     *
//     * @param images        the images to load (uriid, path)
//     * @param base          folder path for the PSML document relative to the berlioz psml folder (can be null if loading from content)
//     */
//    public ReferenceLoader(Map<String, String> images, String base) {
//      this.images = images;
//      this.base = base;
//    };
//
//    /* (non-Javadoc)
//     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
//     */
//    @Override
//    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
//      startElementImage(qName, atts);
//    }
//
//    private void startElementImage(String qName, Attributes atts) {
//      if ("image".equals(qName) && LOAD_IMAGES) {
//        String path = atts.getValue("src");
//        String imageUriid = getImageURIID(path);
//        if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(imageUriid)) {
//          if (path.indexOf('?') != -1) path = path.replaceFirst("\\?(.*?)$", "");
//          if (!this.images.containsKey(imageUriid)) {
//            try {
//              path = URLDecoder.decode(path, "UTF-8");
//            } catch (UnsupportedEncodingException ex) {
//              // should not happen
//              LOGGER.error("UTF-8 is unknown!", ex);
//            }
//            // if path is not relative use original path
//            this.images.put(imageUriid, path.startsWith("/") || this.base == null ? path : (this.base + path));
//          }
//        }
//      }
//    }
//
//    /* (non-Javadoc)
//     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
//     */
//    @Override
//    public void endElement(String uri, String localName, String qName) {
//
//    }
//
//    /* (non-Javadoc)
//     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
//     */
//    @Override
//    public void characters(char[] ch, int start, int length) throws SAXException {
//
//    }
//
//  }
}