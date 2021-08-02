package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.service.ThreadService;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSThreadStatus;
import org.pageseeder.bridge.xml.PSThreadHandler;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.ox.util.XMLUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The type Group thread progress schedule executor runnable.
 *
 * @author ccabral
 * @since 28 July 2021
 */
public class GroupThreadProgressScheduleExecutorRunnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(GroupThreadProgressScheduleExecutorRunnable.class);
  /**
   * The XML returned
   */
  private final String firstRequestXML;
  private final XMLWriter threadWriter;
  private final PSCredentials credentials;
  private final PSConfig psConfig;
  private final int delayInMilleseconds;

  /**
   *
   * @param firstRequestXML
   * @param threadWriter
   * @param credentials
   * @param psConfig
   * @param delayInMilleseconds
   */
  public GroupThreadProgressScheduleExecutorRunnable(String firstRequestXML, XMLWriter threadWriter,
                                                     PSCredentials credentials, PSConfig psConfig,
                                                     int delayInMilleseconds) {
    this.firstRequestXML = firstRequestXML;
    this.threadWriter = threadWriter;
    this.credentials = credentials;
    this.psConfig = psConfig;
    this.delayInMilleseconds = delayInMilleseconds;
  }

  /**
   * Run.
   */
  public void run() throws IOException {

    try {
      threadWriter.openElement("threads");
      ThreadService threadService = new ThreadService();
      PSThreadHandler handler = new PSThreadHandler();
      XMLUtils.parseXML(firstRequestXML, handler);
      PSThreadStatus status = handler.getThreadStatus();
      // The application will use the word attempt to refer to each attempt to check if the thread is finished.
      int attempts = 0;
      String threadId = null;
      Long groupId = null;
      boolean isFinished = false;

      do {
        //write previous attempt xml
        writeThreadStatusXML(threadWriter, attempts, status);

        //check previous attempt status
        if (status != null && !status.isCompleted()) {
          //Wait before perform next attempt
          //TODO Maybe we should try ScheduledExecutorService
          try {
            Thread.sleep(delayInMilleseconds);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }

          //perform a new attempt
          attempts ++;
          threadId = status.getThreadID();
          groupId = status.getGroupID();
          LOGGER.debug("Thread {} attempt {}", threadId, attempts);
          status = threadService.getGroupThreadProgress(new PSGroup(groupId), threadId, credentials, psConfig);
        } else {
          LOGGER.debug("Is status null: {}", status == null);
          if (status != null) {
            LOGGER.debug("If not null the thread id {} and status {}", threadId, status.getStatus().name());
          }
          isFinished = true;
        }
      } while (!isFinished);//While not finished.
    } finally {
      threadWriter.closeElement();//threads
    }
  }

  /**
   *
   * @param writer
   * @param attempt
   * @param status
   * @throws IOException
   */
  private void writeThreadStatusXML(XMLWriter writer, int attempt, PSThreadStatus status) throws IOException {
    writer.openElement("thread");
    writer.attribute("attempt-number", attempt);
    try {
      if (status != null) {
        //The attribute username will not be shown to not expose the username used.
        writer.attribute("id", status.getThreadID());
        writer.attribute("name", status.getThreadName());
        writer.attribute("groupid", String.valueOf(status.getGroupID()));
        writer.attribute("inprogress", status.getStatus().name().toLowerCase());

        //messages
        for (String message : status.getMessages()) {
          if (StringUtils.isBlank(message)) {
            writer.element("message", message);
          }
        }
      }
    } finally {
      writer.closeElement();//thread
    }
  }
}
