/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.generator;

import java.io.IOException;
import java.util.List;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.ox.berlioz.Requests;
import org.pageseeder.ox.core.Model;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>To list the models from configuration</p>
 *
 *
 * @version 14 October 2016
 */
public final class ListModels extends BasicGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListModels.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    List<Model> models = Requests.listModel(req, xml);
    LOGGER.debug("Number of models {}", models.size());

    xml.openElement("models");
    for (Model model : models) {
      //model.load();
      model.toXML(xml);
    }
    xml.closeElement();

  }
}
