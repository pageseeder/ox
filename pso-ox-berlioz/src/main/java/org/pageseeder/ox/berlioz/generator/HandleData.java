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
package org.pageseeder.ox.berlioz.generator;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.servlet.HttpRequestWrapper;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.Errors;
import org.pageseeder.ox.berlioz.request.RequestHandler;
import org.pageseeder.ox.berlioz.request.RequestHandlerFactory;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

/**
 * Receives the document, assign ID, prepare for processing.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @version 14 February 2014
 */
public class HandleData extends ProfilerGenerator {

  private final static Logger LOGGER = LoggerFactory.getLogger(HandleData.class);

  @Override
  public void processSub(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    LOGGER.debug("receive package");

    LOGGER.debug("Model: {}", req.getParameter("model"));

    try {
      HttpServletRequest httpServletRequest = toHttpServletRequest(req);
      RequestHandlerFactory requestHandlerFactory = RequestHandlerFactory.getInstance();
      RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(httpServletRequest);
      List<PackageData> packs = requestHandler.receive(httpServletRequest);

      // if it's empty packagedata
      if (packs == null || packs.isEmpty()) {
        xml.emptyElement("no-package-data");
        req.setStatus(ContentStatus.BAD_REQUEST);
        return;
      }

      // serialize the packages
      xml.openElement("packages", true);
      for (PackageData pack : packs) {
        pack.inspect();
        pack.toXML(xml);
      }
      xml.closeElement();

    } catch (OXException ex) {
      Errors.oxExceptionHandler(req, xml, ex);
    }
  }

  private static HttpServletRequest toHttpServletRequest(ContentRequest req) {
    // get HttpServlet for
    HttpServletRequest hreq = null;
    if (req instanceof HttpRequestWrapper) {
      HttpRequestWrapper wrapper = (HttpRequestWrapper) req;
      hreq = wrapper.getHttpRequest();

      //If the parameter is in the url (berlioz style). It is not query parameter and neither form input.
      //They need to be added to the http request, otherwise however uses it will not have this value.
      Enumeration<String> berliozParameters = req.getParameterNames();
      while (berliozParameters.hasMoreElements()) {
        String parameterName = berliozParameters.nextElement();
        hreq.setAttribute(parameterName, req.getParameter(parameterName, ""));
      }
    } else {
      throw new IllegalArgumentException("Cannot cast ContentRequest to HttpRequest");
    }
    return hreq;
  }

}
