/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.html.tidy;

import org.eclipse.jdt.annotation.NonNull;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.tool.FileResultInfo;
import org.pageseeder.xmlwriter.XMLWriter;
import org.w3c.tidy.TidyMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The Class FileResultInfo.
 *
 * @author Carlos Cabral
 * @since 08 Aug. 2018
 */
public class TidyFileResultInfo extends FileResultInfo {

  private final List<TidyMessage> _messages;

  /**
   * Instantiates a new file result info.
   *
   * @param input the input
   * @param output the output
   * @param status the status
   */
  public TidyFileResultInfo(@NonNull File input,@NonNull File output,@NonNull ResultStatus status,@NonNull List<TidyMessage> messages) {
    super(input, output, status);
    this._messages = messages;
  }

  public List<TidyMessage> getMessages() {
    return this._messages;
  }

  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result-file");
    xml.attribute("input", super.getInput().getName());
    xml.attribute("output", super.getOutput().getName());
    xml.attribute("status", super.getStatus().toString());
    xml.openElement("messages");
    for (TidyMessage message:this._messages) {
      xml.openElement("message");
      xml.attribute("text", message.getMessage());
      xml.attribute("line", message.getLine());
      xml.attribute("column", message.getColumn());
      xml.attribute("error-code", message.getErrorCode());
      xml.attribute("level", message.getLevel().toString());
      xml.closeElement();
    }
    xml.closeElement();//messages
    xml.closeElement();//result-file
  }
}
