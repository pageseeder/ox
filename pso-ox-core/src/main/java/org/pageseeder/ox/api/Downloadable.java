/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.api;

import java.io.File;

/**
 * 
 * Implement this interface to {@link Result} if the output intend to download (expose) from public.
 *
 * @author Ciber Cai
 * @since  17 June 2014
 * @deprecated There is not a need for this class as we can check if there an output (Check {@link DefaultResult})
 */
public interface Downloadable {

  /**
   * Download path.
   *
   * @return The downloadable file.
   */
  public File downloadPath();

}
