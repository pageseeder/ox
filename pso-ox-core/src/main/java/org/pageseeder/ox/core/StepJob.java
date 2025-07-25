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
package org.pageseeder.ox.core;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.JobStatus.STATUS;
import org.pageseeder.ox.util.ISO8601;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * A simple java object to represent a Job for a step.
 *
 * @author Carlos Cabral
 * @since 27 February 2017
 */
public final class StepJob implements XMLWritable, Serializable {

  /**  the serial version. */
  private static final long serialVersionUID = -3028660547170793478L;

  /**
   * Maximum inactive time in milliseconds.
   */
  public static final long DEFAULT_MAX_INACTIVE_TIME_MS = 60 * 60 * 1000; // 60 minutes

  /** The job id. */
  private final String _id;

  /** Time the job was started. */
  private final long _startTime;

  /** The data package;. */
  private final PackageData _package;

  /** The pipeline needs to process. */
  private final StepDefinition _step;

  /** The status of job. */
  private final JobStatus status;

  /** The job result. */
  private Result result;

  /**
   * The milliseconds allowed for this job be inactive, after this time it will
   * be deleted.
   */
  private final long _maxInactiveTime;

  /**
   * Instantiates a new step job.
   *
   * @param step The {@link StepDefinition}
   * @param pack The {@link PackageData}
   */
  public StepJob(StepDefinition step, PackageData pack) {
    this (step, pack, DEFAULT_MAX_INACTIVE_TIME_MS);
  }

  /**
   * Instantiates a new step job.
   *
   * @param step            The {@link StepDefinition}
   * @param pack            The {@link PackageData}
   * @param maxInactiveTime the max inactive time
   */
  public StepJob(StepDefinition step, PackageData pack, long maxInactiveTime) {
    if (step == null) { throw new NullPointerException("step is null."); }
    if (pack == null) { throw new NullPointerException("pack is null."); }

    this._id = "c" + System.nanoTime() + "f" + new Random(System.currentTimeMillis()).nextInt(100);
    this._startTime = System.currentTimeMillis();
    this._step = step;
    this._package = pack;
    this._maxInactiveTime = maxInactiveTime;
    this.status = new JobStatus();
  }

  /**
   * Gets the id.
   *
   * @return the job id.
   */
  public String getId() {
    return this._id;
  }

  /**
   * Gets the step.
   *
   * @return the step
   */
  public StepDefinition getStep() {
    return _step;
  }

  /**
   * Gets the package data.
   *
   * @return the package data
   */
  public PackageData getPackageData() {
    return this._package;
  }

  /**
   * Gets the max inactive time.
   *
   * @return the max inactive time
   */
  public long getMaxInactiveTime() {
    return this._maxInactiveTime;
  }

  /**
   * Gets the status.
   *
   * @return the status of the job
   */
  public JobStatus getStatus() {
    if (!this.status.hasCompleted()) {
      this.status.setPercentage(this._step.percentage());
    }
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
    return System.currentTimeMillis() - this._startTime > this.getMaxInactiveTime() ? true : false;
  }

  /**
   * Set the job to complete status.
   */
  public void started() {
    this.status.setJobStatus(STATUS.PROCESSING);
    this.status.setPercentage(this._step.percentage());
  }

  /**
   * Set the job to complete status.
   *
   * @param result the result
   */
  public void completed(Result result) {
    this.status.setJobStatus(STATUS.COMPLETED);
    this.status.setPercentage(this._step.percentage());
    this.result = result;
  }

  /**
   * Set the job to error status.
   *
   * @param result the details of result.
   */
  public void failed(Result result) {
    this.status.setJobStatus(STATUS.ERROR);
    this.status.setPercentage(this._step.percentage());
    this.result = result;
  }

  /**
   * Set the job to error status.
   */
  public void failed() {
    this.status.setJobStatus(STATUS.ERROR);
    this.status.setPercentage(this._step.percentage());
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("job");
    xml.attribute("id", this._id);
    xml.attribute("start", ISO8601.DATETIME.format(this._startTime));
    JobStatus tempStatus = this.getStatus();
    xml.attribute("status", tempStatus.toString());
    xml.attribute("percentage", tempStatus.getPercentage());
    xml.attribute("message", tempStatus.getMessage());
    xml.attribute("pipeline", this._step.pipeline().id());
    xml.attribute("step", this._step.name());
    this._step.toXML(this.result, xml);
    xml.closeElement();
    tempStatus = null;
  }

}
