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

import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;

import java.util.ArrayList;
import java.util.List;


/**
 * The listener interface for receiving tidyOXMessage events.
 * The class that is interested in processing a tidyOXMessage
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTidyOXMessageListener<code> method. When
 * the tidyOXMessage event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Carlos Cabral
 * @since 17 Aug. 2018
 */
public class TidyOXMessageListener  implements TidyMessageListener{

  /**
   * The list of tidy messages captured during processing.
   */
  private final List<TidyMessage> _messages = new ArrayList<TidyMessage>();

  /* (non-Javadoc)
   * @see org.w3c.tidy.TidyMessageListener#messageReceived(org.w3c.tidy.TidyMessage)
   */
  @Override
  public void messageReceived(TidyMessage message) {
    this._messages.add(message);
  }

  /**
   * Gets the messages.
   *
   * @return the messages
   */
  public List<TidyMessage> getMessages() {
    return this._messages;
  }
}
