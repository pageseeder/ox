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
import org.pageseeder.ox.berlioz.Errors;
import org.pageseeder.ox.berlioz.Requests;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>GEt a pipeline of a model</p>
 *
 *
 * @version 25 June 2020
 */
public final class GetModelPipeline extends BasicGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetModelPipeline.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    Requests.ensureConfigured();
    // get the model
    String name = req.getParameter("model");
    String pipeline = req.getParameter("pipeline");
    // Try model specified in request
    Model model = null;
    if (name != null && !name.isEmpty()) {
      if (Model.isDefined(name)) {
        model = new Model(name);
      } else {
        Errors.noModel(req, xml, name);
      }
    } else {
      model = Model.getDefault();
    }

    if (model != null) {
      model.load();
      if (StringUtils.isBlank(pipeline)) {
        pipeline = model.getPipelineDefault().id();
      }
      if (model.getPipeline(pipeline) != null) {
        model.toXML(xml, pipeline);
      } else {
        Errors.noPipeline(req, xml, pipeline);
      }

    } else {
      LOGGER.error("No model found.");
      xml.emptyElement("model");
    }
  }
}
