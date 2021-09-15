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
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A thread to call the {@link OxGetFileProcessor}
 * @author Carlos Cabral
 * @since 26 August 2016
 *
 */
public class OxGetFileProcessorThread implements Runnable {
  private final static Logger LOGGER = LoggerFactory.getLogger(OxGetFileProcessorThread.class);

  private final File _result;
  private final String _jobId;

  public OxGetFileProcessorThread(File result, String jobId) {
    this._result = result;
    this._jobId = jobId;
  }

  @Override
  public void run() {
    OxGetFileProcessor getFileProcessor = new OxGetFileProcessor(this._result, this._jobId);
    try {
      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      getFileProcessor.process(xml);
      LOGGER.debug(xml.toString());
    } catch (IOException ex) {
      LOGGER.error("OxGetFileProcessor-IO Exception: " + ex.getMessage());
    } catch (BerliozException ex) {
      LOGGER.error("OxGetFileProcessor-Berlioz Exception: " + ex.getMessage());
    }
  }
}
