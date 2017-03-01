/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.api;

/**
 * 
 * Implement this interface to {@link Step} if the custom are able to return the percentage of its process.
 *
 * @author Carlos Cabral
 * @since  17 June 2014
 */
public interface Measurable {

  /**
   * If the value returned is -1, it means unknown.
   * 
   * @return The percentage completed in a process/step/etc.
   */
  public int percentage();

}
