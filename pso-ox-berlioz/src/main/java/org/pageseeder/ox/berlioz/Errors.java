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
package org.pageseeder.ox.berlioz;

import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.ox.OXErrorMessage;
import org.pageseeder.ox.OXException;
import org.pageseeder.xmlwriter.XMLWriter;

import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * A utility class for parameters.
 *
 * @author Christophe Lauret
 * @version 3 November 2011
 */
public final class Errors {

  /**
   * The enum Error type.
   */
  enum ErrorType {
    /**
     * Client error type.
     */
    CLIENT,
    /**
     * Configuration error type.
     */
    CONFIGURATION,
    /**
     * Server error type.
     */
    SERVER}

  /**
   * Utility class.
   */
  private Errors() {}

  /**
   * Ox exception handler.
   *
   * @param xml the xml
   * @param ex  the ex
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void oxExceptionHandler(XMLWriter xml, OXException ex) throws IOException {
    final String message = ex.getMessage();
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT,  ex.getErrorMessageCode(), message, null, xml);
  }

  /**
   * Write the XML for when a required data cannot process.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @param ex  the ex
   * @throws IOException If an error occurs writing XML.
   */
  public static void oxExceptionHandler(ContentRequest req, XMLWriter xml, OXException ex) throws IOException {
    final String message = ex.getMessage();
    generic(ContentStatus.INTERNAL_SERVER_ERROR, ErrorType.CLIENT,  ex.getErrorMessageCode(), message, req, xml);
  }

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
   * @throws IOException If an error occurs writing XML.
   */
  public static void noParameter(ContentRequest req, XMLWriter xml, String name) throws IOException {
    final String message = "The parameter '" + name + "' was not specified.";
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
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
   * @throws IOException If an error occurs writing XML.
   */
  public static void noParameter(ContentRequest req, XMLWriter xml, String... names) throws IOException {
    StringBuilder list = new StringBuilder();
    for (String name : names) {
      if (list.length() > 0) {
        list.append(" ,");
      }
      list.append('\'').append(name).append('\'');
    }
    final String message = "At least one of the following parameters must be specified: " + list;
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * Write the XML for when the user has not logged in, but is required.
   *
   * <p>Also sets the status of the response to 'forbidden'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @throws IOException If an error occurs writing XML.
   */
  public static void noUser(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "The user must be logged in to access this information";
    generic(ContentStatus.FORBIDDEN, ErrorType.CLIENT, message, req, xml);
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
   * @throws IOException If an error occurs writing XML.
   */
  public static void invalidParameter(ContentRequest req, XMLWriter xml, String name) throws IOException {
    final String message = "The parameter '" + name + "' is invalid.";
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * Write the XML for when a required data cannot process.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req     The content request
   * @param xml     The XML writer
   * @param message the message
   * @throws IOException If an error occurs writing XML.
   */
  public static void invalidData(ContentRequest req, XMLWriter xml, String message) throws IOException {
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
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
   * @throws IOException If an error occurs writing XML.
   */
  public static void noDocumentPart(ContentRequest req, XMLWriter xml, String path) throws IOException {
    final String message = "The main document part '" + path + "' could not be found.";
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
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
   * @throws IOException If an error occurs writing XML.
   */
  public static void noTemplate(ContentRequest req, XMLWriter xml, String path) throws IOException {
    final String message = "The template '" + path + "' could not be found.";
    generic(ContentStatus.SERVICE_UNAVAILABLE, ErrorType.CONFIGURATION, message, req, xml);
  }

  /**
   * Write the XML for when the XSLT template could not be found.
   *
   * <p>Also sets the status of the response to 'internal server error'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @param ex  The exception thrown by the transformer
   * @throws IOException If an error occurs writing XML.
   */
  public static void templateError(ContentRequest req, XMLWriter xml, TransformerException ex) throws IOException {
    final String message = ex.getMessageAndLocation();
    generic(ContentStatus.INTERNAL_SERVER_ERROR, ErrorType.CONFIGURATION, message, req, xml);
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
   * @throws IOException If an error occurs writing XML.
   */
  public static void noSchema(ContentRequest req, XMLWriter xml, String path) throws IOException {
    final String message = "The schema '" + path + "' could not be found.";
    generic(ContentStatus.SERVICE_UNAVAILABLE, ErrorType.CONFIGURATION, message, req, xml);
  }

  /**
   * Write the XML for when the schematron validation throws an error.
   *
   * <p>Also sets the status of the response to 'internal server error'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @param ex  The exception thrown by the validator
   * @throws IOException If an error occurs writing XML.
   */
  public static void schemaError(ContentRequest req, XMLWriter xml, Exception ex) throws IOException {
    final String message = ex.getMessage();
    generic(ContentStatus.INTERNAL_SERVER_ERROR, ErrorType.SERVER, message, req, xml);
  }

  /**
   * Write the XML for when the request should be a multipart request but is not.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @throws IOException If an error occurs writing XML.
   */
  public static void notMultipart(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "The specified request is not a multipart request.";
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * Write the XML for when the request should be a multipart request but is not.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @throws IOException If an error occurs writing XML.
   */
  public static void noUploadFile(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "The specified request did not contain any uploaded file.";
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * Write the XML for when the request should be a multipart request but is not.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req The content request
   * @param xml The XML writer
   * @throws IOException If an error occurs writing XML.
   */
  public static void illegalUploadFile(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "The uploaded file could not be parsed.";
    generic(ContentStatus.BAD_REQUEST, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * No model.
   *
   * @param req the req
   * @param xml the xml
   * @param model the model
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noModel(ContentRequest req, XMLWriter xml, String model) throws IOException {
    final String message = "Cannot find the model " + model + ".";
    generic(ContentStatus.NOT_FOUND, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * No packagedata.
   *
   * @param req the req
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noPackagedata(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "Cannot find the packagedata.";
    generic(ContentStatus.NOT_FOUND, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * No pipeline.
   *
   * @param req the req
   * @param xml the xml
   * @param pipelineId the pipelineId
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noPipeline(ContentRequest req, XMLWriter xml, String pipelineId) throws IOException {
    final String message = "Cannot find the pipeline" + pipelineId + ".";
    generic(ContentStatus.NOT_FOUND, ErrorType.CLIENT, message, req, xml);
  }

  /**
   * No step.
   *
   * @param req the req
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void noStep(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "Cannot find the step.";
    generic(ContentStatus.NOT_FOUND, ErrorType.CLIENT, message, req, xml);
  }

  /**
   *
   * @param status
   * @param type
   * @param message
   * @param req
   * @param xml
   * @throws IOException
   */
  private static void generic(ContentStatus status, ErrorType type, String message, ContentRequest req, XMLWriter xml)
      throws IOException {
    generic(status, type,  OXErrorMessage.UNKNOWN.getCode(), message, req, xml);
  }

  /**
   *
   * @param status
   * @param type
   * @param code
   * @param message
   * @param req
   * @param xml
   * @throws IOException
   */
  private static void generic(ContentStatus status, ErrorType type, String code, String message, ContentRequest req,
                              XMLWriter xml) throws IOException {
    xml.openElement("error");
    xml.attribute("type", type.name().toLowerCase());
    xml.attribute("code", code);
    xml.attribute("message", message);
    xml.closeElement();
    if (req != null) {
      req.setStatus(status);
    }
  }
}
