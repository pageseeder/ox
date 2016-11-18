package org.pageseeder.ox.berlioz.generator;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.servlet.HttpRequestWrapper;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.Errors;
import org.pageseeder.ox.berlioz.Requests;
import org.pageseeder.ox.berlioz.util.FileHandler;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives the document, assign ID, prepare for processing.
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @version 14 February 2014
 */
public class HandleData implements ContentGenerator {

  private final static Logger LOGGER = LoggerFactory.getLogger(HandleData.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    LOGGER.debug("receive package");
    // get the model
    Model model = Requests.getModel(req, xml);
    if (model == null) return;
    List<PackageData> packs = null;
    try {
      packs = FileHandler.receive(model.name(), toHttpServletRequest(req));
    } catch (OXException ex) {
      Errors.invalidData(req, xml, ex.getMessage());
      return;
    }

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

  }

  private static HttpServletRequest toHttpServletRequest(ContentRequest req) {
    // get HttpServlet for
    HttpServletRequest hreq = null;
    if (req instanceof HttpRequestWrapper) {
      HttpRequestWrapper wrapper = (HttpRequestWrapper) req;
      hreq = wrapper.getHttpRequest();
    } else {
      throw new IllegalArgumentException("Cannot cast ContentRequest to HttpRequest");
    }
    return hreq;
  }

}
