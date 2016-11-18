/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.tool;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;

/**
 * An abstract implementation of a command result
 *
 * @author Christophe Lauret
 * @since  28 October 2013
 */
public abstract class ResultBase implements Result {

  private final Model _model;

  private final PackageData _data;

  private final long _started;

  private long time = -1;

  private ResultStatus status = ResultStatus.OK;

  private Exception error = null;

  /**
   * @param model The {@link Model}
   * @param data the {@link Package}
   */
  protected ResultBase(Model model, PackageData data) {
    this._model = model;
    this._data = data;
    this._started = System.nanoTime();
  }

  /**
   * @return the {@link Model}
   */
  public final Model model() {
    return this._model;
  }

  /**
   * @return {@link PackageData}
   */
  public final PackageData data() {
    return this._data;
  }

  @Override
  public final long time() {
    if (this.time < 0) {
      done();
    }
    return this.time;
  }

  @Override
  public final ResultStatus status() {
    return this.status;
  }

  @Override
  public Exception error() {
    return this.error;
  }

  /**
   * Invoke this method when the result are ready to compute the time consistently.
   */
  public final void done() {
    // Stores the time in microseconds
    this.time = (System.nanoTime() - this._started) / 1000000;
  }

  /**
   * @param status the status to set
   */
  public final void setStatus(ResultStatus status) {
    this.status = status;
  }

  /**
   * @param ex the exception causing the error.
   */
  public final void setError(Exception ex) {
    this.error = ex;
    this.status = ResultStatus.ERROR;
    done();
  }

  public abstract boolean isDownloadable();

}
