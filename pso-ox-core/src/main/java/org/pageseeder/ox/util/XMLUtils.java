/* Copyright (c) 2011 Allette Systems pty. ltd. */
package org.pageseeder.ox.util;

import java.io.IOException;
import java.io.InputStream;
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
   * Parse the XML
   * @param in defines the input stream.
   * @param handler defines the handler.
   * @throws IOException the IO error occur.
   */
  public static void parseXML(InputStream in, DefaultHandler handler) throws IOException {

    // if no handler provided.
    if (handler == null) { throw new NullPointerException("Handle is null"); }
    if (in == null) { throw new NullPointerException("InputStream is null"); }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);
    factory.setXIncludeAware(true);

    InputSource source = new InputSource(in);
    source.setEncoding("utf-8");

    // And parse!
    SAXParser parser;
    try {
      parser = factory.newSAXParser();
      parser.parse(source, handler);
    } catch (ParserConfigurationException | SAXException ex) {
      LOGGER.error("Cannot parse ", ex);
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
