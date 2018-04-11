/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.client.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Carlos Cabral
 * @since 26 Aug 2016
 */
public class JobIDLoader  extends DefaultHandler {

  /** The jobid. */
  private String jobid = null;

  /* (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (this.jobid == null && "job".equals(localName))
      this.jobid = attributes.getValue("id");
  }

  /**
   * Gets the jobid.
   *
   * @return the jobid
   */
  public String getJobid() {
    return jobid;
  }
}
