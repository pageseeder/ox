/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.html.tidy;

import java.util.ArrayList;
import java.util.List;

import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;


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
