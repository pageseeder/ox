/*
 * Copyright (c) 1999-2017 Allette systems pty. ltd.
 */
package org.pageseeder.ox.process;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.StepJob;

/**
 * A job queue to store the jobs.
 *
 * @author Carlos Cabral
 * @since 27 February 2017
 */
public class StepJobQueue {

  /** Maximo time in hour that a job will be stored in memory. */
  public static final int DEFAULT_MAX_STORED_COMPLETED_JOB = 1000;

  /** Singleton instance (Lazy init) */
  private static volatile StepJobQueue INSTANCE;

  /** The list of waiting job. */
  private final BlockingQueue<StepJob> _waiting;

  /** The list of completed jobs. */
  private final BlockingQueue<StepJob> _completed;

  /** The current running job. **/
  private final BlockingQueue<StepJob> _running;

  /** Max number of completed job stored in memory. */
  private final int _maxStoredCompletedJob;
  
  /**
   * the private constructor.
   *
   * @param maxStoredCompletedJob the max completed jobs that will be stored.
   */
  private StepJobQueue(int maxStoredCompletedJob) {
    this._waiting = new LinkedBlockingQueue<StepJob>();
    this._completed = new LinkedBlockingQueue<StepJob>();
    this._running = new LinkedBlockingQueue<StepJob>();
    if (maxStoredCompletedJob > 0 ) {
     this._maxStoredCompletedJob = maxStoredCompletedJob;
    } else {
      this._maxStoredCompletedJob = DEFAULT_MAX_STORED_COMPLETED_JOB;
    }
  }
  
  /**
   * @return the instance of ImportProcessor
   */
  protected static StepJobQueue getInstance() {
    return getInstance(DEFAULT_MAX_STORED_COMPLETED_JOB);
  }

  /**
   * @return the instance of ImportProcessor
   */
  protected static StepJobQueue getInstance(int maxStoredCompletedJob) {
    if (INSTANCE == null) {
      INSTANCE = new StepJobQueue(maxStoredCompletedJob);
    }
    return INSTANCE;
  }
  
  
  /**
   * @param job the job
   */
  protected void add(StepJob job) {
    this._waiting.add(job);

    //clear completed job when new job comes in.
    clearCompletedJob();
  }

  /**
   * @return the next processing job
   * @throws InterruptedException
   */
  protected StepJob next() throws InterruptedException {
    StepJob job = this._waiting.take();
    if (job != null) {
      this._running.add(job);
    }
    return job;
  }

  /**
   * @return the total number of jobs in the queue.
   */
  protected int total() {
    return this._waiting.size();
  }

  /**
   * @param id the id of the job
   * @return the StepJob
   */
  protected StepJob get(String id) {
    if (id == null) { throw new NullPointerException("job id cannot be null"); }

    // check running job
    for (StepJob job : this._running) {
      if (id.equals(job.getId())) { return job; }
    }

    // check completed queue
    for (StepJob job : this._completed) {
      if (id.equals(job.getId())) { return job; }
    }

    // check the waiting queue
    for (StepJob job : this._waiting) {
      if (id.equals(job.getId())) { return job; }
    }

    return null;
  }

  /**
   * @param jobid the id of the job
   * @return the status of job
   */
  protected JobStatus getJobStatus(String jobid) {
    StepJob job = get(jobid);
    if (job != null) { return job.getStatus(); }

    return null;
  }

  /**
   * @param job set the job to completed queue
   */
  protected void completed(StepJob job) {
    this._completed.add(job);
    this._running.remove(job);
  }

  /**
   * clear the completed job.
   */
  private void clearCompletedJob() {
    for (StepJob job : this._completed) {
      if (this._completed.size() >= this._maxStoredCompletedJob && job.isInactive()) {
        this._completed.remove(job);
      }
    }
  }

}
