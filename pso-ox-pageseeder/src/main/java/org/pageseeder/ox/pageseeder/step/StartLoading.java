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
package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.model.StartLoadingParameter;
import net.pageseeder.app.simple.pageseeder.service.LoadingZoneService;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSThreadStatus;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.pageseeder.thread.GroupThreadProgressScheduleExecutorRunnable;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * The parameters that can be used in the step are:
 *
 * <ul>
 *   <li><b>assignedto</b></li>
 *   <li><b>createxrefs</b></li>
 *   <li><b>description</b></li>
 *   <li><b>due</b></li>
 *   <li><b>folder</b></li>
 *   <li><b>index</b></li>
 *   <li><b>mode</b></li>
 *   <li><b>notification-content</b></li>
 *   <li><b>notification-labels</b></li>
 *   <li><b>notification-subject</b></li>
 *   <li><b>notify</b></li>
 *   <li><b>overwrite</b></li>
 *   <li><b>overwrite-properties</b></li>
 *   <li><b>priority</b></li>
 *   <li><b>status</b></li>
 *   <li><b>summary</b></li>
 *   <li><b>uploadid</b></li>
 *   <li><b>url</b></li>
 *   <li><b>validate</b></li>
 *   <li><b>workflow-labels</b></li>
 *   <li><b>workflow-notify</b></li>
 *   <li><b>xmlspecv</b></li>
 *   <li><b>thread-delay-milleseconds</b> is used to delay each attempt to check the status of unzipping in
 *   pageseeder</li>
 * </ul>
 *
 *
 * @author ccabral
 * @since 15 February 2021
 */
public class StartLoading extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(StartLoading.class);

  private int percentage = 0;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Loading Process");

    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();
    DefaultResult result = new DefaultResult(model, data, info, (File) null);
    LoadingZoneService service = new LoadingZoneService();
    int delayInMilleseconds = StepUtils.getParameterInt(data, info, "thread-delay-milleseconds", 500);
    //There is not an easy way to calculated the percentage, then it will just guess.
    this.percentage = 3;
    try {
      PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", (String) null));
      XMLStringWriter unzipWriter = new XMLStringWriter(XML.NamespaceAware.No);
      XMLStringWriter threadWriter = new XMLStringWriter(XML.NamespaceAware.No);

      LOGGER.debug("Start Loading");
      StartLoadingParameter startLoadingParameters = getStartLoadingParameters(data, info);
      this.percentage = 5;
      GroupThreadProgressScheduleExecutorRunnable executorRunnable = null;
      try {
        service.startLoading(item.getMember(), group, startLoadingParameters, item.getToken(), psConfig, unzipWriter);
        this.percentage = 20;

        result.addExtraXML(new ExtraResultStringXML(unzipWriter.toString()));
        this.percentage = 30;

        executorRunnable = new
            GroupThreadProgressScheduleExecutorRunnable(unzipWriter.toString(), threadWriter, item.getToken(), psConfig,
            delayInMilleseconds);
        executorRunnable.run();
      } finally {
        result.addExtraXML(new ExtraResultStringXML(threadWriter.toString()));
        if (executorRunnable != null && executorRunnable.getLastStatus() != null
            && !PSThreadStatus.Status.COMPLETED.equals(executorRunnable.getLastStatus().getStatus())) {
          result.setError(new OXException(executorRunnable.getLastStatus().getLastMessage()));
        }
        this.percentage = 100;
      }
    } catch (MalformedURLException e) {
      LOGGER.warn("String could not be transformed into URL");
      result.setError(e);
    } catch (IOException e){
      LOGGER.error("Exception: {}", e);
      result.setError(e);
    }

    return result;
  }

  private StartLoadingParameter getStartLoadingParameters(PackageData data, StepInfo info) throws MalformedURLException {
    String assignedTo = StepUtils.getParameter(data, info, "assignedto", (String) null);
    Boolean createXrefs = "true".equals(StepUtils.getParameter(data, info, "createxrefs", "false"));
    String description = StepUtils.getParameter(data, info, "description", (String) null);
    String due = StepUtils.getParameter(data, info, "due", (String) null);
    String folder = StepUtils.getParameter(data, info, "folder", (String) null);
    Boolean index = "true".equals(StepUtils.getParameter(data, info, "index", "false"));
    String mode = StepUtils.getParameter(data, info, "mode", (String) null);
    String notificationContent = StepUtils.getParameter(data, info, "notification-content", (String) null);
    String notificationLabels	 = StepUtils.getParameter(data, info, "notification-labels", (String) null);
    String notificationSubject	 = StepUtils.getParameter(data, info, "notification-subject", (String) null);
    String notify = StepUtils.getParameter(data, info, "notify", (String) null);
    Boolean overwrite = "true".equals(StepUtils.getParameter(data, info, "overwrite", "false"));
    Boolean overwriteProperties = "true".equals(StepUtils.getParameter(data, info, "overwrite-properties", "false"));
    String priority = StepUtils.getParameter(data, info, "priority", (String) null);
    String status = StepUtils.getParameter(data, info, "status", (String) null);
    Boolean summary = "true".equals(StepUtils.getParameter(data, info, "summary", "false"));
    String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
    String urlString = StepUtils.getParameter(data, info, "url", null);
    URL url = null;
    if (!StringUtils.isBlank(urlString)) {
      url = new URL(urlString);
    }
    Boolean validate = "true".equals(StepUtils.getParameter(data, info, "validate", "false"));
    String workflowLabels = StepUtils.getParameter(data, info, "workflow-labels", (String) null);
    String workflowNotify	 = StepUtils.getParameter(data, info, "workflow-notify", (String) null);
    String xmlspec = StepUtils.getParameter(data, info, "xmlspec", (String) null);
    StartLoadingParameter parameter = new StartLoadingParameter(assignedTo, createXrefs, description, due, folder,
        index, mode, notificationContent, notificationLabels, notificationSubject, notify, overwrite, overwriteProperties,
        priority, status, summary, uploadId, url, validate, workflowLabels, workflowNotify, xmlspec);
    return parameter;
  }

  @Override
  public int percentage() {
    return percentage;
  }
}
