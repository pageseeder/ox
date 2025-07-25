/*
 * Copyright (c) 2021 Allette Systems pty. Ltd.
 */
package org.pageseeder.ox.pageseeder.step;

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
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @author lkirkwood
 * @since 2021-10-29
 */
public class ListProjects extends PageseederStep {
  private static Logger LOGGER = LoggerFactory.getLogger(ListProjects.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("ListProjects begin.");

    //Token item to get member and credentials. And the PSConfig
    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();

    HashMap<String, String> params = new HashMap<>();
      params.put("archived",      StepUtils.getParameter(data, info, "archived",     "false"));
      params.put("for",           StepUtils.getParameter(data, info, "for",          "member"));
      params.put("groups",        StepUtils.getParameter(data, info, "groups",       "true"));
      params.put("relationship",  StepUtils.getParameter(data, info, "relationship", "children"));
      params.put("page",          StepUtils.getParameter(data, info, "page",          "1"));
      params.put("pagesize",      StepUtils.getParameter(data, info, "pagesize",      "1000"));

    DefaultResult result = new DefaultResult(model, data, info, null);

    // listProjects requires psberlioz-simple-core:2021.10-03+
    List<PSGroup> groups = new GroupService().listProjects(
      item.getMember(), params, item.getToken(), psConfig);

    XMLStringWriter writer = new XMLStringWriter(NamespaceAware.No);
    writer.openElement("projects");
    for (PSGroup group : groups) {
      writer.openElement("group");
      writer.attribute("id", group.getId().toString());
      writer.attribute("name", group.getName());

      if (group.getParentName() != null) {
        writer.attribute("parent-name", group.getParentName());
      }

      if (group.getShortName() != null) {
        writer.attribute("short-name", group.getShortName());
      }

      writer.attribute("title", group.getTitle());
      writer.attribute("description", group.getDescription());
      writer.attribute("owner", group.getOwner());
      writer.closeElement();
    }
    writer.closeElement();
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    return result;
  }
}