/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.cleanup;

/**
 * @author Carlos Cabral
 * @since 29 Oct. 2018
 */
public enum CleanUpStatus {
  
  /** The not started. */
  NOT_STARTED, 
  
  /** The waiting next iteraction. */
  WAITING_NEXT_ITERACTION,
  
  /** The running. */
  RUNNING,
  
  /** The stopping. */
  STOPPING,
  
  /** The stopped. */
  STOPPED,
  /** If the job failed. */
  FAILED;

}
