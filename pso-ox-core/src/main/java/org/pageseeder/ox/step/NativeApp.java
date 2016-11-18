/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.step;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

  private static Logger LOGGER = LoggerFactory.getLogger(NativeApp.class);

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

    /** A list of logs **/
    private List<String> logs;

    private NativeApplicationResult(Model model, PackageData data) {
      super(model, data);
    }

    /**
     * @param log the log in String
     */
    public void addLog(String log) {
      if (this.logs == null) {
        this.logs = new ArrayList<String>();
      }
      this.logs.add(log);
    }

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

    @Override
    public boolean isDownloadable() {
      return false;
    }
  }
}
