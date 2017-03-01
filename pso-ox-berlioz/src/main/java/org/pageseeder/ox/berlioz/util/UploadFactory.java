/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.pageseeder.berlioz.content.ContentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The factory class to produce UploadProcessor.
 *
 * @author Ciber Cai
 * @version 10 February 2014
 */
public class UploadFactory {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(UploadFactory.class);

  /**  Singleton instance. */
  private static volatile UploadFactory singleton;

  /**  the list of upload job. */
  private final BlockingQueue<UploadJob> jobs;

  /**
   * Instantiates a new upload factory.
   */
  private UploadFactory() {
    this.jobs = new LinkedBlockingQueue<UploadJob>();
  }

  /**
   * Gets the single instance of UploadFactory.
   *
   * @return the instance of UploadFactory.
   */
  public static UploadFactory getInstance() {
    if (singleton == null) {
      singleton = new UploadFactory();
    }
    return singleton;
  }

  /**
   * Make.
   *
   * @param req the ContentRequest
   * @return the UploadProcessor.
   * @throws FileUploadException throw exception when FileUPload error occur.
   */
  public UploadProcessor make(HttpServletRequest req) throws FileUploadException {
    LOGGER.debug("Create a upload processor");
    UploadProcessor manager = new UploadProcessor(req);
    String jobid = req.getSession().getId();
    UploadJob job = new UploadJob(jobid, manager.getProgressListener());
    boolean added = this.jobs.add(job);
    LOGGER.debug("added {}", added);
    // clear the inactive job
    clearCompletedJob();
    return manager;
  }

  /**
   * Gets the upload job.
   *
   * @param req the req
   * @return the UploadJob
   */
  public UploadJob getUploadJob(ContentRequest req) {
    String jobid = req.getSession().getId();
    for (UploadJob job : this.jobs) {
      if (jobid.equals(job.getJobId())) { return job; }
    }
    clearCompletedJob();
    return null;
  }

  /**
   * clear the completed job.
   */
  private void clearCompletedJob() {
    for (UploadJob job : this.jobs) {
      if (job.isInactive()) {
        this.jobs.remove(job);
      }
    }
  }

}
