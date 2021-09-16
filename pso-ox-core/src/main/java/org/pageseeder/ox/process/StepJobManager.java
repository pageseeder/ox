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
 * @since  27 February 2017
 */
public class StepJobManager {

  /** the logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(StepJobManager.class);

  /** the default number of thread for processing pipeline */
  public static final int DEAULT_NUMBER_OF_THREAD = 1;

  /** a thread executor */
  private static ExecutorService DEFAULT_EXECUTOR = null;

  /** the job queue */
  private final StepJobQueue _queue;

  /** the number of thread to process */
  private final int _noThreads;

  /**
   * the pipeline job manager
   */
  public StepJobManager() {
    this(DEAULT_NUMBER_OF_THREAD, StepJobQueue.DEFAULT_MAX_STORED_COMPLETED_JOB);
  }

  /**
   * the pipeline job manager.
   *
   * @param nThreads               The number of pipeline thread.
   * @param maxStoredCompletedJob  The max number of completed job stored in memory
   */
  public StepJobManager(int nThreads, int maxStoredCompletedJob) {
    this._noThreads = nThreads;
    this._queue = StepJobQueue.getInstance(maxStoredCompletedJob);
    synchronized (StepJobManager.class) {
      if (DEFAULT_EXECUTOR == null) {
        start();
      }
    }

  }

  /**
   * Start the Pipeline job.
   */
  private void start() {
    DEFAULT_EXECUTOR = Executors.newFixedThreadPool(this._noThreads, new ThreadFactory() {
      private int no = 1;

      @Override
      public Thread newThread(Runnable r) {
        LOGGER.info("Start a new Pipeline Processor - {}", this.no);
        Thread t = new Thread(r, "Pipeline Processor - " + (this.no++));
        return t;
      }
    });
  }

  public void stop() {
    LOGGER.debug("Stopping the Pipeline Processor.");
    if (DEFAULT_EXECUTOR != null) {
      DEFAULT_EXECUTOR.shutdown();
    }
    LOGGER.debug("Stopped.");
  }

  /**
   * Add a job to process.
   * @param job the StepJob
   */
  public void addJob(StepJob job) {
    // add job to queue
    this._queue.add(job);
    // start  thread or use the existing thread in the pool
    DEFAULT_EXECUTOR.execute(new StepJobProcessor(this._queue));
  }

  /**
   * @param id the id of job
   *
   * @return the status of job
   */
  public JobStatus checkJobStatus(String id) {
    return this._queue.getJobStatus(id);
  }

  /**
   * @return the total number of jobs in the waiting queue.
   */
  public int noWaitingJob() {
    return this._queue.total();
  }

  /**
   * @param id the id of job
   * @return the StepJob
   */
  public StepJob getJobId(String id) {
    return this._queue.get(id);
  }
}
