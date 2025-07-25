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
package org.pageseeder.ox.process;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A job queue to store the jobs.
 *
 * @author Ciber Cai
 * @since 4 April 2016
 */
public class PipelineJobQueue implements XMLWritable {

  /**  the total number of completed jobs to store in memory. */

  private final int _maxStoredCompletedJob;

  /**  Singleton instance (Lazy init). */
  private static volatile PipelineJobQueue INSTANCE;

  /** Shows that this queue is locked. This means that cannot be used. */
  private static volatile AtomicBoolean LOCKED = new AtomicBoolean(false);

  /**  The list of imported jobs. */
  private BlockingQueue<PipelineJob> waiting;

  /**  the list of slow jobs. */
  private BlockingQueue<PipelineJob> slow;

  /**  The list of completed jobs. */
  private BlockingQueue<PipelineJob> completed;

  /**  The current running import job *. */
  private BlockingQueue<PipelineJob> running;

  /** The key will be the package and the value the job id. */
  private ConcurrentHashMap<String, String> packageAndJobMap;

  /**
   * the private constructor.
   */
  private PipelineJobQueue(int maxStoredCompletedJob) {
    this.waiting = new LinkedBlockingQueue<PipelineJob>();
    this.slow = new LinkedBlockingQueue<PipelineJob>();
    this.completed = new LinkedBlockingQueue<PipelineJob>();
    this.running = new LinkedBlockingQueue<PipelineJob>();
    this._maxStoredCompletedJob = maxStoredCompletedJob;
    this.packageAndJobMap = new ConcurrentHashMap<>();

  }

  /**
   * Gets the single instance of PipelineJobQueue.
   *
   * @param maxStoredCompletedJob the max stored completed job
   * @return the instance of ImportProcessor
   */
  public static PipelineJobQueue getInstance(int maxStoredCompletedJob) {
    if (LOCKED.get()) new OXException("Pipeline job queue is locked to use.");
    if (INSTANCE == null) {
      INSTANCE = new PipelineJobQueue(maxStoredCompletedJob);
    }
    return INSTANCE;
  }

  /**
   * Adds the.
   *
   * @param job the job
   */
  protected void add(PipelineJob job) {
    if (job.isSlowJob()) {
      this.slow.add(job);
    } else {
      this.waiting.add(job);
    }

    this.packageAndJobMap.put(job.getPackageData().id(), job.getId());
    //clear completed job when new job comes in.
    clearCompletedJob();
  }

  /**
   * Next.
   *
   * @param slowMode the slow mode
   * @return the next processing job
   * @throws InterruptedException the interrupted exception
   */
  protected PipelineJob next(boolean slowMode) throws InterruptedException {
    PipelineJob job = slowMode ? this.slow.take() : this.waiting.take();
    if (job != null) {
      this.running.add(job);
    }
    return job;
  }

  /**
   * Total.
   *
   * @return the total number of jobs in the queue.
   */
  protected int total() {
    return this.waiting.size() + this.slow.size();
  }

  /**
   * Gets the.
   *
   * @param id the id of the job
   * @return the StepJob
   */
  protected PipelineJob get(String id) {
    if (id == null) { throw new NullPointerException("job id cannot be null"); }

    // check running job
    for (PipelineJob job : this.running) {
      if (id.equals(job.getId())) { return job; }
    }

    // check completed queue
    for (PipelineJob job : this.completed) {
      if (id.equals(job.getId())) { return job; }
    }

    // check the waiting queue
    for (PipelineJob job : this.waiting) {
      if (id.equals(job.getId())) { return job; }
    }

    // check the slow queue
    for (PipelineJob job : this.slow) {
      if (id.equals(job.getId())) { return job; }
    }

    return null;
  }

  /**
   * Gets the job status.
   *
   * @param jobid the id of the job
   * @return the status of job
   */
  protected JobStatus getJobStatus(String jobid) {
    PipelineJob job = get(jobid);
    if (job != null) { return job.getStatus(); }

    return null;
  }

  /**
   * TODO The design of PipelineJob Classes are not good. It needs to be rethought.
   *
   * @param packageId the package id
   * @return job id
   */
  public static String getJobId(String packageId) {
    String jobId = null;
    if (INSTANCE != null) {
      jobId = INSTANCE.packageAndJobMap.get(packageId);
    }
    return jobId;
  }

  /**
   * Completed.
   *
   * @param job set the job to completed queue
   */
  protected void completed(PipelineJob job) {
    this.completed.add(job);
    this.running.remove(job);
  }

  /**
   * clear the completed job.
   */
  private void clearCompletedJob() {
    for (PipelineJob job : this.completed) {
      if (this.completed.size() >= this._maxStoredCompletedJob && job.isInactive()) {
        this.completed.remove(job);
        this.packageAndJobMap.remove(job.getPackageData().id());
      }
    }
  }

  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("jobs");
    toXML(xml, this.completed, "completed");
    toXML(xml, this.running, "running");
    toXML(xml, this.slow, "slow");
    toXML(xml, this.waiting, "waiting");
    xml.closeElement();
  }

  private void toXML(XMLWriter xml, BlockingQueue<PipelineJob> queue, String name) throws IOException {
    xml.openElement(name);
    Iterator<PipelineJob> it = queue.iterator();
    while (it.hasNext()) {
      PipelineJob job = it.next();
      job.toXML(xml);
    }
    xml.closeElement();
  }

  /**
   * Clean all class variables and the singleton instance.
   */
  protected void clear(){
    if (this.waiting != null) {
      this.waiting.clear();
      this.waiting = null;
    }

    if (this.slow != null) {
      this.slow.clear();
      this.slow = null;
    }

    if (this.completed != null) {
      this.completed.clear();
      this.completed = null;
    }

    if (this.running != null) {
      this.running.clear();
      this.running = null;
    }

    if (this.packageAndJobMap != null) {
      this.packageAndJobMap.clear();
      this.packageAndJobMap = null;
    }
    LOCKED.set(true);
    INSTANCE = null;
  }
}
