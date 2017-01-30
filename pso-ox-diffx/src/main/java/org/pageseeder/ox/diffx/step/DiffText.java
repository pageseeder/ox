/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.diffx.step;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.diffx.tool.TidyCommand;
import org.pageseeder.ox.diffx.util.DiffXBasic;
import org.pageseeder.ox.step.SimplifyDOCX;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;


/**
 * <p>A Text Comparison processing step</p>
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>source</var> It could be DOCX, HTML or PSML and will be compared with the target</li>
 *  <li><var>target</var> It could be DOCX, HTML or PSML and will be compared by the source. It the target is not send
 *  It gets the input.</li>
 *  <li><var>xsl-psml</var> the specified transformation file for manipulated the psml before diff it. (Optional) </li>
 *  <li><var>xsl-html</var> the specified transformation file for manipulated the html before diff it. (Optional) </li>
 *  <li><var>xsl-docx</var> the specified transformation file for manipulated the docx before diff it. (Optional) </li>
 * </ul>
 *
 *
 * <h3>How it works</h3>
 * <p>In order to identify what kind of file it is. It will use the extension.</p>
 * <p>However for docx, it needs this file simplified. Then before call this class, this step must be called {@link SimplifyDOCX}. 
 * The {@link SimplifyDOCX} will generate the folder simplified, then this folder should be sent instead of the original docx.</p>
 *
 * @author Carlos Cabral
 * @version 12 April 2015
 */
public class DiffText implements Step {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DiffText.class);
  /**
   * We don't try to compare more that 24K events.
   */
  private static final int MAX_EVENTS = 24000;

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Diff Text.");
    String sourcePath = info.getParameter("source");
    String targetPath = info.getParameter("target");
    if (isBlank(targetPath)) {
      targetPath = info.input();
    }
    File source = data.getFile(sourcePath);
    File target = data.getFile(targetPath);

    // Check the source first
    if (!valid(source)) return new InvalidResult(model, data).error(new IllegalArgumentException("The source is invalid"));

    // Then validate the target
    if (!valid(target)) return new InvalidResult(model, data).error(new IllegalArgumentException("The target is invalid"));
    
    
    
    
    //Getting the content(text)

    CheckTextResult result = new CheckTextResult(model, data, sourcePath, targetPath);
    try {
      
      String charset = data.getProperty("charset", "utf-8");
      String sourceText = toText(model, info, source, charset);
      String targetText = toText(model, info, target, charset);
      result.sourceText = sourceText;
      result.targetText = targetText;
      processDiff(result);
      
    } catch (IOException | TransformerException ex) {
      LOGGER.error("DIFF Text error: " + ex.getMessage());
      result.setError(ex);
    }
    
    
    
    
    return result;
  }

  /**
   * Valid.
   *
   * @param file the file
   * @return true, if successful
   */
  private static boolean valid(File file) {
    if (file == null || !file.exists()) {
      LOGGER.warn("Cannot find file {} exist {} .", file);
      return false;
    }
    return true;
  }

  /**
   * Checks if is blank.
   *
   * @param value the value
   * @return true, if is blank
   */
  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  
  /**
   * Gets the template.
   *
   * @param model the model
   * @param info the info
   * @param parameterName the parameter name
   * @param defaultFileName the default file name
   * @return the template
   */
  private Templates getTemplate(Model model, StepInfo info, String parameterName, String defaultFileName) {
    String value = info.getParameter(parameterName);
    Templates template = null;
    try {
      if (!isBlank(value)) {
        template = model.getTemplates(value);
      } else {
        template = model.getTemplates(defaultFileName);
      }
    } catch (IOException | TransformerConfigurationException ex) {
      LOGGER.error("Cannot find the template {} defined in {} parameter. ", value, parameterName, ex);
    }
    return template;
  }
  
  /**
   * Returns the content as plain text.
   * 
   * The docx must be sent simplified (The Simplified Folder).
   *
   * @param model the model
   * @param info the info
   * @param file the file pointing to a HTML, DOCX (Simplified Folder) or PSML.
   * @param charset the charset
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toText(Model model, StepInfo info, File file, String charset) throws IOException, TransformerException {
    String text = null;
    if (file.getName().toLowerCase().endsWith("simplified")) {
      text = toDOCXText(model, info, file);
    } else if (file.getName().toLowerCase().endsWith("html")) {
      text = toHTMLText(model, info, file, charset);
    } else if (file.getName().toLowerCase().endsWith("psml")) {
      text = toPSMLText(model, info, file, charset);
    }
    return text;
  }
  
  /**
   * Returns the PSML as plain text.
   *
   * @param model the model
   * @param info the info
   * @param psml the file pointing to psml document.
   * @param charset the charset
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toPSMLText(Model model, StepInfo info, File psml, String charset) throws IOException, TransformerException {
    LOGGER.debug("Getting text for PSML.");
    String source = FileUtils.readFileToString(psml, charset);

    // Convert to plain text
    StringWriter text = new StringWriter();
    Templates templates = getTemplate(model, info, "xsl-psml", "psml-text.xsl");
    Transformer transformer = templates.newTransformer();
    transformer.transform(new StreamSource(new StringReader(source)), new StreamResult(text));

    return text.toString();
  }

  /**
   * Returns the HTML as plain text.
   *
   * @param model the model
   * @param info the info
   * @param html the file pointing to html document.
   * @param charset the charset
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toHTMLText(Model model, StepInfo info, File html, String charset) throws IOException, TransformerException {
    LOGGER.debug("Getting text for HTML.");
    // We may need to run tidy first
    Tidy tidy = TidyCommand.newTidy(model);
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
    Templates templates = getTemplate(model, info, "xsl-html", "html-text.xsl");
    Transformer transformer = templates.newTransformer();
    transformer.transform(new StreamSource(new StringReader(xhtml)), new StreamResult(text));

    return text.toString();
  }

  /**
   * Returns the main document part as plain text.
   *
   * @param model the model
   * @param info the info
   * @param simplified the simplified
   * @return The file as plain text.
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while tranforming the content.
   */
  private String toDOCXText(Model model, StepInfo info, File simplified) throws IOException, TransformerException {
    LOGGER.debug("Getting text for DOCX.");
    
    //The word file that has the content.
    File documentXML = new File(simplified, "/word/document.xml");

    //Check if the content file exist
    if (documentXML == null || !documentXML.exists()) {
      LOGGER.error("DOCX - document.xml was not found: " + simplified.getAbsolutePath() + "/word/document.xml");
      throw new FileNotFoundException("DOCX - document.xml was not found.");
    }

    //Extract the text.
    StringWriter text = new StringWriter();

    Templates templates = getTemplate(model, info, "xsl-docx", "docx-text.xsl");
    Transformer transformer = templates.newTransformer();
    transformer.transform(new StreamSource(documentXML), new StreamResult(text));
    return text.toString();
  }
  
  
  
  /**
   * Check the differences between result.sourceText and result.targetText and 
   * then update the result.
   *
   * @param result the result
   * @return the check text result
   * @throws IllegalStateException the illegal state exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void processDiff(CheckTextResult result) throws IllegalStateException, IOException{
    
    //Start the comparison logic
    TextTokenizer tokenizer = new TokenizerByWord(WhiteSpaceProcessing.IGNORE);
    List<TextEvent> htmlEvents = tokenizer.tokenize(result.sourceText);
    List<TextEvent> docxEvents = tokenizer.tokenize(result.targetText);

    EventSequence htmlSequence = toSequence(htmlEvents);
    EventSequence docxSequence = toSequence(docxEvents);
    DiffXAlgorithm algorithm = new DiffXBasic(htmlSequence, docxSequence);
    StringWriter diff = new StringWriter();
    TextDiffxFormatter formatter = new TextDiffxFormatter(diff);
    algorithm.process(formatter);
    formatter.checkClose();

    result.diffXML = diff.toString().replaceAll("<\\?xml(.*?)\\>", "");
    result.setStatus(formatter.hasDiff() ? ResultStatus.ERROR : ResultStatus.OK);
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
  
  /* **********************************************************************************************
   * INNER CLASSES
   * **********************************************************************************************/

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
     * The path to the source file within the package.
     */
    private final String _sourcePath;

    /**
     * The path to the target(main) document file within the package.
     */
    private final String _targetPath;
    
    /**
     * Instantiates a new check text result.
     *
     * @param model the model
     * @param data the data
     * @param sourcePath the source path
     * @param targetPath the target path
     */
    public CheckTextResult(Model model, PackageData data, String sourcePath, String targetPath) {      
      super(model, data);
      this._targetPath = targetPath;
      this._sourcePath = sourcePath;
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
      xml.attribute("path", this._sourcePath);
      xml.writeCDATA(this.sourceText);
      xml.closeElement();

      // Target document
      xml.openElement("target");
      xml.attribute("path", this._targetPath);
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

    /* (non-Javadoc)
     * @see org.pageseeder.ox.tool.ResultBase#isDownloadable()
     */
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
