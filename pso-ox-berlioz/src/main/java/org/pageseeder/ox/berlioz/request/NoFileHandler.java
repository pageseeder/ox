/*
 * Copyright 2025 Allette Systems (Australia)
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
package org.pageseeder.ox.berlioz.request;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.util.BerliozOXUtils;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This can be used when a pipeline can start without a file.
 * To use it, the parameter {@link RequestHandler#HANDLER_TYPE_PARAMETER} should have the value
 * {@link RequestHandlerType#NOFILE}.
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class NoFileHandler implements RequestHandler {

  /** The logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(NoFileHandler.class);

  private final static NoFileHandler INSTANCE = new NoFileHandler();

  private NoFileHandler(){
    LOGGER.info("NoFileHandler created");
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static NoFileHandler getInstance() {
    return INSTANCE;
  }

  /**
   * Receive.
   *
   * If there is any parameter in the url like /model/{model}/pipeline/{pipeline}.html . they will not be in the
   * HttpServletRequest.
   * Then before calling this method, you will need to set them in the attribute of HttpServletRequest, like:
   * req.setAttribute("model", model);
   * req.setAttribute("pipeline", pipeline);
   *
   * @param req the ContentRequest
   * @return the list of PackageData
   * @throws IOException when I/O error occur.
   * @throws OXException the OX exception
   */
  public List<PackageData> receive(HttpServletRequest req) throws IOException, OXException {
    List<PackageData> packs = new ArrayList<>();
    String model = req.getParameter("model");
    if (StringUtils.isBlank(model)) {
      model = (String) req.getAttribute("model");
      if (StringUtils.isBlank(model)) {
        throw new OXException("Model cannot be null or empty");
      }
    }

    PackageData pack = toPackageData(model, toParameters(req.getParameterMap()));
    LOGGER.debug("pack {}", pack.id());
    pack.saveProperties();
    packs.add(pack);
    // Return package data
    return packs;
  }

  /**
   * Create a new package data from the specified file item if possible.
   *
   * @param model the model
   * @param parameters the parameters
   * @return the package data
   */
  private PackageData toPackageData(String model, Map<String, String> parameters) {
    LOGGER.debug("Starts toPackageData {}", model);

    PackageData pack = PackageData.newPackageData(model, null);

    for (Entry<String, String> parameter : parameters.entrySet()) {
      pack.setParameter(parameter.getKey(), parameter.getValue());
    }

    LOGGER.debug("Ends toPackageData {}/{}", model, pack.id());
    return pack;
  }

  /**
   * To parameters.
   *
   * @param urlParameters the url parameters
   * @return the map
   */
  private Map<String, String> toParameters (Map<String, String[]> urlParameters) {
    return BerliozOXUtils.flattenParameters(urlParameters);
  }
}
