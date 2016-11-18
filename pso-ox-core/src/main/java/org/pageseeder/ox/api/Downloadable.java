/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.api;

import java.io.File;

/**
 * 
 * Implement this interface to {@link Result} if the output intend to download (expose) from public.
 *
 * @author Ciber Cai
 * @since  17 June 2014
 */
public interface Downloadable {

  /**
   * @return The downloadable file.
   */
  public File downloadPath();

}
