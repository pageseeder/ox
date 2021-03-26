/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.process;

import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

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

  /**  The list of imported jobs. */
  private final BlockingQueue<PipelineJob> _waiting;

  /**  the list of slow jobs. */
  private final BlockingQueue<PipelineJob> _slow;

  /**  The list of completed jobs. */
  private final BlockingQueue<PipelineJob> _completed;

  /**  The current running import job *. */
  private final BlockingQueue<PipelineJob> _running;

  /** The key will be the package and the value the job id. */
  private final ConcurrentHashMap<String, String> _packageAndJobMap;

  /**
   * the private constructor.
   */
  private PipelineJobQueue(int maxStoredCompletedJob) {
    this._waiting = new LinkedBlockingQueue<PipelineJob>();
    this._slow = new LinkedBlockingQueue<PipelineJob>();
    this._completed = new LinkedBlockingQueue<PipelineJob>();
    this._running = new LinkedBlockingQueue<PipelineJob>();
    this._maxStoredCompletedJob = maxStoredCompletedJob;
    this._packageAndJobMap = new ConcurrentHashMap<>();

  }

  /**
   * Gets the single instance of PipelineJobQueue.
   *
   * @param maxStoredCompletedJob the max stored completed job
   * @return the instance of ImportProcessor
   */
  public static PipelineJobQueue getInstance(int maxStoredCompletedJob) {
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
      this._slow.add(job);
    } else {
      this._waiting.add(job);
    }

    this._packageAndJobMap.put(job.getPackageData().id(), job.getId());
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
    PipelineJob job = slowMode ? this._slow.take() : this._waiting.take();
    if (job != null) {
      this._running.add(job);
    }
    return job;
  }

  /**
   * Total.
   *
   * @return the total number of jobs in the queue.
   */
  protected int total() {
    return this._waiting.size() + this._slow.size();
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
    for (PipelineJob job : this._running) {
      if (id.equals(job.getId())) { return job; }
    }

    // check completed queue
    for (PipelineJob job : this._completed) {
      if (id.equals(job.getId())) { return job; }
    }

    // check the waiting queue
    for (PipelineJob job : this._waiting) {
      if (id.equals(job.getId())) { return job; }
    }

    // check the slow queue
    for (PipelineJob job : this._slow) {
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
   * @param packageId
   * @return
   */
  public static String getJobId(String packageId) {
    String jobId = null;
    if (INSTANCE != null) {
      jobId = INSTANCE._packageAndJobMap.get(packageId);
    }
    return jobId;
  }

  /**
   * Completed.
   *
   * @param job set the job to completed queue
   */
  protected void completed(PipelineJob job) {
    this._completed.add(job);
    this._running.remove(job);
  }

  /**
   * clear the completed job.
   */
  private void clearCompletedJob() {
    for (PipelineJob job : this._completed) {
      if (this._completed.size() >= this._maxStoredCompletedJob && job.isInactive()) {
        this._completed.remove(job);
        this._packageAndJobMap.remove(job.getPackageData().id());
      }
    }
  }

  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("jobs");
    toXML(xml, this._completed, "completed");
    toXML(xml, this._running, "running");
    toXML(xml, this._slow, "slow");
    toXML(xml, this._waiting, "waiting");
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
}
