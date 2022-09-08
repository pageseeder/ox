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

import net.pageseeder.app.simple.pageseeder.model.UnzipParameter;
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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * This step calls a service in pageseeder that is asynchronous. Therefor it is recommended to use this with async=true.
 *
 * The parameters that can be used in the step are:
 *
 * <ul>
 *   <li><b>path</b></li>
 *   <li><b>deleteoriginal</b></li>
 *   <li><b>uploadid</b></li>
 *   <li><b>xlinkid</b></li>
 *   <li><b>thread-delay-milleseconds</b> is used to delay each attempt to check the status of unzipping in
 *   pageseeder</li>
 * </ul>
 *
 * @author ccabral
 * @since 15 February 2021
 */
public class UnzipLoadingZoneContent extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(UnzipLoadingZoneContent.class);

  private int percentage = 0;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Unzip Loading zone Content");

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

      //Unzip
      UnzipParameter unzipParameter = getUnzipParameters(data, info);
      this.percentage = 5;

      GroupThreadProgressScheduleExecutorRunnable executorRunnable = null;
      try {
        //The unzip is an Asynchronous process therefore we need to check its status.
        service.unzip(item.getMember(), group, unzipParameter, item.getToken(), psConfig, unzipWriter);
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
    } catch (Exception e){
      LOGGER.error("Exception: {}", e);
      result.setError(e);
    }

    return result;
  }

  private UnzipParameter getUnzipParameters(PackageData data, StepInfo info) {
    String pathString = StepUtils.getParameter(data, info, "path", (String) null);
    Path path = Paths.get(pathString);
    Boolean deleteOriginal = "true".equals(StepUtils.getParameter(data, info, "deleteoriginal", "false"));
    String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
    String xLinkIdString = StepUtils.getParameter(data, info, "xlinkid", (String) null);
    Long xLinkId = null;
    if (!StringUtils.isBlank(xLinkIdString)) {
      xLinkId = Long.parseLong(xLinkIdString);
    }
    UnzipParameter parameter = new UnzipParameter(path, deleteOriginal, uploadId, xLinkId);
    return parameter;
  }

  @Override
  public int percentage() {
    return this.percentage;
  }
}
