/* Copyright (c) 1999-2014 weborganic systems pty. ltd. */
package org.pageseeder.ox.diffx.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.pageseeder.diffx.algorithm.DiffXAlgorithm;
import org.pageseeder.diffx.config.DiffXConfig;
import org.pageseeder.diffx.config.WhiteSpaceProcessing;
import org.pageseeder.diffx.event.DiffXEvent;
import org.pageseeder.diffx.event.TextEvent;
import org.pageseeder.diffx.format.DiffXFormatter;
import org.pageseeder.diffx.load.text.TextTokenizer;
import org.pageseeder.diffx.load.text.TokenizerByWord;
import org.pageseeder.diffx.sequence.EventSequence;
import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.diffx.util.DiffXBasic;
import org.pageseeder.ox.tool.Command;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;

/**
 * Compare the text of 2 files. They could be DOCX, HTML or PSML.
 *
 * <h3>How it works</h3>
 * <p>In order to identify what kind of file it is. It will use the extension.
 * <p>However when the docx is simplified it will be saved in the folder simplified, therefore every time when it
 * receives the folder simplified it will treat as docx.
 * <p> To send the DOCX you can send the whole file docx or the simplified folder created by the {@link Simplify} service.
 *
 * @author Christophe Lauret
 * @version 28 October 2013
 */
public final class DiffTextCommand implements Command<Result> {

  /**  Logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(DiffTextCommand.class);
  /**
   * We don't try to compare more that 24K events.
   */
  private static final int MAX_EVENTS = 24000;

  /**
   * The model used by this command.
   */
  private final Model _model;

  /**
   * The path to the source file within the package.
   */
  private final String _sourcePath;

  /**
   * The path to the target(main) document file within the package.
   */
  private final String _targetPath;

  private Templates psml;

  private Templates html;

  private Templates docx;

  /**
   * Create a new command for this model.
   *
   * @param model the model
   * @param sourcePath the sourcePath file within the package.
   * @param targetPath the targetPath to the target (Main) document file within the package.
   */
  public DiffTextCommand(Model model, String sourcePath, String targetPath) {
    this._model = model;
    this._sourcePath = sourcePath;
    this._targetPath = targetPath;
  }

  /**
   * @param templates the template for transformation the psml content.
   */
  public void setPSMLTransformation(Templates templates) {
    this.psml = templates;
  }

  /**
   * @param templates the template for transformation the html content.
   */
  public void setHTMLTransformation(Templates templates) {
    this.html = templates;
  }

  /**
   * @param templates the template for transformation the docx content.
   */
  public void setDOCXTransformation(Templates templates) {
    this.docx = templates;
  }

  @Override
  public Model getModel() {
    return this._model;
  }

  @Override
  public CheckTextResult process(PackageData data) {
    CheckTextResult result = new CheckTextResult(data);

    try {
      String charset = data.getProperty("charset", "utf-8");

      //Source
      File source = getSource(this._model, data);
      String sourceText = toText(source, charset);

      //Target
      File target = getTarget(this._model, data);
      String targetText = toText(target, charset);

      //Start the comparison logic
      TextTokenizer tokenizer = new TokenizerByWord(WhiteSpaceProcessing.IGNORE);
      List<TextEvent> htmlEvents = tokenizer.tokenize(sourceText);
      List<TextEvent> docxEvents = tokenizer.tokenize(targetText);

      EventSequence htmlSequence = toSequence(htmlEvents);
      EventSequence docxSequence = toSequence(docxEvents);
      DiffXAlgorithm algorithm = new DiffXBasic(htmlSequence, docxSequence);
      StringWriter diff = new StringWriter();
      TextDiffxFormatter formatter = new TextDiffxFormatter(diff);
      algorithm.process(formatter);
      formatter.checkClose();

      result.sourceText = sourceText;
      result.targetText = targetText;
      result.diffXML = diff.toString().replaceAll("<\\?xml(.*?)\\>", "");
      result.setStatus(formatter.hasDiff() ? ResultStatus.ERROR : ResultStatus.OK);

      result.done();

    } catch (Exception ex) {
      result.setError(ex);
    }

    return result;
  }

  /**
   * To sequence.
   *
   * @param events the events
   * @return the event sequence
   */
  private EventSequence toSequence(List<TextEvent> events) {
    int to = Math.min(events.size(), MAX_EVENTS);
    EventSequence sequence = new EventSequence(events.size());
    for (int i = 0; i < to; i++) {
      sequence.addEvent(events.get(i));
    }
    return sequence;
  }

  /**
   * Returns the content as plain text.
   *
   * The docx must be sent simplified (The Simplified Folder).
   *
   *
   * @param file the file pointing to a HTML, DOCX (Simplified Folder) or PSML.
   * @param charset the charset
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toText(File file, String charset) throws IOException, TransformerException {
    String text = null;
    if (file.getName().toLowerCase().endsWith("simplified")) {
      text = toDOCXText(file);
    } else if (file.getName().toLowerCase().endsWith("html")) {
      text = toHTMLText(file, charset);
    } else if (file.getName().toLowerCase().endsWith("psml")) {
      text = toPSMLText(file, charset);
    }
    return text;
  }

  /**
   * Returns the PSML as plain text.
   *
   * @param psml the file pointing to psml document.
   * @param charset the charset
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toPSMLText(File psml, String charset) throws IOException, TransformerException {

    String source = FileUtils.readFileToString(psml, charset);

    // Convert to plain text
    StringWriter text = new StringWriter();
    Templates templates = this.psml != null ? this.psml : this._model.getTemplates("psml-text.xsl");
    Transformer transformer = templates.newTransformer();
    transformer.transform(new StreamSource(new StringReader(source)), new StreamResult(text));

    return text.toString();
  }

  /**
   * Returns the HTML as plain text.
   *
   * @param html the file pointing to html document.
   * @param charset the charset
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toHTMLText(File html, String charset) throws IOException, TransformerException {
    // We may need to run tidy first
    Tidy tidy = TidyCommand.newTidy(this._model);
    // tidy.setOnlyErrors(true);
    // tidy.setShowErrors(0);
    StringWriter buffer = new StringWriter();
    String source = FileUtils.readFileToString(html, charset);
    source = source.replaceAll("<\\?xml(.*?)\\>", "");
    tidy.parse(new StringReader(source), buffer);
    String xhtml = buffer.toString();

    // We must remove the XHTML DOCTYPE declaration since W3 has shutdown its servers for XHTML DTDs
    xhtml = xhtml.replaceAll("<\\!DOCTYPE(.*?)\\>\n?", "");

    // Convert to plain text
    StringWriter text = new StringWriter();
    Templates templates = this.html != null ? this.html : this._model.getTemplates("html-text.xsl");
    Transformer transformer = templates.newTransformer();
    transformer.transform(new StreamSource(new StringReader(xhtml)), new StreamResult(text));

    return text.toString();
  }

  /**
   * Returns the main document part as plain text.
   *
   * @param docx the file pointing to the main document part
   * @return The file as plain text.
   *
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toDOCXText(File simplified) throws IOException, TransformerException {
    //The word file that has the content.
    File documentXML = new File(simplified, "/word/document.xml");

    //Check if the content file exist
    if (documentXML == null || !documentXML.exists()) {
      LOGGER.error("DOCX - document.xml was not found: " + simplified.getAbsolutePath() + "/word/document.xml");
      throw new FileNotFoundException("DOCX - document.xml was not found.");
    }

    //Extract the text.
    StringWriter text = new StringWriter();
    Templates templates = this.docx != null ? this.docx : this._model.getTemplates("docx-text.xsl");
    Transformer transformer = templates.newTransformer();
    transformer.transform(new StreamSource(documentXML), new StreamResult(text));
    return text.toString();
  }

  /**
   * Get the source file.
   * In case it is a docx, it calls the Simplify command to extract it.
   *
   * @param model the model
   * @param data the data
   * @return the source
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private File getSource(Model model, PackageData data) throws IOException {
    return getFile(model, data, true);
  }

  /**
   * Get the target file.
   * In case it is a docx, it calls the Simplify command to extract it.
   *
   * @param model the model
   * @param data the data
   * @return the target
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private File getTarget(Model model, PackageData data) throws IOException {
    return getFile(model, data, false);
  }

  /**
   * Get the source or the target file.
   * In case it is a docx, it calls the Simplify command to extract it.
   *
   * @param model the model
   * @param data the data
   * @param isSource the is source
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private File getFile(Model model, PackageData data, boolean isSource) throws IOException {
    String path = null;
    String parentFolderName = null;

    if (isSource) {
      path = this._sourcePath;
      parentFolderName = "source";
    } else {
      path = this._targetPath;
      parentFolderName = "target";
    }

    File file = data.getFile(path);

    // TODO Move commented code below to `pso-docx-ox` module
    /*
    if (path.toLowerCase().endsWith("docx")) {
      //If it is docx, it need to be unpacked.
      file = unpackDocx(model, data, parentFolderName, file);
    }
    */

    return file;
  }


  // TODO Move commented code below to `pso-docx-ox` module

  /*
   * Unpack the docx in the destination folder + /simplified.
   * The parameter destination folder exist, because the source and the target are docx, then it uses the destination
   * folder to differentiate.
   *
   * @param model the model
   * @param data the data
   * @param parentFolderName the parent folder name
   * @param docx the docx
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
/*
  private File unpackDocx(Model model, PackageData data, String parentFolderName, File docx) throws IOException {

    //Create destination folders
    File destination = data.getFile(parentFolderName);
    destination.mkdirs();

    //Create the Simplified folder
    File simplified = new File(destination, "simplified");
    simplified.mkdir();

    //Unzip the docx
    ZipUtils.unzip(docx, new File(destination, "unpacked"));

    //Simplified
    SimplifyDOCXCommand command = new SimplifyDOCXCommand(model);
    command.setSourceDir(parentFolderName + File.separator + "unpacked");
    command.setTargetDir(parentFolderName + File.separator + "simplified");
    Result result = command.process(data);

    //Check the result
    simplified = null;
    if (result.error() == null) {
      simplified = new File(destination, "simplified");
      if (simplified == null || !simplified.exists()) {
        LOGGER.error("Simplified was not found: " + destination.getAbsolutePath() + File.separator + "simplified");
        throw new FileNotFoundException("Simplified was not found: " + destination.getAbsolutePath() + File.separator + "simplified");
      }
    } else {
      LOGGER.error("Error while simplifing the docx: " + result.error().getMessage());
      throw new FileNotFoundException("Error while simplifing the docx: " + result.error().getMessage());
    }
    return simplified;
  }
 */

  // Results for this command
  // ----------------------------------------------------------------------------------------------

  /**
   * The Class CheckTextResult.
   *
   * @author Christophe Lauret
   * @version 28 October 2013
   */
  private final class CheckTextResult extends ResultBase implements Result {

    /** The source text. */
    private String sourceText = "";

    /** The target text. */
    private String targetText = "";

    /** The diff xml. */
    private String diffXML = "";

    /**
     * Instantiates a new check text result.
     *
     * @param data the data
     */
    public CheckTextResult(PackageData data) {
      super(DiffTextCommand.this._model, data);
    }

    /* (non-Javadoc)
     * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
     */
    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result", true);
      xml.attribute("name", "diff-text");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      // Source document
      xml.openElement("source");
      xml.attribute("path", DiffTextCommand.this._sourcePath);
      xml.writeCDATA(this.sourceText);
      xml.closeElement();

      // Target document
      xml.openElement("target");
      xml.attribute("path", DiffTextCommand.this._targetPath);
      xml.writeCDATA(this.targetText);
      xml.closeElement();

      // Settings specified
      xml.openElement("diff");
      xml.writeXML(this.diffXML);
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

  /**
   * The Class TextDiffxFormatter.
   */
  public static class TextDiffxFormatter implements DiffXFormatter {

    /**  Diff output. */
    final Writer w;

    /** The state of the previous event  (-1 = delete, 0 = format, 1 = insert). */
    private int last = 0;

    /**
     * State variable set to <code>true</code> if a difference is detected.
     */
    private boolean hasDiff = false;

    /**
     * Instantiates a new text diffx formatter.
     *
     * @param w the w
     */
    public TextDiffxFormatter(Writer w) {
      this.w = w;
    }

    /* (non-Javadoc)
     * @see com.topologi.diffx.format.DiffXFormatter#setConfig(com.topologi.diffx.config.DiffXConfig)
     */
    @Override
    public void setConfig(DiffXConfig config) {}

    /* (non-Javadoc)
     * @see com.topologi.diffx.format.DiffXFormatter#format(com.topologi.diffx.event.DiffXEvent)
     */
    @Override
    public void format(DiffXEvent e) throws IOException, IllegalStateException {
      checkClose();
      this.w.write(e.toXML());
      this.w.write(' ');
      this.last = 0;
    }

    /* (non-Javadoc)
     * @see com.topologi.diffx.format.DiffXFormatter#delete(com.topologi.diffx.event.DiffXEvent)
     */
    @Override
    public void delete(DiffXEvent e) throws IOException, IllegalStateException {
      if (this.last > 0) {
        this.w.write("</ins>");
      }
      if (this.last != -1) {
        this.w.write("<del>");
      }
      this.w.write(e.toXML());
      this.w.write(' ');
      this.last = -1;
      this.hasDiff = true;
    }

    /* (non-Javadoc)
     * @see com.topologi.diffx.format.DiffXFormatter#insert(com.topologi.diffx.event.DiffXEvent)
     */
    @Override
    public void insert(DiffXEvent e) throws IOException, IllegalStateException {
      if (this.last < 0) {
        this.w.write("</del>");
      }
      if (this.last != 1) {
        this.w.write("<ins>");
      }
      this.w.write(e.toXML());
      this.w.write(' ');
      this.last = 1;
      this.hasDiff = true;
    }

    /**
     * Check close.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws IllegalStateException the illegal state exception
     */
    public void checkClose() throws IOException, IllegalStateException {
      if (this.last > 0) {
        this.w.write("</ins>");
      }
      if (this.last < 0) {
        this.w.write("</del>");
      }
    }

    /**
     * Checks for diff.
     *
     * @return the hasDiff
     */
    public boolean hasDiff() {
      return this.hasDiff;
    }
  }
}
