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
package org.pageseeder.ox.inspector;

import org.pageseeder.ox.api.PackageInspector;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.CharsetDetector;
import org.pageseeder.ox.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A inspector for PSML.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @since 13 November 2013
 */
public class PSMLInspector implements PackageInspector {

  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLInspector.class);

  private final static String PREFIX = "psml.";

  @Override
  public String getName() {
    return "ox-psml-inspector";
  }

  @Override
  public boolean supportsMediaType(String mediatype) {
    return "application/vnd.pageseeder.psml+xml".equals(mediatype.trim());
  }

  @Override
  public void inspect(PackageData pack) {
    File psml = pack.getOriginal();
    if (psml != null && psml.exists() && psml.length() > 0) {
      try {

        // Detect the charset used from actual codes.
        Charset charset = CharsetDetector.getFromBOM(psml);
        if (charset == null) {
          charset = CharsetDetector.getFromContent(psml);
        }
        pack.setProperty(PREFIX + "charset", charset.name());

        // check for well-formedness
        boolean isWellformness = isWellformedness(psml);
        pack.setProperty(PREFIX + "wellformedness", String.valueOf(isWellformness));

      } catch (IOException ex) {
        LOGGER.warn("Cannot inspect PSML {}", ex);
      }

      // parse the xml
      try (InputStream in = new FileInputStream(psml)) {
        GetPSMLHandler handler = new GetPSMLHandler(pack);
        XMLUtils.parseXML(in, handler);
      } catch (IOException ex) {
        LOGGER.warn("Cannot parse the PSML {}", ex);
      }

    }
  }

  /**
   * To return whether the XML is wellformness.
   *
   * @param xml the inspect file.
   * @return the status whether it is wellformness
   */
  private static boolean isWellformedness(File xml) {
    boolean isValid = false;
    if (xml != null && xml.exists()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      try {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(new FileInputStream(xml));
        isValid = true;
      } catch (ParserConfigurationException | SAXException | IOException ex) {
        isValid = false;
      }
    }
    return isValid;
  }

  private static class GetPSMLHandler extends DefaultHandler implements ContentHandler {

    private final PackageData _pack;

    /**
     * Instantiates a new Get psml handler.
     *
     * @param pack the pack
     */
    public GetPSMLHandler(PackageData pack) {
      this._pack = pack;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      if ("document".equals(localName) || "document".equals(qName)) {
        for (int i = 0; i < atts.getLength(); i++) {
          this._pack.setProperty(PREFIX + atts.getLocalName(i), atts.getValue(i));
        }
      }

      if ("uri".equals(localName) || "uri".equals(qName)) {
        for (int i = 0; i < atts.getLength(); i++) {
          this._pack.setProperty(PREFIX + atts.getLocalName(i), atts.getValue(i));
        }
      }
    }
  }

}
