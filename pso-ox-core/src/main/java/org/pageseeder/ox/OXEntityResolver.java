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
package org.pageseeder.ox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.InputStream;

/**
 * Resolves the identifiers specific to the OX itself.
 *
 * <p>OX public identifiers should match the following:
 *
 * <pre>
 *   -//PageSeeder//DTD::OX model 1.0//EN
 * </pre>
 *
 * <p>Note: this resolver also accepts the alias prefix <code>-//OX//DTD::</code>.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since OX 0.6.1
 */
public final class OXEntityResolver implements EntityResolver {

  /**
   * The prefix used by OX for all public identifiers.
   * <p>
   * Public identifiers starting with any other prefix will be ignored.
   */
  public static final String PUBLIC_ID_PREFIX = "-//PageSeeder//DTD::OX ";

  /**
   * The prefix used by OX for all public identifiers.
   *
   * Public identifiers starting with any other prefix will be ignored.
   */
  private static final String ALIAS_ID_PREFIX = "-//OX//DTD::";

  /**
   * The suffix used by OX for all public identifiers.
   */
  private static final String PUBLIC_ID_SUFFIX = "//EN";

  /**
   * A single instance.
   */
  private static final OXEntityResolver SINGLETON = new OXEntityResolver();

  /**
   * A logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXEntityResolver.class);

  /**
   * Creates a new OX Entity resolver.
   */
  private OXEntityResolver() {}

  /**
   * @see org.xml.sax.EntityResolver#resolveEntity(String, String)
   *
   * @param publicId The public identifier for the entity.
   * @param systemId The system identifier for the entity.
   *
   * @return The entity as an XML input source.
   *
   * @throws SAXException If the library has not been defined.
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
    InputSource source = null;
    // process only public identifiers that are valid for OX
    String dtd = toFileName(publicId);
    if (dtd != null) {
      LOGGER.debug("resolved {} to /library/{}", publicId, dtd);
      // return a special input source
      InputStream inputStream = OXEntityResolver.class.getResourceAsStream("/library/" + dtd);
      source = new InputSource(inputStream);
      // use the default behaviour
    } else {
      LOGGER.info("Tried to use the entity resolver on unknown public ID '{}'", publicId);
    }
    return source;
  }

  /**
   * Returns an entity resolver instance.
   *
   * @return an entity resolver instance.
   */
  public static OXEntityResolver getInstance() {
    return SINGLETON;
  }

  /**
   * Returns the file name for the specified public ID.
   *
   * @param publicId the public identifier.
   * @return The corresponding filename.
   */
  private static String toFileName(String publicId) {
    if (publicId == null) { return null; }
    if (!publicId.endsWith(PUBLIC_ID_SUFFIX)) { return null; }
    int length = publicId.length() - PUBLIC_ID_SUFFIX.length();
    if (publicId.startsWith(PUBLIC_ID_PREFIX)) {
      return publicId.substring(PUBLIC_ID_PREFIX.length(), length).toLowerCase().replace(' ', '-') + ".dtd";
    } else if (publicId.startsWith(ALIAS_ID_PREFIX)) {
      return publicId.substring(ALIAS_ID_PREFIX.length(), length).toLowerCase().replace(' ', '-') + ".dtd";
    } else {
      return null;
    }
  }

}
