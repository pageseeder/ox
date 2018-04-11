/*
 *  Copyright (c) 2015 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.client;

import java.io.IOException;
import java.io.StringReader;

import org.pageseeder.bastille.security.Obfuscator;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.xml.XMLUtils;
import org.pageseeder.ox.client.handler.JobStatusLoader;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check the status of a job in the ox server.
 *
 * @author Carlos Cabral
 * @since 26 August 2016
 */
public final class OxStatusCheckProcessor {

  /** The Constant STATUS_COMPLETED. */
  public static final String STATUS_COMPLETED = "COMPLETED";
  /** The Constant STATUS_ERROR. */
  public static final String STATUS_ERROR = "ERROR";

  /** The _job id. */
  private final String _jobId;

  /** The job status loader. */
  private JobStatusLoader jobStatusLoader;

  /** The write file link. */
  private boolean _writeFileLink;

  /**
   * Instantiates a new ox status check processor.
   *
   * @param jobId the job id
   */
  public OxStatusCheckProcessor(String jobId) {
    this(jobId, false);
  }


  /**
   * Instantiates a new ox status check processor.
   *
   * @param jobId the job id
   * @param writeFileLink the write file link
   */
  public OxStatusCheckProcessor(String jobId, boolean writeFileLink) {
    if (jobId == null) { throw new NullPointerException("Job Id is null"); }
    this._jobId = jobId;
    this._writeFileLink = writeFileLink;
  }



  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OxStatusCheckProcessor.class);


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
   * @param xml the xml
   * @param oxProperties the ox properties
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws BerliozException the berlioz exception
   */
  public void process(XMLWriter xml, OxProperties oxProperties) throws IOException, BerliozException {
    LOGGER.debug("Processing...");
    long begining = System.nanoTime();
    try {
      //Is ox configured
      if (!oxProperties.isConfigured()) {
        LOGGER.error("The OX Config is empty.");
        throw new BerliozException("The OX Config is empty.");
      }

      String url = oxProperties.getStatusURL(this._jobId);
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

      // read response
      String response = post.getResponse();
      LOGGER.debug("Got response {}", response);

      // parse and find the jobid
      setJobStatusLoader(new JobStatusLoader());
      XMLUtils.parse(getJobStatusLoader(), new StringReader(response), false);

      // redirect
      if (getJobStatusLoader().getStatus() == null) {
        LOGGER.error("Failed to load job status from response");
        throw new BerliozException("Failed to load job status from response.");
      }
      LOGGER.debug("Found status {}", getJobStatusLoader().getStatus());
      toXML(xml);
    } finally {
      LOGGER.debug("Process time [ {} ] ms", (System.nanoTime() - begining) / 1000);
    }
  }


  /**
   * write the result in xml format.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void toXML(XMLWriter xml) throws IOException {
    xml.openElement("ox-status");
    xml.attribute("status", getJobStatusLoader().getStatus());
    xml.attribute("percentage", getJobStatusLoader().getPercentage());
    if(getJobStatusLoader().getStatus().equals(STATUS_COMPLETED)) {
      xml.attribute("file-link", this._writeFileLink? getJobStatusLoader().getFilelink(): "***");
    } else if (getJobStatusLoader().getFilelink().equals(STATUS_ERROR)){
      xml.attribute("message", getJobStatusLoader().getMessage());
    }
    xml.closeElement();
  }



  /**
   * Gets the job status loader.
   *
   * @return the job status loader
   */
  public JobStatusLoader getJobStatusLoader() {
    return jobStatusLoader;
  }

  /**
   * Sets the job status loader.
   *
   * @param jobStatusLoader the new job status loader
   */
  private void setJobStatusLoader(JobStatusLoader jobStatusLoader) {
    this.jobStatusLoader = jobStatusLoader;
  }
}