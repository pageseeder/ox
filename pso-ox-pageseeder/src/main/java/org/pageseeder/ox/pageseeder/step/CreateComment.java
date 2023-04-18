/*
 * Copyright 2023 Allette Systems (Australia)
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

import net.pageseeder.app.simple.pageseeder.service.CommentService;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.util.StepUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ccabral
 * @since 19 April 2023
 */
public class CreateComment extends PageseederStep {
  private static Logger LOGGER = LoggerFactory.getLogger(CreateComment.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Create Pageseeder Group");
    //Token item to get member and credentials. And the PSConfig
    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();

    //Find Projects Parameters
    String groupName = StepUtils.getParameter(data, info, "group", "");



    DefaultResult result = new DefaultResult(model, data, info, null);

    //create service to find Projects
    CommentService service = new CommentService();
    //service.create()

/*    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    writer.openElement("group");
    writer.attribute("id", group.getId().toString());
    writer.attribute("name", group.getName());
    writer.closeElement(); //group
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));*/

    LOGGER.debug("End Create Pageseeder Group");
    return result;

  }
}
