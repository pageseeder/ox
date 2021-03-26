/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.core;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;

/**
 * A simple java object to represent the job job status.
 *
 * @author Ciber Cai
 * @since  15 October 2013
 */
public class JobStatus implements XMLWritable, Serializable {

  private static final long serialVersionUID = -6772190282366148257L;

  public enum STATUS {
    /** to indicate the current job is stopped (not started yet). */
    STOPPED,

    /** to indicate the current job is processing. */
    PROCESSING,

    /** to indicate the current job has completed. */
    COMPLETED,

    /** to indicate the current job has error. */
    ERROR,

    ;
  }

  /**
   * The completed percentage of the job (from 0 to 100)
   */
  private int percentage;

  /**
   * The job message
   */
  private String message;

  /**
   * The status of job
   */
  private JobStatus.STATUS status;

  /**
   * the constructor of {@link JobStatus}
   */
  protected JobStatus() {
    reset();
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("job-status");
    xml.attribute("status", this.status.name());
    xml.attribute("percentage", String.valueOf(this.percentage));
    xml.writeText(this.message);
    xml.closeElement();

  }

  @Override
  public String toString() {
    return this.status.name();
  }

  /**
   * @return the percentage
   */
  public int getPercentage() {
    return this.percentage;
  }

  /**
   * Return true when job status is completed or job status is error.
   * @return the status whether has complete.
   */
  public boolean hasCompleted() {
    return this.status == STATUS.COMPLETED || this.status == STATUS.ERROR;
  }

  /**
   * @param percentage the percentage to set
   */
  public void setPercentage(int percentage) {
    if (percentage > 100) {
      this.percentage = 100;
    } else if (percentage < 0) {
      this.percentage = -1;// It means unknown
    } else {
      this.percentage = percentage;
    }
  }

  /**
   * @param status the jobStatus to set
   */
  public void setJobStatus(JobStatus.STATUS status) {
    this.status = status;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Reset the job status.
   */
  private void reset() {
    this.percentage = -1;
    this.status = STATUS.PROCESSING;
  }
}
