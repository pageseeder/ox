/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.html.tidy;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Carlos Cabral
 * @since 17 Aug. 2018
 */
public final class TidyVoidWriter extends Writer {

  /* (non-Javadoc)
   * @see java.io.Writer#write(char[], int, int)
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {}

  /* (non-Javadoc)
   * @see java.io.Writer#flush()
   */
  @Override
  public void flush() throws IOException {}

  /* (non-Javadoc)
   * @see java.io.Writer#close()
   */
  @Override
  public void close() throws IOException {}

}
