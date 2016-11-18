/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.process;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.PipelineJob;

/**
 * A job queue to store the jobs.
 *
 * @author Ciber Cai
 * @since 4 April 2016
 */
class PipelineJobQueue {

  /** the total number of completed jobs to store in memory */
  private static final int MAX_COMPLETED_JOBS_TO_STORE = 1000;

  /** Singleton instance (Lazy init) */
  private static volatile PipelineJobQueue INSTANCE;

  /** The list of imported jobs */
  private final BlockingQueue<PipelineJob> _waiting;

  /** the list of slow jobs */
  private final BlockingQueue<PipelineJob> _slow;

  /** The list of completed jobs */
  private final BlockingQueue<PipelineJob> _completed;

  /** The current running import job **/
  private final BlockingQueue<PipelineJob> _running;

  /**
   * the private constructor
   */
  private PipelineJobQueue() {
    this._waiting = new LinkedBlockingQueue<PipelineJob>();
    this._slow = new LinkedBlockingQueue<PipelineJob>();
    this._completed = new LinkedBlockingQueue<PipelineJob>();
    this._running = new LinkedBlockingQueue<PipelineJob>();

  }

  /**
   * @return the instance of ImportProcessor
   */
  protected static PipelineJobQueue getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PipelineJobQueue();
    }
    return INSTANCE;
  }

  /**
   * @param job the job
   */
  protected void add(PipelineJob job) {
    if (job.isSlowJob()) {
      this._slow.add(job);
    } else {
      this._waiting.add(job);
    }

    //clear completed job when new job comes in.
    clearCompletedJob();
  }

  /**
   * @return the next processing job
   * @throws InterruptedException
   */
  protected PipelineJob next(boolean slowMode) throws InterruptedException {
    PipelineJob job = slowMode ? this._slow.take() : this._waiting.take();
    if (job != null) {
      this._running.add(job);
    }
    return job;
  }

  /**
   * @return the total number of jobs in the queue.
   */
  protected int total() {
    return this._waiting.size() + this._slow.size();
  }

  /**
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
   * @param jobid the id of the job
   * @return the status of job
   */
  protected JobStatus getJobStatus(String jobid) {
    PipelineJob job = get(jobid);
    if (job != null) { return job.getStatus(); }

    return null;
  }

  /**
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
      if (this._completed.size() >= MAX_COMPLETED_JOBS_TO_STORE && job.isInactive()) {
        this._completed.remove(job);
      }
    }
  }

}
