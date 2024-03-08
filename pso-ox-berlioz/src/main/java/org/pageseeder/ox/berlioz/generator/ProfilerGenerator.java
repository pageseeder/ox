/*
 * Copyright 2024 Allette Systems (Australia)
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
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 *
 * @author Carlos Cabral
 * @version 7 March 2024
 */
public abstract class ProfilerGenerator implements ContentGenerator {

  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    long startTime = System.currentTimeMillis();
    boolean shouldProfile = "true".equalsIgnoreCase(GlobalSettings.get("ox2.profile", "false"));
    //To run garbage collector
    boolean runGC = "true".equalsIgnoreCase(GlobalSettings.get("ox2.run-gc", "false"));
    final String model = req.getParameter("model");
    final String pipeline = req.getParameter("pipeline");
    final String step = req.getParameter("step");

    logInfo(shouldProfile,"start", model, pipeline, step, 0L);

    processSub(req, xml);

    if (runGC) {
      logInfo(shouldProfile,"Before GC", model, pipeline, step, System.currentTimeMillis() - startTime);
      System.out.println("[OX Profile] Calling garbage collector.");
      System.gc();
      logInfo(shouldProfile,"After GC", model, pipeline, step, System.currentTimeMillis() - startTime);
    }

    logInfo(shouldProfile,"end", model, pipeline, step, System.currentTimeMillis() - startTime);
  }

  protected void logInfo(boolean shouldProfile, String title, String model, String pipeline, String step, long timeSpent) {
    if (shouldProfile) {
      System.out.println("##########################################################################################");
      System.out.println("[OX Profile] " + title);
      Runtime runtime = Runtime.getRuntime();
      if (!StringUtils.isBlank(model)) {
        System.out.println("[OX Profile] Model: " + model);
      }

      if (!StringUtils.isBlank(pipeline)) {
        System.out.println("[OX Profile] Pipeline: " + pipeline);
      }

      if (!StringUtils.isBlank(step)) {
        System.out.println("[OX Profile] Step: " + step);
      }
      System.out.println("[OX Profile] max memory: " + runtime.maxMemory());
      System.out.println("[OX Profile] free memory: " + runtime.freeMemory());
      System.out.println("[OX Profile] total memory: " + runtime.totalMemory());
      if (timeSpent > 0L) {
        System.out.println("[OX Profile] time spent: " + timeSpent + "ms");
      }
      System.out.println("##########################################################################################");
    }
  }

  public abstract void processSub(ContentRequest req, XMLWriter xml) throws BerliozException, IOException;

}
