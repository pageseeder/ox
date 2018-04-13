/* Copyright (c) 2011 Allette Systems pty. ltd. */
package org.pageseeder.ox.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The utilities class for XML.
 *
 * @author Ciber Cai
 * @since 09 March 2011
 */
public final class XMLUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);

  private XMLUtils() {}

  /**
   * Parses the XML.
   *
   * @param xmlData the xml data
   * @param handler the handler
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void parseXML(String xmlData, DefaultHandler handler) throws IOException {
    if (xmlData == null) throw new NullPointerException("Handle is null");
    try (StringReader reader = new StringReader(xmlData)) {
      InputSource source = new InputSource(reader);
      parseXML(source, handler);
    }
  }
  
  /**
   * Parse the XML
   * @param in defines the input stream.
   * @param handler defines the handler.
   * @throws IOException the IO error occur.
   */
  public static void parseXML(InputStream in, DefaultHandler handler) throws IOException {
    // if no handler provided.
    if (handler == null) { throw new NullPointerException("Handle is null"); }
    if (in == null) { throw new NullPointerException("InputStream is null"); }

    InputSource source = new InputSource(in);
    source.setEncoding("utf-8");

    parseXML(source, handler);
  }
  
  /**
   * Parses the XML.
   *
   * @param source the source
   * @param handler the handler
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void parseXML(InputSource source, DefaultHandler handler) throws IOException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);
    factory.setXIncludeAware(true);

    source.setEncoding("utf-8");
    try
    {
      SAXParser parser = factory.newSAXParser();
      parser.parse(source, handler);
    } catch (ParserConfigurationException e) {
      LOGGER.error("Parse XML Configurate Error. ", e);
    } catch (SAXException e) {
      LOGGER.error("Parse XML SAX Error. ", e);
    }
  }
  

  /**
   * Transform the XML to result.
   *
   * @param xmlSource defines the input source
   * @param template defines the transformer source.
   * @param result defines the result.
   * @param parameters defines the list of parameter.
   * @throws IOException the IO error occur.
   * @throws TransformerException the transformation error occur.
   */
  public static void transform(Source xmlSource, Templates template, Result result, Map<String, String> parameters) throws IOException, TransformerException {
    Transformer transformer = template.newTransformer();
    if (parameters != null) {
      for (String key : parameters.keySet()) {
        transformer.setParameter(key, parameters.get(key));
      }
    }
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(xmlSource, result);
  }

}
