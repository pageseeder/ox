/*
 *  Copyright (c) 2012 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.util;

import org.apache.commons.fileupload.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A simple ProgressListener for monitor the file upload</p> 
 * 
 * @author Ciber Cai
 * @version 06 March 2013
 */
public class FileUploadListener implements ProgressListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadListener.class);

  private long megaBytes = -1;

  private long percentage = 0L;

  @Override
  public void update(long bytesRead, long contentLength, int items) {
    long mBytes = bytesRead / 1000000;
    if (this.megaBytes == mBytes) { return; }
    LOGGER.debug("have read {} out of {} ", bytesRead, contentLength);
    if (bytesRead > 0 && contentLength > 0) {
      this.percentage = bytesRead * 100l / contentLength;
    }
    if (contentLength < 0) {
      this.percentage = -1;
    }
    this.megaBytes = mBytes;
  }

  /***
   * @return the percentage of the upload progress.
   */
  public long percentage() {
    return this.percentage;
  }
}
