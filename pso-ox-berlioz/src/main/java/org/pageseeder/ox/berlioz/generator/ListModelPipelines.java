/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.generator;

import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.ox.berlioz.Errors;
import org.pageseeder.ox.core.Model;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>To list the pipeline from configuration</p>
 *
 *
 * @version 19 June 2014
 */
public final class ListModelPipelines extends BasicGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListModelPipelines.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // get the model
    String name = req.getParameter("model");
    // Try model specified in request
    if (name != null && !name.isEmpty()) {
      if (Model.isDefined(name)) {
        Model model = new Model(name);
        model.load();
        model.toXML(xml);
      } else {
        Errors.invalidParameter(req, xml, "model");
      }
    } else {
      LOGGER.error("No model found.");
      xml.emptyElement("model");
    }
  }
}
