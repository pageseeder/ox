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
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A manager class for dealing with {@link PipelineJob}.
 *
 * @author Ciber Cai
 * @since  10 November 2014
 */
public class PipelineJobManager implements XMLWritable{

  /** the logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineJobManager.class);

  /** a thread executor */
  private static ExecutorService DEFAULT_EXECUTOR = null;

  /** a thread executor */
  private static ExecutorService SLOW_EXECUTOR = null;

  /** the job queue */
  private PipelineJobQueue queue;

  /** the number of thread to process */
  private final int _noThreads;

  /**
   * the pipeline job manager
   */
  public PipelineJobManager() {
    this(StepJobManager.DEAULT_NUMBER_OF_THREAD, StepJobQueue.DEFAULT_MAX_STORED_COMPLETED_JOB);
  }

  /**
   * the pipeline job manager
   * @param nThreads number of pipeline thread.
   */
  public PipelineJobManager(int nThreads, int maxStoredCompletedJob) {
    this._noThreads = nThreads;
    this.queue = PipelineJobQueue.getInstance(maxStoredCompletedJob);
    synchronized (PipelineJobManager.class) {
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

    SLOW_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        LOGGER.info("Start Slow Lane Pipeline Processor.");
        return new Thread(r, "Pipeline Processor - Slow Land.");
      }
    });
  }

  public void stop() {
    LOGGER.debug("Stopping the Pipeline Processor.");
    if (DEFAULT_EXECUTOR != null) {
      DEFAULT_EXECUTOR.shutdown();
      DEFAULT_EXECUTOR = null;
    }

    if (SLOW_EXECUTOR != null) {
      SLOW_EXECUTOR.shutdown();
      SLOW_EXECUTOR = null;
    }

    if (this.queue != null) {
      this.queue.clear();
      this.queue = null;
    }
    LOGGER.debug("Stopped.");
  }

  /**
   * Add a job to process.
   * @param job the PipelineJob
   */
  public void addJob(PipelineJob job) {
    // add job to queue
    this.queue.add(job);
    if (job.isSlowJob()) {
      SLOW_EXECUTOR.execute(new PipelineProcessor(this.queue, true));
    } else {
      // start  thread or use the existing thread in the pool
      DEFAULT_EXECUTOR.execute(new PipelineProcessor(this.queue, false));
    }

  }

  /**
   * @param id the id of job
   *
   * @return the status of job
   */
  public JobStatus checkJobStatus(String id) {
    return this.queue.getJobStatus(id);
  }

  /**
   * @return the total number of jobs in the waiting queue.
   */
  public int noWaitingJob() {
    return this.queue.total();
  }

  /**
   * @param id the id of job
   * @return the PipelineJob
   */
  public PipelineJob getJobId(String id) {
    return this.queue.get(id);
  }

  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    this.queue.toXML(xml);
  }
}
