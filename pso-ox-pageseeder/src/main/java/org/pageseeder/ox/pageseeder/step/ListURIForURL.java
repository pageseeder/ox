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

import net.pageseeder.app.simple.pageseeder.model.ListURIParameters;
import net.pageseeder.app.simple.pageseeder.service.URIService;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.search.Page;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * TODO allow to select which user to use (logged or app user)
 *
 * This sevices uses this
 * https://dev.pageseeder.com/api/web_services/services/list-uri-uris-forurl-GET.html
 *
 * <h3>Parmeters:</h3>
 * <ul>
 *   <li>group: the group name (required)</li>
 *   <li>folder: it is the folder path (required). If it is not informed, then it will use the root folder of the group.
 *       Examples:
 *       folder parameter equal folder01/folder01_01 the application will change to
 *       http://pageseederdomain/ps/project/group/folder01/folder01_01
 *   </li>
 *   <li>page: page number (Optional)</li>
 *   <li>pagesize: page size (Optional)</li>
 *   <li>relationship: children, descendants, etc (Optional)</li>
 *   <li>type: document, folder, all (optional)</li>
 *   <li>all-data: to retrieve all data (uris) for this folder.</li>
 * </ul>
 *
 * @author Carlos Cabral
 * @since 14 April 2021
 */
public class ListURIForURL implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(ListURIForURL.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Listing URIs fro URL");

    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());
    PSConfig psConfig = PSConfig.getDefault();

    //Find Projects Parameters
    PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", ""));
    String folder = StepUtils.getParameter(data, info, "folder", "");
    String url = buildURL(psConfig, group, folder);
    int pageNumber = StepUtils.getParameterInt(data, info, "page", 1);
    int pageSize =  StepUtils.getParameterInt(data, info, "pagesize", 100);
    Page page = new Page(pageNumber, pageSize);
    String relationship = StepUtils.getParameter(data, info, "relationship", "");
    String type =  StepUtils.getParameter(data, info, "type", "all");
    boolean allData = "true".equalsIgnoreCase(StepUtils.getParameter(data, info, "all-data", "false"));

    ListURIParameters listURIParameters = new ListURIParameters(group, url, page, relationship, type, allData);

    DefaultResult result = new DefaultResult(model, data, info, null);

    //Call service.
    URIService uriService = new URIService();
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    try {
      uriService.listForURL(listURIParameters, item.getToken(), psConfig, writer);
    } catch (IOException e) {
      result.setError(e);
    }

    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End listing URIs for url");
    return result;
  }

  private String buildURL (PSConfig psConfig, PSGroup group, String folder) {
    Objects.requireNonNull(group.getName());
    StringBuilder path = new StringBuilder("/");
    path.append(group.getName().replace("-", "/"));
    if (!StringUtils.isBlank(folder)) {
      path.append("/");
      path.append(folder);
    }
    return psConfig.buildDocumentURL(path.toString());
  }
}
