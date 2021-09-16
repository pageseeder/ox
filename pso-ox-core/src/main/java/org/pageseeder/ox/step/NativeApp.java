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
package org.pageseeder.ox.step;

import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A step to run the native application </p>.
 *
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var> the app location, which is a relative path in {@link PackageData} .</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>Otherwise return {@link NativeApplicationResult}
 *
 * @author Ciber Cai
 * @since 19 July 2016
 */
public class NativeApp implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(NativeApp.class);

  /* (non-Javadoc)
   * @see org.pageseeder.ox.api.Step#process(org.pageseeder.ox.core.Model, org.pageseeder.ox.core.PackageData, org.pageseeder.ox.api.StepInfo)
   */
  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    File app = data.getFile(info.getParameter("input", info.input()));
    // if the input file is not exist
    if (app == null || !app.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the input file " + app + ".")); }

    NativeApplicationResult result = new NativeApplicationResult(model, data);
    Process p = null;
    try {
      StringBuilder cmd = new StringBuilder(app.getCanonicalPath());
      if (data.getParameter("args") != null) {
        cmd.append(" ").append(data.getParameter("args"));
      }

      // execute the application
      LOGGER.debug("execute " + cmd.toString());
      p = Runtime.getRuntime().exec(cmd.toString());

      // store the output
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = reader.readLine();
      while (line != null) {
        line = reader.readLine();
        LOGGER.debug(">" + line);
        result.addLog(line);
      }
    } catch (Exception ex) { // catch everything
      LOGGER.warn("Cannot execute the app {}", ex);
      result.setError(ex);
    } finally {
      try {
        if (p != null) {
          p.waitFor();
          p.destroy();
        }
      } catch (InterruptedException ex) {
        LOGGER.warn("Cannot stop the executable file", ex);
        p = null;
      }
    }

    // Stop the timer
    result.done();
    return result;
  }

  /**
   * A {@link Result} for {@link NativeApplicationCommand} only.
   *
   * @author Ciber Cai
   * @since  13 February 2014
   */
  private static class NativeApplicationResult extends ResultBase implements Result {

    /**  A list of logs *. */
    private List<String> logs;

    /**
     * Instantiates a new native application result.
     *
     * @param model the model
     * @param data the data
     */
    private NativeApplicationResult(Model model, PackageData data) {
      super(model, data);
    }

    /**
     * Adds the log.
     *
     * @param log the log in String
     */
    public void addLog(String log) {
      if (this.logs == null) {
        this.logs = new ArrayList<String>();
      }
      this.logs.add(log);
    }

    /* (non-Javadoc)
     * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
     */
    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result", true);
      xml.attribute("type", "NativeApplicationResult");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      xml.openElement("logs");
      if (this.logs != null) {
        for (String log : this.logs) {
          xml.element("log", log);
        }
      }
      xml.closeElement(); // logs

      // The details of any error
      if (this.error() != null) {
        OXErrors.toXML(error(), xml, true);
      }

      xml.closeElement();// result
    }

    /* (non-Javadoc)
     * @see org.pageseeder.ox.tool.ResultBase#isDownloadable()
     */
    @Override
    public boolean isDownloadable() {
      return false;
    }
  }
}
