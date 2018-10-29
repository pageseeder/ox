/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pageseeder.ox.process.PipelineJobQueue;
import org.pageseeder.ox.util.CleanUpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Cabral
 * @since 29 Oct. 2018
 */
public class CleanUpJob implements Runnable {
  
   /** The logger job. */
   private final Logger LOGGER_JOB = LoggerFactory.getLogger(CleanUpJob.class);
   
   /** Indicates if the thread should stop (true) or not (false). */
   private AtomicBoolean stop = new AtomicBoolean(false); 
   
   /** how long a file can be inactive in the drive. */
   private final long _maxInactiveTime;
   
   /** The packages root directory. */
   private final File _base;
   
   /** Indicate job status. */
   private volatile CleanUpStatus status;
   
   /** check up Delay in milliseconds. */
   private final long _checkUpDelay;
   
   /**
    * Instantiates a new clean up job.
    *
    * @param maxInactiveTime How long a file can be inactive in the drive.
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
  
   /* (non-Javadoc)
    * @see java.lang.Runnable#run()
    */
   @Override
   public void run() {
     
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
   }
   
   /**
    * Delete temporary files created by this application which are at least an hour old.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void clean() throws IOException {
     clean(this.getBase(), 0);
   }
  
   /**
    * Clean.
    *
    * @param file the file/folder needs to delete
    * @param depth the depth
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void clean(final File file, int depth) throws IOException {
     //keep deleting while it is false
     if (!this.getStop().get()) {
       if (file == null || !file.exists()) { throw new IllegalArgumentException("The file is null or it doesn't exist."); }
  
       long threshold = System.currentTimeMillis() - this.getMaxInactiveTime();
       boolean isExpired =  file.lastModified() < threshold;
       
       if (file.isDirectory()) {         
         //If depth equal 1 this file is the package folder which its name is the package id. If this package is in the job list then
         //should not be removed. Otherwise yes
         final boolean isInJobList = depth == 1 && !StringUtils.isBlank(PipelineJobQueue.getJobId(file.getName()));

         if (!isInJobList) {
           //Go through the files and sub directories
           for (File f : file.listFiles()) {
             clean(f, depth+1);
           }
           
           // If the folder is empyt and it is not the base folder (depth != 0)
           final boolean isBaseFolder = depth == 0;
           final boolean isEmpyt = file.listFiles().length == 0;
           if (isEmpyt && !isBaseFolder) {
             //Delete directory
             delete(file);
           }
         }
       } else if (isExpired) {
         //Delete file
         delete(file);
       }
     }
   }
   
   /**
    * Delete file if the time allowed to stored in the drive is over.
    *
    * @param candidate the candidate
    */
   private void delete (File candidate) {
     long threshold = System.currentTimeMillis() - this._maxInactiveTime;
     if (candidate.lastModified() < threshold) {
       if (candidate.delete()) {
         LOGGER_JOB.trace("Deleted file {}.", candidate.getAbsolutePath());
       } else {
         LOGGER_JOB.error("Failed to delete file {}.", candidate.getAbsolutePath());
       }
     }
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