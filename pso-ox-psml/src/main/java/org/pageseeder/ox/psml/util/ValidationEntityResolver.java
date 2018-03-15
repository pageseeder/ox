/*
 * Copyright (c) 1999-2016 Allette Systems Pty Ltd
 */
package org.pageseeder.ox.psml.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Collects validation errors.
 *
 * @author Philip Rutherford
 *
 * @version 3.1000
 * @since 3.1000
 */
public final class ValidationEntityResolver implements EntityResolver {

  @Override
  public InputSource resolveEntity(String publicId, String systemId) {
    if (systemId.startsWith("ps://"))
      return new InputSource(ValidationEntityResolver.class.getResourceAsStream('/'+systemId.substring(5)));
    return null;
  }

}
