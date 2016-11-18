/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.generator;

import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.berlioz.Requests;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.Pipeline;
import org.pageseeder.ox.core.StepDefinition;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generator to invoke OX Step.
 *
 * <h3>Parameter</h3>
 * <ul>
 *  <li><var>pipeline</var> the id of the pipeline. By default use the first pipline in model</li>
 *  <li><var>step</var> the id of step. By default use the first step in pipeline.</li>
 * </ul>
 *
 *
 * @author Ciber Cai
 * @version 16 June 2014
 */
public class ExecuteStep implements ContentGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteStep.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // get the model
    Model model = Requests.getModel(req, xml);
    if (model == null) {
      req.setStatus(ContentStatus.NOT_FOUND);
      LOGGER.error("No model found.");
      return;
    }

    // Fetch the package data
    PackageData data = Requests.getPackageData(req, xml);

    if (data == null) {
      req.setStatus(ContentStatus.NOT_FOUND);
      LOGGER.error("No package data found.");
      return;
    }

    // find the pipeline
    String pindex = req.getParameter("pipeline");

    Pipeline pipeline = null;
    if (pindex != null) {
      pipeline = model.getPipeline(pindex);
    } else {
      pipeline = model.getPipeline(0);
    }

    if (pipeline == null) {
      req.setStatus(ContentStatus.NOT_FOUND);
      LOGGER.error("No pipeline found.");
      return;
    }

    // find the step
    String sindex = req.getParameter("step");
    StepDefinition stepDef = null;
    if (sindex != null) {
      stepDef = pipeline.getStep(sindex);
    } else {
      stepDef = pipeline.getStep(0);
    }

    if (stepDef == null) {
      req.setStatus(ContentStatus.NOT_FOUND);
      LOGGER.error("No Step found.");
      return;
    }

    // execute the step
    Result result = stepDef.exec(data);
    stepDef.toXML(result, xml);

  }
}
