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
import org.pageseeder.ox.core.StepJob;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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

  /** Shows that this queue is locked. This means that cannot be used. */
  private static volatile AtomicBoolean LOCKED = new AtomicBoolean(false);

  /** The list of waiting job. */
  private BlockingQueue<StepJob> waiting;

  /** The list of completed jobs. */
  private BlockingQueue<StepJob> completed;

  /** The current running job. **/
  private BlockingQueue<StepJob> running;

  /** Max number of completed job stored in memory. */
  private int _maxStoredCompletedJob;

  /**
   * the private constructor.
   *
   * @param maxStoredCompletedJob the max completed jobs that will be stored.
   */
  private StepJobQueue(int maxStoredCompletedJob) {
    this.waiting = new LinkedBlockingQueue<StepJob>();
    this.completed = new LinkedBlockingQueue<StepJob>();
    this.running = new LinkedBlockingQueue<StepJob>();
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
  protected static synchronized StepJobQueue getInstance(int maxStoredCompletedJob) {
    if (LOCKED.get()) new OXException("Step job queue is locked to use.");
    if (INSTANCE == null) {
      INSTANCE = new StepJobQueue(maxStoredCompletedJob);
    }
    return INSTANCE;
  }

  /**
   * @param job the job
   */
  protected void add(StepJob job) {
    this.waiting.add(job);

    //clear completed job when new job comes in.
    clearCompletedJob();
  }

  /**
   * @return the next processing job
   * @throws InterruptedException
   */
  protected StepJob next() throws InterruptedException {
    StepJob job = this.waiting.take();
    if (job != null) {
      this.running.add(job);
    }
    return job;
  }

  /**
   * @return the total number of jobs in the queue.
   */
  protected int total() {
    return this.waiting.size();
  }

  /**
   * @param id the id of the job
   * @return the StepJob
   */
  protected StepJob get(String id) {
    if (id == null) { throw new NullPointerException("job id cannot be null"); }

    // check running job
    for (StepJob job : this.running) {
      if (id.equals(job.getId())) { return job; }
    }

    // check completed queue
    for (StepJob job : this.completed) {
      if (id.equals(job.getId())) { return job; }
    }

    // check the waiting queue
    for (StepJob job : this.waiting) {
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
    this.completed.add(job);
    this.running.remove(job);
  }

  /**
   * clear the completed job.
   */
  private void clearCompletedJob() {
    for (StepJob job : this.completed) {
      if (this.completed.size() >= this._maxStoredCompletedJob && job.isInactive()) {
        this.completed.remove(job);
      }
    }
  }

  /**
   * Clean all class variables and the singleton instance.
   */
  protected void clear(){
    if (this.waiting != null) {
      this.waiting.clear();
      this.waiting = null;
    }

    if (this.completed != null) {
      this.completed.clear();
      this.completed = null;
    }

    if (this.running != null) {
      this.running.clear();
      this.running = null;
    }
    LOCKED.set(true);
    INSTANCE = null;
  }
}
