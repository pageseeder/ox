/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.client.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Load the  job ID form the document server response.
 *
 * @author Carlos Cabral
 * @since 26 Aug 2016
 */
public class JobStatusLoader extends DefaultHandler {

  /** The status. */
  private String status = null;

  /** The percentage. */
  private String percentage = null;

  /** The filelink. */
  private String filelink = null;
  /** The error message. */
  private String message = null;
  /** Indicate if the current element is message. */
  private boolean reachedmessage = Boolean.FALSE;
  /* (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if ("job-status".equals(localName)) {
      this.status = attributes.getValue("status");
      this.percentage = attributes.getValue("percentage");
    } else if ("job".equals(localName)) {
      this.filelink = attributes.getValue("path");
    } else if ("message".equalsIgnoreCase(localName)) {
      reachedmessage = Boolean.TRUE;
    }
  }

  /**
   * Receive notification of character data inside an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method to take specific actions for each chunk of character data
   * (such as adding the data to a node or buffer, or printing it to
   * a file).</p>
   *
   * @param ch The characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#characters
   */
  public void characters (char ch[], int start, int length) throws SAXException {
    if(reachedmessage) {
      message = new String(ch, start, length);
      reachedmessage = Boolean.FALSE;
    }
  }


  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Gets the percentage.
   *
   * @return the percentage
   */
  public String getPercentage() {
    return percentage;
  }

  /**
   * Gets the filelink.
   *
   * @return the filelink
   */
  public String getFilelink() {
    return filelink;
  }

  public String getMessage() {
    return message;
  }
}
