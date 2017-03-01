/*
 * Copyright (c) 2012 Allette Systems (Australia) Pty. Ltd.
 */
package org.pageseeder.ox.berlioz;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A utility class for parameters.
 *
 * @author Christophe Lauret
 * @version 3 November 2011
 */
public final class Errors {

  /**
   * Utility class.
   */
  private Errors() {}

  // Client errors
  // ----------------------------------------------------------------------------------------------

  /**
   * Write the XML for when a required parameter is missing.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param name The name of the missing parameter
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noParameter(ContentRequest req, XMLWriter xml, String name) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The parameter '" + name + "' was not specified.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when a required parameter is missing.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req   The content request
   * @param xml   The XML writer
   * @param names The names of the missing parameter
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noParameter(ContentRequest req, XMLWriter xml, String... names) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    StringBuilder list = new StringBuilder();
    for (String name : names) {
      if (list.length() > 0) {
        list.append(" ,");
      }
      list.append('\'').append(name).append('\'');
    }
    xml.attribute("message", "At least one of the following parameters must be specified: " + list);
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when the user has not logged in, but is required.
   *
   * <p>Also sets the status of the response to 'forbidden'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noUser(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The user must be logged in to access this information");
    xml.closeElement();
    req.setStatus(ContentStatus.FORBIDDEN);
  }

  /**
   * Write the XML for when a required parameter is missing.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param name The name of the invalid parameter
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void invalidParameter(ContentRequest req, XMLWriter xml, String name) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The parameter '" + name + "' is invalid.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when a required data cannot process.
   * 
   * <p>Also sets the status of the response to 'bad request'.
   * 
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param message the message
   * @throws IOException If an error occurs writing XML.
   */
  public static void invalidData(ContentRequest req, XMLWriter xml, String message) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", message);
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when the main document could not be found.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param path The path of the document part
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noDocumentPart(ContentRequest req, XMLWriter xml, String path) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The main document part '" + path + "' could not be found.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when the XSLT template could not be found.
   *
   * <p>Also sets the status of the response to 'service unavailable'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param path The path of the document part
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noTemplate(ContentRequest req, XMLWriter xml, String path) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "configuration");
    xml.attribute("message", "The template '" + path + "' could not be found.");
    xml.closeElement();
    req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);
  }

  /**
   * Write the XML for when the XSLT template could not be found.
   *
   * <p>Also sets the status of the response to 'internal server error'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param ex   The exception thrown by the transformer
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void templateError(ContentRequest req, XMLWriter xml, TransformerException ex) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "server");
    xml.attribute("message", ex.getMessageAndLocation());
    xml.closeElement();
    req.setStatus(ContentStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Write the XML for when the schema could not be found.
   *
   * <p>Also sets the status of the response to 'service unavailable'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param path The path of the document part
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noSchema(ContentRequest req, XMLWriter xml, String path) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "configuration");
    xml.attribute("message", "The schema '" + path + "' could not be found.");
    xml.closeElement();
    req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);
  }

  /**
   * Write the XML for when the schematron validation throws an error.
   *
   * <p>Also sets the status of the response to 'internal server error'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param ex   The exception thrown by the validator
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void schemaError(ContentRequest req, XMLWriter xml, Exception ex) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "server");
    xml.attribute("message", ex.getMessage());
    xml.closeElement();
    req.setStatus(ContentStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Write the XML for when the request should be a multipart request but is not.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void notMultipart(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The specified request is not a multipart request.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when the request should be a multipart request but is not.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void noUploadFile(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The specified request did not contain any uploaded file.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when the request should be a multipart request but is not.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   *
   * @throws IOException If an error occurs writing XML.
   */
  public static void illegalUploadFile(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The uploaded file could not be parsed.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * No model.
   *
   * @param req the req
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noModel(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "Cannot find the Model.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * No packagedata.
   *
   * @param req the req
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noPackagedata(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "Cannot find the packagedata.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * No pipeline.
   *
   * @param req the req
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noPipeline(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "Cannot find the pipeline.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

  /**
   * No step.
   *
   * @param req the req
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noStep(ContentRequest req, XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "Cannot find the step.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }

}
