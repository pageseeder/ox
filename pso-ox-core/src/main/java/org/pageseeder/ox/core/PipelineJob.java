/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.JobStatus.STATUS;
import org.pageseeder.ox.util.ISO8601;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A simple java object to represent a Job for each Pipeline
 *
 * @author Ciber Cai
 * @since  07 November 2014
 */
public final class PipelineJob implements XMLWritable, Serializable {

  /** the serial version */
  private static final long serialVersionUID = -3028660547170793478L;

  /**
   * Maximum inactive time in milliseconds
   */
  private static final long MAX_INACTIVE_TIME_MS = 60 * 60 * 1000; // 60 minutes

  /**
   * The job id
   */
  private final String _id;

  /**
   * Time the job was started
   */
  private final long _startTime;

  /**
   * The data package;
   */
  private final PackageData _package;

  /**
   * The pipeline needs to process
   */
  private final Pipeline _pipeline;

  /**
   * The status of job
   */
  private final JobStatus status;

  /**
   * The file to download
   */
  private String download;

  /**
   * The job result
   */
  private List<Result> results;

  /**
   * to indicate the job is in slow land
   */
  private boolean isSlow = false;

  /**
   * @param pipeline The {@link Pipeline }
   * @param pack The {@link PackageData }
   */
  public PipelineJob(Pipeline pipeline, PackageData pack) {
    if (pipeline == null) { throw new NullPointerException("pipeline is null."); }
    if (pack == null) { throw new NullPointerException("pack is null."); }

    this._id = "c" + System.nanoTime() + "f" + new Random(System.currentTimeMillis()).nextInt(100);
    this._startTime = System.currentTimeMillis();
    this._pipeline = pipeline;
    this._package = pack;
    this.status = new JobStatus();
    this.results = new ArrayList<>();
  }

  /**
   * @return the job id.
   */
  public String getId() {
    return this._id;
  }

  /**
   * @return the pipeline
   */
  public Pipeline getPipeline() {
    return this._pipeline;
  }

  /**
   * @return the package data
   */
  public PackageData getPackageData() {
    return this._package;
  }

  /**
   * @return the download path
   */
  public String getDownload() {
    return this.download;
  }

  /**
   * @return the status of the job
   */
  public JobStatus getStatus() {
    return this.status;
  }

  /**
   * Indicates whether this job is considered inactive.
   *
   * <p>An job is considered inactive if the job have have created longer than a specified time
   * since it is created.
   *
   * @return status of the job.
   */
  public boolean isInactive() {
    return System.currentTimeMillis() - this._startTime > MAX_INACTIVE_TIME_MS ? true : false;
  }

  /**
   * @return whether the job is slow job.
   */
  public boolean isSlowJob() {
    return this.isSlow;
  }

  /**
   * Set the job to complete status.
   */
  public void started() {
    this.status.setJobStatus(STATUS.PROCESSING);
    this.status.setPercentage(1);
  }
  
  /**
   * Set the job to complete status.
   */
  public void completed() {
    this.status.setJobStatus(STATUS.COMPLETED);
    this.status.setPercentage(100);
  }

  /**
   * Set the job to error status.
   */
  public void failed() {
    this.status.setJobStatus(STATUS.ERROR);
  }

  /**
   * @param download set the download path
   */
  public void setDownload(String download) {
    this.download = download;
  }

  /**
   * @param slow to indicate that the job is to run on slow mode.
   */
  public void setSlowMode(boolean slow) {
    this.isSlow = slow;
  }

  public void addStepResult (Result result) {
    if (result != null) this.results.add(result);
  }
  
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("job");
    xml.attribute("id", this._id);
    xml.attribute("start", ISO8601.DATETIME.format(this._startTime));
    xml.attribute("status", this.status.toString());
    xml.attribute("input", this._package.getOriginal().getName());
    xml.attribute("mode", this.isSlow ? "slow" : "normal");
    if (this.download != null) {
      xml.attribute("path", this.download);
    }
    
    xml.openElement("results");
      for (Result result:results) {
        result.toXML(xml);
      }
    xml.closeElement();//results
    
    xml.closeElement();
  }

}
