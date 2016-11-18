/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.process;

import java.io.File;
import java.io.IOException;

import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.Downloadable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.Pipeline;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.core.StepDefinition;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A thread for processing step jobs from pipeline.</p>
 *
 * @author Ciber Cai
 * @since  07 November 2014
 */
class PipelineProcessor implements Runnable {

  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineProcessor.class);

  private final PipelineJobQueue _queue;

  private final boolean _slowMode;

  PipelineProcessor(PipelineJobQueue queue) {
    this._queue = queue;
    this._slowMode = false;
  }

  /**
   * @param queue the job queue.
   * @param slowMode whether to use slow mode
   */
  PipelineProcessor(PipelineJobQueue queue, boolean slowMode) {
    this._queue = queue;
    this._slowMode = slowMode;
  }

  @Override
  public void run() {
    while (true) {
      try {
        // get the first waiting job from queue
        PipelineJob job = this._queue.next(this._slowMode);

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
  private static void process(PipelineJob job) {
    Pipeline pipeline = job.getPipeline();
    PackageData data = job.getPackageData();
    JobStatus status = job.getStatus();
    int perc = 100 / pipeline.size();
    int buffer = 5;
    boolean failed = false;

    LOGGER.debug("total number of step {} ", pipeline.size());
    for (int i = 0; i < pipeline.size(); i++) {
      LOGGER.debug("processing the [{}] step ", i + 1);
      status.setPercentage(status.getPercentage() + buffer);
      StepDefinition stepDef = pipeline.getStep(i);
      // execute the step
      Result result = stepDef.exec(data);

      status.setPercentage(status.getPercentage() + perc - buffer);

      // set the download path if the result object is {@link Downloadable}
      if (result.status() == ResultStatus.OK && result instanceof Downloadable) {
        File output = ((Downloadable) result).downloadPath();

        if (output != null && output.isFile()) {
          String filename = data.id() + File.separator + output.getName();
          File destFile = new File(OXConfig.getOXTempFolder(), filename);
          if (!destFile.getParentFile().exists()) {
            destFile.mkdirs();
          }
          LOGGER.debug("store file to {}", destFile.getAbsoluteFile());
          try {
            FileUtils.copy(output, destFile);
          } catch (IOException ex) {
            LOGGER.debug("Cannot copy file from  {} to {}", output, destFile, ex);
            failed = true;
          }
          job.setDownload(filename);
        } else if (output != null && output.isDirectory()) {
          String filename = data.id() + "/" + System.nanoTime() + "-" + output.getName() + ".zip";
          File destFile = new File(OXConfig.getOXTempFolder(), filename);

          if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
          }
          try {
            ZipUtils.zip(output, destFile);
          } catch (IOException ex) {
            LOGGER.error("Cannot compress folder {} to file {}", output, destFile, ex);
          }
          LOGGER.debug("store dir to {}", destFile.getAbsoluteFile());
          // FileUtils.copyFile(output, destFile);
          job.setDownload(filename);
        }
      }

      // catch the error
      if (result.status() == ResultStatus.ERROR) {
        LOGGER.debug("result {}", result.status());
        job.failed(result);
        failed = true;
        break;
      }
    }
    // set the status to completed.
    if (!failed) {
      job.completed();
    }
  }

}
