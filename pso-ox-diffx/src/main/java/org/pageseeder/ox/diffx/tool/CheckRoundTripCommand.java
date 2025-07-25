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
package org.pageseeder.ox.diffx.tool;

import org.apache.commons.io.FileUtils;
import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.diffx.step.CheckRoundTrip;
import org.pageseeder.ox.diffx.util.Clones;
import org.pageseeder.ox.tool.Command;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.CharsetDetector;
import org.pageseeder.ox.util.ZipUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The type Check round trip command.
 *
 * @author Christophe Lauret
 * @version 1 November 2013
 */
public final class CheckRoundTripCommand implements Command<Result> {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(CheckRoundTrip.class);

  /**
   * The model used by this command.
   */
  private final Model _model;

  /**
   * The path to the HTML file within the package.
   */
  private final Map<String, Templates> templates = new HashMap<>(2);

  /**
   * The path to the source file within the package.
   */
  private String source;

  /**
   * The path to the DOCX document to use as a base.
   */
  private String base = "base.docx";

  /**
   * The public download folder.
   */
  private File download = null;

  /**
   * Create a new command for this model.
   *
   * @param model the model
   */
  public CheckRoundTripCommand(Model model) {
    this._model = model;
  }

  @Override
  public Model getModel() {
    return this._model;
  }

  /**
   * Sets base docx.
   *
   * @param path The path to base docx to use
   */
  public void setBaseDOCX(String path) {
    this.base = path;
  }

  /**
   * Sets source.
   *
   * @param path the path to the source file within the package.
   */
  public void setSource(String path) {
    this.source = path;
  }

  /**
   * Sets download.
   *
   * @param download The public download folder.
   */
  public void setDownload(File download) {
    this.download = download;
  }

  @Override
  public Result process(PackageData data) {
    CheckRoundTripResult result = new CheckRoundTripResult(this._model, data);
    File folder = data.getFile("roundtrip");
    Map<String, String> parameters = data.getParameters();
    folder.mkdir();

    try {

      Round round1 = null;
      Round round2 = null;

      if (this.source.toLowerCase().endsWith("html") || this.source.toLowerCase().endsWith("htm")) {
        File html = data.getFile(this.source);
        File xhtml = new File(folder, html.getName().substring(0, html.getName().lastIndexOf('.')) + ".xhtml");
        File base = this._model.getFile(this.base);
        String charset = data.getProperty("charset", "utf-8");
        tidy(html, charset, xhtml);

        // Do the round-tripping twice
        result.round1 = round1 = toDOCX(xhtml, base, folder, parameters, 1);
        result.round2 = round2 = toDOCX(round1.b.file, base, folder, parameters, 2);

      } else if (this.source.toLowerCase().endsWith("xml")) {

        File docx = data.getFile(this.source);
        result.round1 = round1 = toHTML(docx, folder, parameters, 1);
        result.round2 = round2 = toHTML(round1.b.file, folder, parameters, 2);

      }

      // Checks
      if (round1.a.checksum() != round2.a.checksum()) {
        round1.a.countElements();
        round2.a.countElements();
        boolean similarCount = round2.a.counter.same(round1.a.counter);
        result.setStatus(similarCount ? ResultStatus.WARNING : ResultStatus.ERROR);
        if (this.download != null) {
          round1.a.save(this.download, data);
          round2.a.save(this.download, data);
        }
      }
      if (round1.b.checksum() != round2.b.checksum()) {
        round1.b.countElements();
        round2.b.countElements();
        boolean similarCount = round2.b.counter.same(round1.b.counter);
        result.setStatus(similarCount && result.status() != ResultStatus.ERROR ? ResultStatus.WARNING : ResultStatus.ERROR);
        if (this.download != null) {
          round1.b.save(this.download, data);
          round2.b.save(this.download, data);
        }
      }

      result.done();

    } catch (Exception ex) {
      LOGGER.error("Exception: " + ex.getMessage(), ex);
      result.setError(ex);
    }

    return result;
  }

  private Round toDOCX(File xhtml, File base, File folder, Map<String, String> parameters, int round) throws IOException, TransformerException {
    // Create subfolder to store data for this round
    File f = new File(folder, Integer.toString(round));
    f.mkdir();

    // Unpack the base
    File base1 = new File(f, "base");
    base1.mkdir();
    ZipUtils.unzip(base, base1);

    // Updated DOCX
    File docx = new File(f, "base/word/document.xml");
    RoundTripData a = new RoundTripData(docx, "docx", round);
    Clones.clone(base1);
    transform(xhtml, docx, "to-docx.xsl", parameters);
    Clones.clean(base1);

    // Generate the HTML
    File html = new File(f, "html.html");
    transform(docx, html, "to-html.xsl", parameters);

    // Tidy to XHTML
    File xhtmlout = new File(f, "html.xhtml");
    RoundTripData b = new RoundTripData(xhtmlout, "xhtml", round);
    Charset charset = CharsetDetector.getFromContent(html);
    tidy(html, charset.name(), xhtmlout);

    return new Round(a, b, round);
  }

  private Round toHTML(File docx, File folder, Map<String, String> parameters, int round) throws IOException, TransformerException {
    // Create subfolder to store data for this round
    File f = new File(folder, Integer.toString(round));
    f.mkdir();

    // Generate HTML
    File html = new File(f, "html.html");
    transform(docx, html, "to-html.xsl", parameters);

    // Tidy to XHTML
    File xhtml = new File(f, "html.xhtml");
    RoundTripData a = new RoundTripData(xhtml, "html", round);
    Charset charset = CharsetDetector.getFromContent(html);
    tidy(html, charset.name(), xhtml);

    // Prepare DOCX Folder
    File base = docx;
    if ("document.xml".equals(docx.getName())) {
      base = docx.getParentFile().getParentFile();
    }
    File baseout = new File(f, "base");
    baseout.mkdir();
    FileUtils.copyDirectory(base, baseout);

    // Generate DOCX
    File docxout = new File(baseout, "word/document.xml");
    RoundTripData b = new RoundTripData(docxout, "docx", round);
    Clones.clone(baseout);
    transform(xhtml, docxout, "to-docx.xsl", parameters);
    Clones.clean(baseout);

    return new Round(a, b, round);
  }

  /**
   *
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while transforming the content.
   */
  private void tidy(File html, String charset, File xhtml) throws IOException, TransformerException {
    // We may need to run tidy first
    Tidy tidy = TidyCommand.newTidy(this._model);
    tidy.setShowWarnings(false);
    tidy.setQuiet(true);
    Writer buffer = new StringWriter();

    String source = FileUtils.readFileToString(html, charset);
    source = source.replaceAll("<\\?xml(.*?)\\>", "");
    tidy.parse(new StringReader(source), buffer);

    // We must remove the XHTML DOCTYPE declaration since W3 has shutdown its servers for XHTML DTDs
    String data = buffer.toString().replaceAll("<\\!DOCTYPE(.*?)\\>\n?", "");
    FileUtils.write(xhtml, data, "utf-8");
  }

  /**
   *
   * @throws IOException Should an error occur while reading the file.
   * @throws TransformerException Should an error occur while transforming the content.
   */
  private void transform(File docx, File html, String xsltName, Map<String, String> parameters) throws IOException, TransformerException {
    //FIXME get the template from the project path.
    Templates templates = this.templates.get(xsltName);
    if (templates == null) {
      // Get the templates
      templates = this._model.getTemplates(xsltName);
      if (templates == null) throw new FileNotFoundException(xsltName);
      this.templates.put(xsltName, templates);
    }
    Transformer transformer = templates.newTransformer();
    // Add the parameters
    for (Entry<String, String> p : parameters.entrySet()) {
      transformer.setParameter(p.getKey(), p.getValue());
    }
    transformer.transform(new StreamSource(docx), new StreamResult(html));
  }

  /**
   * Gets the extension.
   *
   * @param file the file
   * @return the extension
   */
  private static String getExtension(File file) {
    String fileName = file.getName();
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) return fileName.substring(fileName.lastIndexOf(".") + 1);
    else return "";
  }

  // Results for this command
  // ----------------------------------------------------------------------------------------------

  /**
   *
   * @author Christophe Lauret
   * @version 28 October 2013
   */
  private static final class CheckRoundTripResult extends ResultBase implements Result {

    private Round round1 = null;

    private Round round2 = null;

    /**
     * Instantiates a new Check round trip result.
     *
     * @param model the model
     * @param data  the data
     */
    public CheckRoundTripResult(Model model, PackageData data) {
      super(model, data);
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result", true);
      xml.attribute("type", "check-roundtrip");//Kept because of old system that still use this (14 April 2016).
      xml.attribute("name", "check-roundtrip");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      if (this.round1 != null) {
        xml.attribute("type-a", getExtension(this.round1.a.file));
        xml.attribute("type-b", getExtension(this.round1.b.file));
        this.round1.toXML(xml);
      }
      if (this.round2 != null) {
        this.round2.toXML(xml);
      }
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

  // Objects handled by this class
  // ----------------------------------------------------------------------------------------------

  /**
   *
   * @author Christophe Lauret
   * @version 28 October 2013
   */
  private final class Round implements XMLWritable {

    private final RoundTripData a;

    private final RoundTripData b;

    private final int round;

    /**
     * Instantiates a new Round.
     *
     * @param a     the a
     * @param b     the b
     * @param round the round
     */
    public Round(RoundTripData a, RoundTripData b, int round) {
      this.a = a;
      this.b = b;
      this.round = round;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      if (this.a != null) {
        this.a.toXML(xml);
      }
      if (this.b != null) {
        this.b.toXML(xml);
      }
    }

  }

  /**
   *
   * @author Christophe Lauret
   * @version 28 October 2013
   */
  private final class RoundTripData implements XMLWritable {

    private final File file;

    private final String type;

    private final int round;

    private long checksum = 0;

    private ElementCounter counter;

    private File copy = null;

    /**
     * Instantiates a new Round trip data.
     *
     * @param f     the f
     * @param type  the type
     * @param round the round
     */
    public RoundTripData(File f, String type, int round) {
      this.file = f;
      this.type = type;
      this.round = round;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("file");
      xml.attribute("name", this.file.getName());
      xml.attribute("type", this.type);
      xml.attribute("round", this.round);
      if (this.file.exists()) {
        xml.attribute("size", Long.toString(this.file.length()));
      }
      xml.attribute("checksum", Long.toString(checksum(), 16));
      if (this.counter != null) {
        this.counter.toXML(xml);
      }
      if (this.copy != null) {
        xml.openElement("download");
        xml.attribute("href", "/download/" + this.copy.getParentFile().getName() + "/" + this.copy.getName());
        xml.closeElement();
      }
      xml.closeElement();
    }

    /**
     * Checksum long.
     *
     * @return the long
     * @throws IOException the io exception
     */
    public long checksum() throws IOException {
      if (this.checksum == 0) {
        if (this.file.exists()) {
          this.checksum = FileUtils.checksumCRC32(this.file);
        }
      }
      return this.checksum;
    }

    /**
     * Save.
     *
     * @param download the download
     * @param pack     the pack
     * @throws IOException the io exception
     */
    public void save(File download, PackageData pack) throws IOException {
      File sub = pack.getDownloadDir(download);
      String name = pack.getProperty("name", pack.id());
      if ("html".equals(this.type)) {
        File to = new File(sub, name + "-roundtrip-" + this.round + ".xhtml");
        FileUtils.copyFile(this.file, to);
        to.deleteOnExit();
        this.copy = to;
      } else if ("docx".equals(this.type)) {
        File to = new File(sub, name + "-roundtrip-" + this.round + ".docx");
        File dir = this.file.getParentFile().getParentFile();
        ZipUtils.zip(dir, to);
        to.deleteOnExit();
        this.copy = to;
      }
    }

    /**
     * Count elements.
     */
    public void countElements() {
      ElementCounter counter = new ElementCounter();
      if (!this.file.exists()) return;
      try {
        // Get the SAX Parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        // Set stream
        InputStream input = new FileInputStream(this.file);
        InputSource is = new InputSource(input);
        is.setEncoding("UTF-8");

        // Parse
        parser.parse(is, counter);
      } catch (Exception ex) {

      }
      this.counter = counter;
    }
  }

  /**
   * SAX parser that counts elements in XML.
   *
   * @author Christophe Lauret
   * @version 1 November 2013
   */
  private final class ElementCounter extends DefaultHandler implements XMLWritable {

    private final Map<String, ElementCount> elements = new HashMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      ElementCount count = this.elements.get(qName);
      if (count == null) {
        count = new ElementCount(qName);
        this.elements.put(qName, count);
      }
      count.increment();
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("elements", true);
      for (ElementCount e : this.elements.values()) {
        e.toXML(xml);
      }
      xml.closeElement();
    }

    /**
     * Same boolean.
     *
     * @param counter the counter
     * @return the boolean
     */
    public boolean same(ElementCounter counter) {
      if (this.elements.size() != counter.elements.size()) return false;
      for (Entry<String, ElementCount> e : counter.elements.entrySet()) {
        ElementCount exp = e.getValue();
        ElementCount got = this.elements.get(e.getKey());
        if (got == null || got.getCount() != exp.getCount()) return false;
      }
      return true;
    }
  }

  /**
   * Number of instances an element was found.
   *
   * @author Christophe Lauret
   * @version 1 November 2013
   */
  private final class ElementCount implements XMLWritable {

    private final String _name;
    private int count = 0;

    /**
     * Instantiates a new Element count.
     *
     * @param name the name
     */
    public ElementCount(String name) {
      this._name = name;
    }

    /**
     * Increment.
     */
    public void increment() {
      this.count++;
    }

    /**
     * Gets count.
     *
     * @return the count
     */
    public int getCount() {
      return this.count;
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("element");
      xml.attribute("name", this._name);
      xml.attribute("count", this.count);
      xml.closeElement();
    }
  }
}
