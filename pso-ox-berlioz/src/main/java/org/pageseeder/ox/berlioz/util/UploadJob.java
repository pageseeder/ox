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
package org.pageseeder.ox.berlioz.util;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 * A simple java object for represent the UploadJob .
 *
 * @author Ciber Cai
 * @version 10 February 2014
 */
public class UploadJob implements XMLWritable {

  public static enum STATUS {
    PROCESSING, COMPLETED, ERROR;
  }

  /** Maximum inactive time in milliseconds */
  private static final long MAX_INACTIVE_TIME_MS = 10 * 60 * 1000; // 10 minutes

  /** the job id **/
  private final String jobId;

  /** the ProgressListener **/
  private final FileUploadListener listener;

  /** the status **/
  private STATUS status;

  /** the number of percentage has been done **/
  private long percentage;

  /** the start time**/
  private final long startTime;

  public UploadJob(String jobid, FileUploadListener listener) {
    this.jobId = jobid;
    this.listener = listener;
    this.status = STATUS.PROCESSING;
    this.startTime = System.currentTimeMillis();
    this.percentage = 1;
  }

  /**
   * Indicates whether this job is considered inactive.
   *
   * <p>An import job is considered inactivate if more than max inactive time occurred
   * since the start time - that is if the import job was started over an hour ago.
   *
   * @return <code>true</code> if more than 1 hour occurred since the start time;
   *         <code>false</code> up to 1 hour after the start time.
   */
  public boolean isInactive() {
    updateStatus();
    return System.currentTimeMillis() - this.startTime > MAX_INACTIVE_TIME_MS ? true : false;
  }

  /**
   * @return the jobId
   */
  public String getJobId() {
    return this.jobId;
  }

  /**
   * @return the status
   */
  public STATUS getStatus() {
    return this.status;
  }

  public void setStatus(STATUS status) {
    this.status = status;
  }

  private void updateStatus() {
    this.percentage = this.listener.percentage();
    if (this.percentage >= 100) {
      this.status = STATUS.COMPLETED;
    } else if (this.percentage < 0) {
      this.status = STATUS.ERROR;
    } else {
      this.status = STATUS.PROCESSING;
    }

  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    updateStatus();
    xml.openElement("job");
    xml.attribute("id", this.jobId);
    xml.attribute("precentage", String.valueOf(this.percentage));
    xml.attribute("status", this.status.toString());
    xml.closeElement();
  }

}
