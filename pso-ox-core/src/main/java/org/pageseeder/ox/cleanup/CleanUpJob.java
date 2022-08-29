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
package org.pageseeder.ox.cleanup;

import org.pageseeder.ox.process.PipelineJobQueue;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Carlos Cabral
 * @since 29 Oct. 2018
 */
public class CleanUpJob implements Runnable {

   /** The logger job. */
   private final Logger LOGGER_JOB = LoggerFactory.getLogger(CleanUpJob.class);

   /** Indicates if the thread should stop (true) or not (false). */
   private AtomicBoolean stop = new AtomicBoolean(false);

   /** how long (milliseconds) a file can be inactive in the drive. */
   private final long _maxInactiveTime;

   /** The packages root directory. */
   private final File _base;

   /** Indicate job status. */
   private volatile CleanUpStatus status;

   /** check up Delay in milliseconds. */
   private final long _checkUpDelay;

   /**
    * Store a list of files that must be ignored and not deleted.
    * However if the file is a folder, if the subfiles are not in this list then they will be deleted.
    */
   private final List<File> filesToIgnore = new ArrayList<>();

   /**
    * Instantiates a new clean up job.
    *
    * @param maxInactiveTime How long (milliseconds) a file can be inactive in the drive.
    * @param checkUpDelay the check up delay
    * @param base The packages root directory.
    */
   public CleanUpJob(long maxInactiveTime, long checkUpDelay, File base) {
     super();
     LOGGER_JOB.debug("Max Inactive Time: {}", maxInactiveTime);
     LOGGER_JOB.debug("Base Directory: {}", base);

     //Validate inputs
     if (maxInactiveTime < 1) throw new IllegalArgumentException("Max Inactive Time must be positive.");
     if (checkUpDelay < 1) throw new IllegalArgumentException("Delay must be positive.");
     if (base == null || !base.exists() || !base.isDirectory()) throw new IllegalArgumentException("Base Directory is invalid.");

     //Set class atttributes
     this._maxInactiveTime = maxInactiveTime;
     this._checkUpDelay = checkUpDelay;
     this._base = base;
     this.setStatus(CleanUpStatus.NOT_STARTED);
   }

   /**
    * Add file to be ignored and then they will not be deleted.
    * @param toIgnore
    */
   public void addFileToIgnore(File toIgnore){
     this.filesToIgnore.add(toIgnore);
   }

   /**
    * remove file that is supposed to be ignored.
    * @param toIgnore
    */
   public void removeFileToIgnore(File toIgnore) {
     this.filesToIgnore.remove(toIgnore);
   }

   /* (non-Javadoc)
    * @see java.lang.Runnable#run()
    */
   @Override
   public void run() {
     try {
       LOGGER_JOB.info("Start clean up JOB at {}", format(LocalDateTime.now()));
       while (!this.getStop().get()) {

         LOGGER_JOB.debug("Running another clean up at {}", format(LocalDateTime.now()));
         try {
           this.setStatus(CleanUpStatus.RUNNING);
           this.clean();
         } catch (IOException ex) {
           LOGGER_JOB.error("Catched the following error '{}' while performing the clean up. ", ex.getMessage());
         }
         LOGGER_JOB.debug("Finished another clean up at {}", format(LocalDateTime.now()));

         //Waiting for next iteraction
         this.setStatus(CleanUpStatus.WAITING_NEXT_ITERACTION);
         try {
           Thread.sleep(this.getCheckUpDelay());
         } catch (InterruptedException ex) {
           break;
         }
       }
       this.setStatus(CleanUpStatus.STOPPED);
       LOGGER_JOB.info("Stop clean up JOB at {}", format(LocalDateTime.now()));
     } catch (Exception ex) {
       LOGGER_JOB.error("Clean up JOB failed at {} due to {}", format(LocalDateTime.now()), ex.getMessage());
       this.setStatus(CleanUpStatus.FAILED);
     }
   }

   /**
    * Delete temporary files created by this application which are at least an hour old.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void clean() throws IOException {
     LOGGER_JOB.trace("Cleaning Starter Folder: {}", this.getBase().getAbsolutePath());
     clean(this.getBase(), 0);
   }

   /**
    * Clean.
    *
    * &gt;p&lt;Depth:&gt;/p&lt;
    * &gt;ul&lt;
    *   &gt;li&lt;- depth == 0 means base folder &gt;/li&lt;
    *   &gt;li&lt;- depth == 1 means package or upload folder &gt;/li&lt;
    *   &gt;li&lt;- depth > 1  means inside packages or uploads folder &gt;/li&lt;
    * &gt;/ul&lt;
    *
    *
    * @param file the file/folder needs to delete
    * @param depth the depth
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void clean(final File file, int depth) throws IOException {
     //keep deleting while it is false
     LOGGER_JOB.trace("Current File {} and depth {}", file.getAbsolutePath(), depth);
     if (!this.getStop().get()) {
       if (file == null || !file.exists()) { throw new IllegalArgumentException("The file is null or it doesn't exist."); }

       long threshold = System.currentTimeMillis() - this.getMaxInactiveTime();
       boolean isExpired =  file.lastModified() < threshold;

       if (file.isDirectory()) {
         // Depth equal 1 t means package folder which its name is the package id.
         // If this package is in the job list then should not be removed.
         // If it is not in the package and neither expired, then it should be kept.
         // If it is upload folder it should go forward and delete inside files if expired
         // (Normally upload folder is file that should be ignored).
         final boolean isInJobList = depth == 1 && !StringUtils.isBlank(PipelineJobQueue.getJobId(file.getName()));
         final boolean isUploadFolder = depth == 1 && shouldBeIgnored(file);
         final boolean keepFolder =  (isInJobList) || (depth == 1 && !isInJobList && !isExpired);


         LOGGER_JOB.trace("Current File {} and is in job list {}", file.getAbsolutePath(), isInJobList);
         LOGGER_JOB.trace("Current File {} and is Expired {}", file.getAbsolutePath(), isExpired);
         LOGGER_JOB.trace("Current File {} and is upload {}", file.getAbsolutePath(), isUploadFolder);
         LOGGER_JOB.trace("Current File {} and keep folder {}", file.getAbsolutePath(), keepFolder);

         if (!keepFolder || isUploadFolder) {
           //The orignal last modified date before deleting its children files. because when one is deleted, the parent
           // folder last modified date is updated.
           long originalLastModifiedDate = file.lastModified();

           //Go through the files and sub directories
           for (File f : file.listFiles()) {
             clean(f, depth+1);
           }

           // If the folder is empyt and it is not the base folder (depth != 0).
           final boolean isBaseFolder = depth == 0;
           final boolean isEmpyt = file.listFiles().length == 0;
           if (isEmpyt && !isBaseFolder) {
             //Delete directory
             delete(file, originalLastModifiedDate);
           }
         }
       } else if (isExpired) {
         //Delete file
         delete(file, file.lastModified());
       }
     }
   }

   /**
    * Delete file if the time allowed to stored in the drive is over.
    *
    * @param toDelete The file to be deleted.
    * @param lastModifiedDate The 'toDelete' last modified date.
    */
   private void delete (File toDelete, long lastModifiedDate) {
     long threshold = System.currentTimeMillis() - this._maxInactiveTime;
     if (lastModifiedDate < threshold && !shouldBeIgnored(toDelete)) {
       if (toDelete.delete()) {
         LOGGER_JOB.trace("Deleted file {}.", toDelete.getAbsolutePath());
       } else {
         LOGGER_JOB.error("Failed to delete file {}.", toDelete.getAbsolutePath());
       }
     }
   }

   /**
    * Returns true if this file should be ignored and not deleted. Otherwise true.
    *
    * @param toDelete
    * @return
    */
   private boolean shouldBeIgnored(File toDelete) {
     return this.filesToIgnore.contains(toDelete);
   }

   /**
    * Format.
    *
    * @param time the time
    * @return the string
    */
   private String format(LocalDateTime time) {
     return time.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
   }

   /**
    * Stop.
    */
   public void stop() {
     this.setStop(true);
     this.setStatus(CleanUpStatus.STOPPING);
   }

   /**
    * Gets the status.
    *
    * @return the status
    */
   public CleanUpStatus getStatus() {
     return this.status;
   }

   /**
    * Sets the status.
    *
    * @param status the new status
    */
   private void setStatus(CleanUpStatus status) {
     this.status = status;
   }

   /**
    * Gets the max inactive time.
    *
    * @return the max inactive time
    */
   public long getMaxInactiveTime() {
     return _maxInactiveTime;
   }

   /**
    * Gets the base.
    *
    * @return the base
    */
   public File getBase() {
     return _base;
   }

   /**
    * Gets the check up delay.
    *
    * @return the check up delay
    */
   public long getCheckUpDelay() {
     return _checkUpDelay;
   }

   /**
    * Gets the stop.
    *
    * @return the stop
    */
   public AtomicBoolean getStop() {
     return stop;
   }

   /**
    * Sets the stop.
    *
    * @param stop the new stop
    */
   private void setStop(boolean stop) {
     this.stop.set(stop);
   }
}