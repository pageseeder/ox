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
package org.pageseeder.ox.berlioz.util;

import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.berlioz.request.RequestHandler;
import org.pageseeder.ox.berlioz.request.RequestHandlerType;
import org.pageseeder.ox.core.*;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Berlioz ox utils.
 *
 * @author ccabral
 * @since 13 May 2025
 */
public class BerliozOXUtils {
  /** The logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(BerliozOXUtils.class);

  /**
   * Gets parameter handler type.
   *
   * @param req the req
   * @return the parameter handler type
   */
  public static String getParameterHandlerType(HttpServletRequest req) {
    String parameterHandler = req.getParameter(RequestHandler.HANDLER_TYPE_PARAMETER);
    if (StringUtils.isBlank(parameterHandler)) {
      parameterHandler = (String) req.getAttribute(RequestHandler.HANDLER_TYPE_PARAMETER);
    }
    return parameterHandler;
  }

  /**
   * Gets request handler type.
   *
   * @param requestHandlerType the request handler type
   * @return the request handler type
   */
  public static RequestHandlerType getRequestHandlerType(String requestHandlerType) {
    RequestHandlerType type = RequestHandlerType.FILE;
    if (requestHandlerType != null) {
      try {
        type = RequestHandlerType.valueOf(requestHandlerType.toUpperCase());
      } catch (IllegalArgumentException e) {
        LOGGER.info("Invalid request handler type: {}", requestHandlerType);
      }
    }
    return type;
  }

  /**
   * To pipeline jobs.
   *
   * @param packs the packs
   * @return the list
   */
  public static List<PipelineJob> toPipelineJobs(List<PackageData> packs) {
    ensureConfigured();
    List<PipelineJob> jobs = new ArrayList<>();
    long slowSize = GlobalSettings.get("ox2.slow-mode.size", -1);
    long maxInactiveTimeAllowed = Long.parseLong(GlobalSettings.get("ox2.max-inactive-time-ms",
        String.valueOf(StepJob.DEFAULT_MAX_INACTIVE_TIME_MS)));
    LOGGER.debug("Started creating the Pipeline Jobs");
    for (PackageData pack : packs) {
      boolean isSlowMode = slowSize > 0 && pack.getOriginal().exists() && (pack.getOriginal().length() - slowSize * 1024 > 0);
      LOGGER.debug("slow mode {}", isSlowMode);
      String p = pack.getParameter("pipeline");
      String modelName = pack.getParameter("model");
      Model model = new Model(modelName);
      LOGGER.debug("Model {} ", modelName);
      if (p != null) {
        Pipeline pipeline = model.getPipeline(p);
        if (pipeline != null) {
          PipelineJob job = new PipelineJob(pipeline, pack);
          job.setSlowMode(isSlowMode);
          job.setMaxInactiveTimeAllowed(maxInactiveTimeAllowed);
          jobs.add(job);
        } else {
          LOGGER.warn("pipeline {} not found", p);
        }
      } else {
        Pipeline pipeline = model.getPipelineDefault();
        PipelineJob job = new PipelineJob(pipeline, pack);
        job.setSlowMode(isSlowMode);
        jobs.add(job);
      }
    }
    LOGGER.debug("Ended creating the Pipeline Jobs");

    return jobs;
  }

  /**
   * Ensure the configuration file is set.
   */
  private static void ensureConfigured() {
    OXConfig config = OXConfig.get();
    File dir = config.getModelsDirectory();
    LOGGER.debug("Model Directory is null {}", dir == null);
    if (dir == null) {
      LOGGER.debug("Global Settings {}", GlobalSettings.getAppData());
      config.setModelsDirectory(new File(GlobalSettings.getAppData(), "model"));
    }
  }

  /**
   * Flatten parameters.
   *
   * @param newParameters the parameters to be flattened
   * @return the map
   */
  public static Map<String, String> flattenParameters (Map<String, String[]> newParameters) {
    return flattenParameters(newParameters, null);
  }


  /**
   * Flatten parameters.
   *
   * @param newParameters            the parameters to be flattened and added to alreadyFlattenParameters
   * @param alreadyFlattenParameters the parameters already flattened
   * @return the map
   */
  public static Map<String, String> flattenParameters (Map<String, String[]> newParameters, Map<String, String> alreadyFlattenParameters) {
    if (alreadyFlattenParameters == null) {
      alreadyFlattenParameters = new HashMap<>();
    }

    if (newParameters != null) {
      for(Map.Entry<String, String[]> param:newParameters.entrySet()) {
        if (!alreadyFlattenParameters.containsKey(param.getKey())) {
          alreadyFlattenParameters.put(param.getKey(), StringUtils.convertToString(param.getValue(), ","));
        }
      }
    }

    return alreadyFlattenParameters;
  }
}
