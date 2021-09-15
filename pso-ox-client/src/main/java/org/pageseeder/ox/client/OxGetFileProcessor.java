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
package org.pageseeder.ox.client;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * It check the job status and when complete it download the file and save it in the destination.
 *
 * @author Carlos Cabral
 * @since 01 December 2015
 */
public final class OxGetFileProcessor {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OxGetFileProcessor.class);

  /**  It is maximum size allowed for each call of transfer form *. */
  private static final long MAX_16MB = 1<<24;

  /** The job id that is creating the file.**/
  private final String _jobId;

  /** The file downloaded will be saved here. It must be a file **/
  private final File _destination;

  /**
   * Instantiates a new ox get file processor.
   *
   * @param destination  the destination is a {@link File} where the downloaded file will be saved.
   * @param jobId        the job id is a {@link String}, it should be the id of the job that is generating the file.
   */
  public OxGetFileProcessor(File destination, String jobId) {
    if (jobId == null) { throw new NullPointerException("Job Id is null"); }
    if (destination == null) { throw new NullPointerException("Destination is null"); }
    this._jobId = jobId;
    this._destination = destination;
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

      OxStatusCheckProcessor statusProcessor = new OxStatusCheckProcessor(this._jobId, true);
      String status = null;
      String filePath = null;
      statusProcessor.process(xml, oxProperties);
      status = statusProcessor.getJobStatusLoader().getStatus();
      filePath = statusProcessor.getJobStatusLoader().getFilelink();
      if (status.equals(OxStatusCheckProcessor.STATUS_COMPLETED)) {
        //Get URL to download the file
        String url = oxProperties.getDownloadURL(filePath);

        //Load the file from URL and save it in the destination
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        try(FileOutputStream fos = new FileOutputStream(this._destination);) {
          //Transfer from 0 to 16MB, if the file is bigger than that a loop is necessary
          long downloaded = fos.getChannel().transferFrom(rbc, 0, MAX_16MB);

          LOGGER.debug("Downloaded Size: " + downloaded);
          toXML(xml);
        }
      } else {
        errorToXML(xml, status, statusProcessor.getJobStatusLoader().getMessage(), statusProcessor.getJobStatusLoader().getPercentage());
      }
    } finally {
      LOGGER.debug("Process time [ {} ] ms", (System.nanoTime() - begining) / 1000);
    }
  }

  /**
   * Return the result in xml format.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void toXML(XMLWriter xml) throws IOException {
      xml.openElement("ox-get-file");
      xml.attribute("status", "ok");
      xml.attribute("sever-location", this._destination.getAbsolutePath());
      xml.closeElement();
  }

  /**
   * Return the result in xml format.
   *
   * @param xml the xml
   * @param status the status
   * @param message the message
   * @param percentage the percentage
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void errorToXML(XMLWriter xml, String status, String message, String percentage) throws IOException {
      xml.openElement("ox-get-file");
      xml.attribute("status", status);
      xml.attribute("message", message);
      xml.attribute("percentage", percentage);
      xml.closeElement();
  }
}