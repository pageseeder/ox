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
package org.pageseeder.ox.berlioz.xml;

import org.pageseeder.ox.berlioz.model.JobResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
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
//  private static Logger LOGGER = LoggerFactory.getLogger(JobResponseHandler.class);

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
