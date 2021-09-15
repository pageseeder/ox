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
package org.pageseeder.ox.diffx.step;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.diffx.tool.CheckRoundTripCommand;
import org.pageseeder.ox.tool.InvalidResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>source</var>This file will be converted .</li>
 * <li><var>to</var>The location of the final psml.</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>.</p>
 *
 *
 * @author Carlos Cabral
 * @version 13 April 2015
 */
public class CheckRoundTrip implements Step {

  /** The logger. */
  private static Logger LOGGER = LoggerFactory.getLogger(CheckRoundTrip.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    String source = info.getParameter("source", data.getOriginal().getName());

    // Check if valid first
    if (!isValid(data, source)) {
      LOGGER.warn("The source file does not exist {}.", source);
      return new InvalidResult(model, data).error(new IllegalArgumentException("The source file does not exist."));
    }

    // Instantiate the Round Trip Command
    CheckRoundTripCommand command = new CheckRoundTripCommand(model);
    File download = data.getFile("download");
    download.mkdirs();
    command.setDownload(data.getDownloadDir(download));
    command.setSource(source);
    return command.process(data);
  }

  /**
   * Checks if is valid.
   *
   * @param data the data
   * @param path the path
   * @return true, if is valid
   */
  private boolean isValid(PackageData data, String path) {
    boolean isValid = false;
    if (!isBlank(path)) {
      File file = data.getFile(path);
      if (file != null && file.exists()) {
        isValid = true;
      }
    }
    return isValid;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
