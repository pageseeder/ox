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

import net.pageseeder.app.simple.pageseeder.model.ProjectsFindParameter;
import net.pageseeder.app.simple.pageseeder.service.GroupService;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TODO after tested move to ox project to the module psml.
 *
 * TODO allow to select which user to use (logged or app user)
 *
 *
 *
 * @author vku
 * @since 11 February 2021
 */
public class FindProjects extends PageseederStep {
  private static Logger LOGGER = LoggerFactory.getLogger(FindProjects.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Find Pageseeder Projects or Group");

    //Token item to get member and credentials. And the PSConfig
    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();

    //Find Projects Parameters
    Boolean archived = "true".equals(StepUtils.getParameter(data, info, "archived", "false"));
    String forParameter = StepUtils.getParameter(data, info, "for", null);
    String namePrefix = StepUtils.getParameter(data, info, "nameprefix", null);
    String titlePrefix = StepUtils.getParameter(data, info, "titleprefix", null);
    Integer page = StepUtils.getParameterInt(data, info, "page", 1);
    Integer pageSize = StepUtils.getParameterInt(data, info, "pageSize", 1000);

    //Create find projects parameters
    ProjectsFindParameter parameters = new ProjectsFindParameter(archived, forParameter, namePrefix, titlePrefix, page, pageSize);

    DefaultResult result = new DefaultResult(model, data, info, null);

    //create service to find Projects
    GroupService service = new GroupService();
    List<PSGroup> groupList = service.findProjectsList(item.getMember(), parameters, item.getToken(),psConfig);

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    writer.openElement("projects");
    for (PSGroup group : groupList) {
      writer.openElement("group");
      writer.attribute("id", group.getId().toString());
      writer.attribute("name", group.getName());

      if (group.getParentName() != null) {
        //project name
        writer.attribute("parent-name", group.getParentName());
      }

      if (group.getShortName() != null) {
        //Group name without project (if it is a group)
        writer.attribute("short-name", group.getShortName());
      }

      writer.attribute("title", group.getTitle());
      writer.attribute("description", group.getDescription());
      writer.attribute("owner", group.getOwner());
      writer.closeElement(); //group
    }
    writer.closeElement();//projects

    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Find Pageseeder Projects or Group");
    return result;
  }
}
