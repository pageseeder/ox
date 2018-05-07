/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.model;

import java.io.Serializable;

/**
 *
 * @author Carlos Cabral
 * @since  13 April 2018
 */
public final class JobResponse implements Serializable {

  /** the serial version */
  private static final long serialVersionUID = -3028660547170793478L;

  /**
   * The job id
   */
  private final String _id;

  /**
   * Time the job was started
   */
  private final String _startTime;

  /**
   * The status of job
   */
  private final String _status;

  /**
   * The job input file name
   */
  private final String _input;


  /**
   * The Job processing mode
   */
  private final String _mode;
   
  public JobResponse(String id, String startTime, String status, String input, String mode) {
    super();
    this._id = id;
    this._startTime = startTime;
    this._status = status;
    this._input = input;
    this._mode = mode;
  }

  public String getId() {
    return _id;
  }

  public String getStartTime() {
    return _startTime;
  }

  public String getStatus() {
    return _status;
  }

  public String getInput() {
    return _input;
  }

  public String getMode() {
    return _mode;
  }}
