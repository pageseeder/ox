package org.pageseeder.ox.diffx.tool;

/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.tool.Command;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessage.Level;
import org.w3c.tidy.TidyMessageListener;

/**
 * @author Christophe Lauret
 * @version 8 November 2013
 */
public class TidyCommand implements Command<Result> {

  /** To completely ignore print messages (captured via callback messages anyway) */
  private final static PrintWriter VOID_PRINTER = new PrintWriter(new VoidWriter());

  /**
   * The model used by this command.
   */
  private final Model _model;

  /**
   * Path to the HTML to tidy
   */
  private String htmlPath = null;

  /**
   * The public download folder.
   */
  private File download = null;

  /**
   * Create a new tidy command.
   *
   * @param model The model to use
   */
  public TidyCommand(Model model) {
    this._model = model;
  }

  /**
   * @param htmlPath the htmlPath to set
   */
  public void setHtmlPath(String htmlPath) {
    this.htmlPath = htmlPath;
  }

  /**
   * @param download The public download folder.
   */
  public void setDownload(File download) {
    this.download = download;
  }

  /**
   * Tidy on the HTML so that it can be converted to PSML with XSLT.
   *
   * <p>Implementation notes:
   * <ul>
   *   <li>This implementation uses jTidy.</li>
   *   <li>tidy.setEncloseText(true); although it was used to enclose text at the body level in, it was
   *   removed because it splits mixed body content</li>
   * </ul>
   *
   * @param data The HTML
   *
   * @return The tidied content
   */
  @Override
  public TidyResult process(PackageData data) {
    TidyResult result = new TidyResult(data);

    File document = data.getFile(this.htmlPath);

    try {
      // Set new Tidy
      Tidy tidy = newTidy(this._model);
      tidy.setMessageListener(result);

      // so we strip it out as the transformer doesn't recognise it
      String charset = data.getProperty("charset", "utf-8");
      String html = FileUtils.readFileToString(document, charset);
      String source = html.replaceAll("<\\?xml(.*?)\\>", "");

      // Let's go
      StringWriter buffer = new StringWriter();
      tidy.parse(new StringReader(source), buffer);
      String xhtml = buffer.toString();

      // We must remove the XHTML DOCTYPE declaration since W3 has shutdown its servers for XHTML DTDs
      result.setXHTML(xhtml.replaceAll("<\\!DOCTYPE(.*?)\\>\n?", ""));

      // Save the file
      File internal = data.getFile("source.xhtml");
      FileUtils.write(internal, result.getXHTML(), "utf-8");

      // Make it downloadable
      if (this.download != null) {
        File sub = data.getDownloadDir(this.download);
        String name = data.getProperty("name", data.id());
        File downloadable = new File(sub, name + ".xhtml");
        FileUtils.write(downloadable, result.getXHTML(), "utf-8");
        downloadable.deleteOnExit();
        result.downloadable = downloadable;
      }

      // Stop the timer
      result.done();

    } catch (IOException ex) {
      result.setError(ex);
    }
    return result;
  }

  /**
   * Return a new tidy configuration for the model
   *
   * @param model
   * @return the Tidy
   */
  public static Tidy newTidy(Model model) {
    Tidy tidy = new Tidy();
    tidy.setXHTML(true);
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
    tidy.setWraplen(0);
    tidy.setNumEntities(true);
    tidy.setXmlOut(true);
    tidy.setOutputEncoding("utf-8");
    tidy.setDocType(null);
    tidy.setTidyMark(false);
    tidy.setErrout(VOID_PRINTER);

    // Try to load config from model
    Properties p = model.getProperties("tidy.properties");
    if (p != null) {
      tidy.setConfigurationFromProps(p);
    }
    return tidy;
  }

  @Override
  public Model getModel() {
    return this._model;
  }

  private final class TidyResult extends ResultBase implements TidyMessageListener, XMLWritable {

    /**
     * The generated XHTML data
     */
    private String xhtml = null;

    /**
     * The list of tidy messages captured during processing.
     */
    private final List<TidyMessage> messages = new ArrayList<TidyMessage>();

    private File downloadable = null;

    /**
     *
     */
    public TidyResult(PackageData data) {
      super(TidyCommand.this._model, data);
    }

    @Override
    public void messageReceived(TidyMessage message) {
      this.messages.add(message);
      if (message.getLevel() == Level.WARNING && status() != ResultStatus.ERROR) {
        setStatus(ResultStatus.WARNING);
      }
      if (message.getLevel() == Level.ERROR) {
        setStatus(ResultStatus.ERROR);
      }
    }

    /**
     * @return the xhtml
     */
    public String getXHTML() {
      return this.xhtml;
    }

    /**
     * @param xhtml the xhtml to set
     */
    public void setXHTML(String xhtml) {
      this.xhtml = xhtml;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("type", "tidy");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      // Check if downloadable
      if (this.downloadable != null) {
        xml.openElement("download");
        xml.attribute("href", "/download/" + data().id() + "/" + this.downloadable.getName());
        xml.closeElement();
      }

      xml.openElement("content");
      xml.writeXML(this.xhtml);
      xml.closeElement();
      xml.openElement("raw-content");
      xml.writeCDATA(this.xhtml);
      xml.closeElement();

      // Any message
      xml.openElement("messages");
      for (TidyMessage m : this.messages) {
        xml.openElement("message");
        xml.attribute("level", m.getLevel().toString());
        xml.attribute("line", m.getLine());
        xml.attribute("column", m.getColumn());
        xml.attribute("code", m.getErrorCode());
        xml.writeText(m.getMessage());
        xml.closeElement();
      }
      xml.closeElement();

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      }

      xml.closeElement();
    }

    @Override
    public boolean isDownloadable() {
      return false;
    }

  }

  private static final class VoidWriter extends Writer {

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {}

    @Override
    public void flush() throws IOException {}

    @Override
    public void close() throws IOException {}

  }
}
