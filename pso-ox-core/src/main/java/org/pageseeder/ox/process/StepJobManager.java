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

import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.StepJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A manager class for dealing with {@link StepJob}.
 *
 * @author Carlos Cabral
 * @since 27 February 2017
 */
public class StepJobManager {

  /** the logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(StepJobManager.class);

  /**
   * the default number of thread for processing step
   */
  public static final int DEAULT_NUMBER_OF_THREAD = 1;

  /** a thread executor */
  private static ExecutorService DEFAULT_EXECUTOR = null;

  /** the job queue */
  private StepJobQueue queue;

  /** the number of thread to process */
  private final int _noThreads;

  /**
   * the pipeline job manager
   */
  public StepJobManager() {
    this(DEAULT_NUMBER_OF_THREAD, StepJobQueue.DEFAULT_MAX_STORED_COMPLETED_JOB);
  }

  /**
   * the step job manager.
   *
   * @param nThreads              The number of step thread.
   * @param maxStoredCompletedJob The max number of completed job stored in memory
   */
  public StepJobManager(int nThreads, int maxStoredCompletedJob) {
    this._noThreads = nThreads;
    this.queue = StepJobQueue.getInstance(maxStoredCompletedJob);
    synchronized (StepJobManager.class) {
      if (DEFAULT_EXECUTOR == null) {
        start();
      }
    }

  }

  /**
   * Start the Step job manager.
   */
  private void start() {
    DEFAULT_EXECUTOR = Executors.newFixedThreadPool(this._noThreads, new ThreadFactory() {
      private int no = 1;

      @Override
      public Thread newThread(Runnable r) {
        LOGGER.info("Start a new Step job manager - {}", this.no);
        Thread t = new Thread(r, "Step job manager - " + (this.no++));
        return t;
      }
    });
  }

  /**
   * Stop.
   */
  public void stop() {
    LOGGER.debug("Stopping the Step job manager.");
    if (DEFAULT_EXECUTOR != null) {
      DEFAULT_EXECUTOR.shutdown();
      DEFAULT_EXECUTOR = null;
    }
    if (this.queue != null) {
      this.queue.clear();
      this.queue = null;
    }
    LOGGER.debug("Stopped.");
  }

  /**
   * Add a job to process.
   *
   * @param job the StepJob
   */
  public void addJob(StepJob job) {
    // add job to queue
    this.queue.add(job);
    // start  thread or use the existing thread in the pool
    DEFAULT_EXECUTOR.execute(new StepJobProcessor(this.queue));
  }

  /**
   * Check job status job status.
   *
   * @param id the id of job
   * @return the status of job
   */
  public JobStatus checkJobStatus(String id) {
    return this.queue.getJobStatus(id);
  }

  /**
   * No waiting job int.
   *
   * @return the total number of jobs in the waiting queue.
   */
  public int noWaitingJob() {
    return this.queue.total();
  }

  /**
   * Gets job id.
   *
   * @param id the id of job
   * @return the StepJob
   */
  public StepJob getJobId(String id) {
    return this.queue.get(id);
  }
}
