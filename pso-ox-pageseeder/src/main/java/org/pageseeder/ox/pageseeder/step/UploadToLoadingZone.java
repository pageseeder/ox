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

import net.pageseeder.app.simple.pageseeder.model.LoadingZoneUploadParameter;
import net.pageseeder.app.simple.pageseeder.service.LoadingZoneService;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.model.PSThreadStatus;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
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
 * @author ccabral
 * @since 15 February 2021
 */
public class UploadToLoadingZone extends PageseederStep {

  private static Logger LOGGER = LoggerFactory.getLogger(UploadToLoadingZone.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    //Upload file
    LOGGER.debug("Uploading to Loading Zone");

    File input = StepUtils.getInput(data, info);

    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();
    DefaultResult result = new DefaultResult(model, data, info, (File) null);
    LoadingZoneService service = new LoadingZoneService();
    int delayInMilleseconds = StepUtils.getParameterInt(data, info, "thread-delay-milleseconds", 500);
    try {
      XMLStringWriter loadingWriter = new XMLStringWriter(XML.NamespaceAware.No);
      XMLStringWriter threadWriter = new XMLStringWriter(XML.NamespaceAware.No);
      GroupThreadProgressScheduleExecutorRunnable executorRunnable = null;
      try {
        LoadingZoneUploadParameter uploadParameter = getLoadingZoneParameters(data, info);
        int httpCode = service.upload(input, uploadParameter, item.getToken(), psConfig, loadingWriter);
        result.addExtraXML(new ExtraResultStringXML(loadingWriter.toString()));

        if (httpCode == 202) {
          executorRunnable = new
              GroupThreadProgressScheduleExecutorRunnable(loadingWriter.toString(), threadWriter, item.getToken(), psConfig,
              delayInMilleseconds);
          executorRunnable.run();
        }
      } finally {
        result.addExtraXML(new ExtraResultStringXML(threadWriter.toString()));
        if (executorRunnable != null && executorRunnable.getLastStatus() != null
            && !PSThreadStatus.Status.COMPLETED.equals(executorRunnable.getLastStatus().getStatus())) {
          result.setError(new OXException(executorRunnable.getLastStatus().getLastMessage()));
        }
      }
    } catch (MalformedURLException e) {
      LOGGER.error("String could not be transformed into URL: {}", e);
      result.setError(e);
    } catch (IOException e) {
      LOGGER.error("Exception: {}", e);
      result.setError(e);
    }


    return result;
  }

  private LoadingZoneUploadParameter getLoadingZoneParameters(PackageData data, StepInfo info) throws MalformedURLException {
    String group = StepUtils.getParameter(data, info, "group", (String) null);
    String filename = StepUtils.getParameter(data, info, "filename", (String) null);
    String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
    String xLinkId = StepUtils.getParameter(data, info, "xlinkid", (String) null);
    String member = StepUtils.getParameter(data, info, "member", (String) null);
    Boolean overwrite = "true".equals(StepUtils.getParameter(data, info, "overwrite", "false"));
    Boolean autoload = "true".equals(StepUtils.getParameter(data, info, "autoload", "false"));
    String folder = StepUtils.getParameter(data, info, "folder", (String) null);
    String urlString = StepUtils.getParameter(data, info, "url", null);
    URL url = null;
    if (!StringUtils.isBlank(urlString)) {
      url = new URL(urlString);
    }
    Boolean overwriteProperties = "true".equals(StepUtils.getParameter(data, info, "overwrite-properties", "false"));
    String title = StepUtils.getParameter(data, info, "title", (String) null);
    String description = StepUtils.getParameter(data, info, "description", (String) null);
    String labels = StepUtils.getParameter(data, info, "labels", (String) null);
    String docId = StepUtils.getParameter(data, info, "docid", (String) null);
    LoadingZoneUploadParameter parameter = new LoadingZoneUploadParameter(group, filename, uploadId, xLinkId, member,
        overwrite, autoload, folder, url, overwriteProperties, title, description, labels, docId);
    return parameter;
  }
}
