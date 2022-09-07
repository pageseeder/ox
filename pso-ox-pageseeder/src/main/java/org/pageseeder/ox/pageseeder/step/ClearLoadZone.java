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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * The parameters that can be used in the step are:
 *
 * <ul>
 *   <li><b>uploadid</b></li>
 *   <li><b>xlinkid</b></li>
 * </ul>
 *
 *
 * @author ccabral
 * @author Adriano Akaishi
 * @since 07 September 2022
 */
public class ClearLoadZone extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(ClearLoadZone.class);

  private int percentage = 0;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start cleaning load zone");

    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();
    DefaultResult result = new DefaultResult(model, data, info, (File) null);
    LoadingZoneService service = new LoadingZoneService();

    //There is not an easy way to calculated the percentage, then it will just guess.
    this.percentage = 3;
    try {
      PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", (String) null));
      String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
      String xlinkid = StepUtils.getParameter(data, info, "xlinkid", (String) null);
      XMLStringWriter unzipWriter = new XMLStringWriter(XML.NamespaceAware.No);
      XMLStringWriter threadWriter = new XMLStringWriter(XML.NamespaceAware.No);
//
//      LOGGER.debug("Start Loading");
//      StartLoadingParameter startLoadingParameters = getStartLoadingParameters(data, info);
//      this.percentage = 5;
//      try {
//        service.startLoading(item.getMember(), group, startLoadingParameters, item.getToken(), psConfig, unzipWriter);
//        this.percentage = 20;
//
//        result.addExtraXML(new ExtraResultStringXML(unzipWriter.toString()));
//        this.percentage = 30;
//
//        GroupThreadProgressScheduleExecutorRunnable executorRunnable = new
//            GroupThreadProgressScheduleExecutorRunnable(unzipWriter.toString(), threadWriter, item.getToken(), psConfig,
//            delayInMilleseconds);
//        executorRunnable.run();
      } finally {
//        result.addExtraXML(new ExtraResultStringXML(threadWriter.toString()));
//        this.percentage = 100;
      }
//    }
//    catch (MalformedURLException e) {
//      LOGGER.warn("String could not be transformed into URL");
//      result.setError(e);
//    } catch (IOException e){
//      LOGGER.error("Exception: {}", e);
//      result.setError(e);
//    }

    return result;
  }


  @Override
  public int percentage() {
    return percentage;
  }
}
