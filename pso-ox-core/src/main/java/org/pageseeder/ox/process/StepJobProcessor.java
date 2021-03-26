/*
 *  Copyright (c) 2017 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.process;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepDefinition;
import org.pageseeder.ox.core.StepJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>A thread for processing step jobs.</p>
 *
 * @author Carlos Cabral
 * @since  27 February 2017
 */
class StepJobProcessor implements Runnable {

  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(StepJobProcessor.class);

  private final StepJobQueue _queue;

  StepJobProcessor(StepJobQueue queue) {
    this._queue = queue;
  }

  @Override
  public void run() {
    while (true) {
      try {
        // get the first waiting job from queue
        StepJob job = this._queue.next();

        // process the job
        LOGGER.debug("start job {}", job.getId());
        process(job);

        // put it to completed list
        if (job.getStatus().hasCompleted()) {
          this._queue.completed(job);
        }

        LOGGER.debug("job completed ? {}", job.getStatus());

        Thread.sleep(1000);

      } catch (InterruptedException ex) {
        LOGGER.warn("Pipeline process thread error {}.", ex);
      }
    }

  }

  /**
   * Process the pipeline.
   *
   * @param job the job needs to process.
   * @throws IOException when I/O error occur.
   */
  private static void process(StepJob job) {
    StepDefinition step = job.getStep();
    PackageData data = job.getPackageData();
    //Change the status from stopped to processing.
    job.started();
    LOGGER.debug("processing the step {}", step.name());

    // execute the step
    //TODO HTTPSession
    Result result = step.exec(data);

    LOGGER.debug("result {}", result.status());
    switch (result.status()) {
    case OK:
    case WARNING:
      // Set to complete if Status equal OK or WARNING
      job.completed(result);
      break;
    case ERROR:
      //Set to failed
      job.failed(result);
      break;
    }
  }
}
