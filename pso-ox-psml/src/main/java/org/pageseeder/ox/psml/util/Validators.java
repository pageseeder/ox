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
package org.pageseeder.ox.psml.util;

import org.pageseeder.schematron.SchematronException;
import org.pageseeder.schematron.SchematronResult;
import org.pageseeder.schematron.Validator;
import org.pageseeder.schematron.ValidatorFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

/**
 * General XML Utilities.
 *
 * @author Philip Rutherford
 * @author Jean-Baptiste Reure
 *
 * @version 8 March 2018
 */
public final class Validators {

  // Messages customized by Rick are in 'XMLSchemaMessages.properties'
  // See on how to customize this for xerces http://xerces.apache.org/xerces2-j/properties.html
  private static final String TOPOLOGI_PARSER = "com.topologi.xerces.parsers.SAXParser";

  /**
   * Schematron validator factory object
   */
  private static final ValidatorFactory SCHEMATRON_FACTORY = new ValidatorFactory();

  /**
   * No constructor for utility classes.
   */
  private Validators() {
  }

  /**
   * Validates the Well-formedness of an XML file.
   *
   * @param in  A stream on the XML content to validate
   *
   * @return the errors list.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  public static List<String> validateWellFormednessReturnErrors(InputStream in) throws SAXException, IOException {
    if (in == null) throw new NullPointerException("Cannot validate a null file");
    XMLReader reader = XMLReaderFactory.createXMLReader(TOPOLOGI_PARSER);
    ValidationErrorHandler eh = new ValidationErrorHandler();
    reader.setErrorHandler(eh);
    reader.parse(new InputSource(in));
    return eh.getErrorList();
  }

  /**
   * Validates the Well-formedness of an XML file.
   *
   * @param in  the XML content to validate
   *
   * @return the errors list.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  public static List<String> validateWellFormednessReturnErrors(String in) throws SAXException, IOException {
    if (in == null) throw new NullPointerException("Cannot validate a null file");
    XMLReader reader = XMLReaderFactory.createXMLReader(TOPOLOGI_PARSER);
    ValidationErrorHandler eh = new ValidationErrorHandler();
    reader.setErrorHandler(eh);
    reader.parse(new InputSource(new StringReader(in)));
    return eh.getErrorList();
  }

  /**
   * Validates an XML file using Schematron.
   *
   * @param in         the XML content to validate
   * @param schemaPath The path to the XSD file.
   * @param namespace  Namespace used for the external schema location property.
   *
   * @return the errors message.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  public static String validateXmlFileWithSchema(String in, String schemaPath, String namespace) throws SAXException,
      IOException {
    ValidationErrorHandler eh = validateWithSchema(new InputSource(new StringReader(in)), schemaPath, namespace);
    return eh.getErrors();
  }

  /**
   * Validates an XML file using Schematron.
   *
   * @param in         A reader on the XML content to validate
   * @param schemaPath The path to the XSD file.
   * @param namespace  Namespace used for the external schema location property.
   *
   * @return the errors message.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  public static String validateXmlFileWithSchema(InputStream in, String schemaPath, String namespace) throws SAXException,
      IOException {
    ValidationErrorHandler eh = validateWithSchema(new InputSource(in), schemaPath, namespace);
    return eh.getErrors();
  }

  /**
   * Validates an XML file using Schematron.
   *
   * @param in         the XML content to validate
   * @param schemaPath The path to the XSD file.
   * @param namespace  Namespace used for the external schema location property.
   *
   * @return the list of errors.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  public static List<String> validateXmlFileWithSchemaReturnErrors(String in, String schemaPath, String namespace) throws SAXException,
      IOException {
    ValidationErrorHandler eh = validateWithSchema(new InputSource(new StringReader(in)), schemaPath, namespace);
    return eh.getErrorList();
  }

  /**
   * Validates an XML file using Schematron.
   *
   * @param in         A reader on the XML content to validate
   * @param schemaPath The path to the XSD file.
   * @param namespace  Namespace used for the external schema location property.
   *
   * @return the list of errors.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  public static List<String> validateXmlFileWithSchemaReturnErrors(InputStream in, String schemaPath, String namespace) throws SAXException,
      IOException {
    ValidationErrorHandler eh = validateWithSchema(new InputSource(in), schemaPath, namespace);
    return eh.getErrorList();
  }

  /**
   * Validates an XML file using Schematron.
   *
   * @param in         A reader on the XML content to validate
   * @param schemaPath The path to the XSD file.
   * @param namespace  Namespace used for the external schema location property.
   *
   * @return the error handler.
   *
   * @throws IOException          Should any IO error occur
   * @throws SAXException         Should any parsing error occur (file invalid, etc...)
   * @throws NullPointerException if the specified reader is <code>null</code>.
   */
  private static ValidationErrorHandler validateWithSchema(InputSource in, String schemaPath, String namespace) throws SAXException,
      IOException {
    if (in == null) throw new NullPointerException("Cannot validate a null file");
    XMLReader reader = XMLReaderFactory.createXMLReader(TOPOLOGI_PARSER);
    reader.setFeature("http://xml.org/sax/features/validation", true);
    reader.setFeature("http://apache.org/xml/features/validation/schema", true);
    reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
    ValidationErrorHandler eh = new ValidationErrorHandler();
    reader.setErrorHandler(eh);
    String path = new File(schemaPath).exists() ?
                  new File(schemaPath).toURI().toURL().toString().replaceAll(" ", "%20") : schemaPath;
    if (namespace == null) {
      reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", path);
    } else {
      reader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", namespace+" "+path);
    }
    reader.setEntityResolver(new ValidationEntityResolver());
    reader.parse(in);
    return eh;
  }

  /**
   * Validates an XML file using Schematron.
   *
   * @param in                   The XML content to validate
   * @param schematronSource     The Schematron source.
   *
   * @return <code>null</code> if successful or the error message otherwise.
   *
   * @throws SchematronException if there was an error when validating.
   */
  public static String validateXmlFileWithSchematron(String in, Source schematronSource) throws SchematronException {
    if (in == null) throw new NullPointerException("Cannot validate a null file");
    if (schematronSource == null) return null;
    // use default preprocessor: iso_svrl.xsl (included in ant_schematron.jar)
    Validator validator = SCHEMATRON_FACTORY.newValidator(schematronSource);
    SchematronResult result = validator.validate(new StreamSource(new StringReader(in)));
    if (!result.isValid()) {
      List<String> errors = result.getFailedAssertions();
      StringBuilder message = new StringBuilder();
      for (String error : errors) {
        message.append(error).append("\n");
      }
      return message.toString();
    }
    return null;
  }
}
