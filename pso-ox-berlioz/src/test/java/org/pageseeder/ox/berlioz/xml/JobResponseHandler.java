/*
 * Copyright (c) 2017 Allette systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.xml;

import org.pageseeder.ox.berlioz.model.JobResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Carlos Cabral
 * @since 13 April 2018
 */
public class JobResponseHandler extends DefaultHandler {
  
  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(JobResponseHandler.class);

  /**
   * Job.
   */
  private JobResponse job;


  public JobResponseHandler() {
    super();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String element = localName.length() == 0? qName : localName;
    if ("job".equals(element)) {
      this.job = new JobResponse(attributes.getValue("id"),
                                 attributes.getValue("start"), 
                                 attributes.getValue("status"), 
                                 attributes.getValue("input"), 
                                 attributes.getValue("mode"));
    }
  }

  public JobResponse getJob() {
    return job;
  }
}
