/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.html.tidy;

import org.jetbrains.annotations.NotNull;
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
   * @param input    the input
   * @param output   the output
   * @param status   the status
   * @param messages the messages
   */
  public TidyFileResultInfo(@NotNull File input,@NotNull File output,@NotNull ResultStatus status,@NotNull List<TidyMessage> messages) {
    super(input, output, status);
    this._messages = messages;
  }

  /**
   * Gets messages.
   *
   * @return the messages
   */
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
