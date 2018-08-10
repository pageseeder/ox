/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.tool;

import java.io.File;
import java.io.IOException;

import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * The Class FileResultInfo.
 *
 * @author Carlos Cabral
 * @since 08 Aug. 2018
 */
public class FileResultInfo implements XMLWritable{
  
  /** The input. */
  private final File _input;
  
  /** The output. */
  private final File _output;
  
  /** The status. */
  private final ResultStatus _status;

  /**
   * Instantiates a new file result info.
   *
   * @param input the input
   * @param output the output
   * @param status the status
   */
  public FileResultInfo(File input, File output, ResultStatus status) {
    super();
    this._input = input;
    this._output = output;
    this._status = status;
  }
  
  /**
   * Gets the input.
   *
   * @return the input
   */
  public File getInput() {
    return this._input;
  }

  /**
   * Gets the output.
   *
   * @return the output
   */
  public File getOutput() {
    return this._output;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public ResultStatus getStatus() {
    return this._status;
  }

  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result-file");
    xml.attribute("input", this._input.getName());
    xml.attribute("output", this._output.getName());
    xml.attribute("status", this._status.toString());
    xml.closeElement();//result-file
  }
}
