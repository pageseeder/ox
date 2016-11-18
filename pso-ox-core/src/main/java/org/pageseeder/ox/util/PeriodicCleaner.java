/*
 * Copyright (c) 2014 Allette Systems (Australia) Pty. Ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically removes old packages from the temporary directory.
 *
 * @author Christophe Lauret
 * @since  28 October 2013
 */
public final class PeriodicCleaner {

  /**
   * Initial delay before the first cleanup (2 minutes).
   */
  public static final long INITIAL_DELAY_MINUTES = 2;

  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicCleaner.class);

  /**
   * Create a scheduler.
   */
  private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

  /**
   * The maximum age for the package date (1 hour).
   */
  private static final long MAX_AGE = 1000 * 60 * 30;

  /**
   * The directory to clean on a regular basis.
   */
  private static volatile File directory = null;

  /**
   * The directory to clean on a regular basis.
   */
  private static volatile File download = null;

  /**
   * A handle on the scheduled cleaner.
   */
  private static ScheduledFuture<?> handle = null;

  /**
   * Utility class.
   */
  private PeriodicCleaner() {}

  /**
   * Start the cleaning thread.
   *
   * @param rate the period in minutes between clean ups
   */
  public synchronized static void start(int rate) {
    if (handle != null) throw new IllegalStateException("Cleaner already started");
    handle = SCHEDULER.scheduleAtFixedRate(new Cleaner(), INITIAL_DELAY_MINUTES, rate, TimeUnit.MINUTES);
    LOGGER.info("Started cleaner thread with rate={} minutes (initial delay={} minutes)", rate, INITIAL_DELAY_MINUTES);
  }

  /**
   * Stops the cleaning thread.
   */
  public synchronized static void stop() {
    if (handle != null) {
      // do not interrupt
      handle.cancel(false);
      handle = null;
    }
  }

  /**
   * @param directory the directory to set
   */
  public static void setDirectory(File directory) {
    PeriodicCleaner.directory = directory;
  }

  /**
   * @param directory the directory to set
   */
  public static void setDownload(File directory) {
    PeriodicCleaner.download = directory;
  }

  /**
   * Delete temporary files created by this application which are at least an hour old.
   *
   * @throws IOException
   */
  public static void clean() throws IOException {
    if (directory == null) {
      LOGGER.warn("Directory to clean is not set properly");
    }
    clean(directory);
    if (download == null) {
      clean(download);
    }
  }

  /**
   * @param file the file/folder needs to delete
   * @throws IOException
   */
  public static void clean(final File file) throws IOException {
    if (file == null || !file.exists()) { throw new IllegalArgumentException("The file is null or it doesn't exist."); }

    long threshold = System.currentTimeMillis() - MAX_AGE;
    if (file.isDirectory() && file.lastModified() < threshold) {
      for (File f : file.listFiles()) {
        clean(f);
      }
      // empty folder
      if (file.listFiles().length == 0) {
        file.delete();
      }
    } else {
      file.delete();
    }
  }

  /**
   * Command that actually performs the cleaning.
   *
   * @author Christophe Lauret
   * @since  25 October 2013
   */
  private static class Cleaner implements Runnable {

    /**
     * Simply invokes the {@link #clean()} method.
     */
    @Override
    public void run() {
      try {
        clean();
      } catch (IOException ex) {
        LOGGER.warn("Cannot clean the working folders.", ex);
      }
    }
  }

}
