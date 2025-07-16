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
import org.pageseeder.ox.berlioz.request.RequestHandlerType;
import org.pageseeder.ox.core.*;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ccabral
 * @since 13 May 2025
 */
public class BerliozOXUtils {
  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(BerliozOXUtils.class);

  public static RequestHandlerType getRequestHandlerType(String requestHandlerType) {
    RequestHandlerType type = RequestHandlerType.FILE;
    if (requestHandlerType != null) {
      try {
        type = RequestHandlerType.valueOf(requestHandlerType.toUpperCase());
      } catch (IllegalArgumentException e) {
        type = RequestHandlerType.FILE;
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
    List<PipelineJob> jobs = new ArrayList<PipelineJob>();
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
    return flattenParameters(newParameters);
  }



  /**
   * Flatten parameters.
   *
   * @param newParameters the parameters to be flattened and added to alreadyFlattenParameters
   * @param alreadyFlattenParameters the parameters already flattened
   * @return the map
   */
  public static Map<String, String> flattenParameters (Map<String, String[]> newParameters, Map<String, String> alreadyFlattenParameters) {
    Map<String, String> parameters = new HashMap<String, String>();

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
