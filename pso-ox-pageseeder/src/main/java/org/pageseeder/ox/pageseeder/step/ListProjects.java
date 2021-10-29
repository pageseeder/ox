/*
 * Copyright (c) 2021 Allette Systems pty. Ltd.
 */
package org.pageseeder.ox.pageseeder.step;

import java.util.HashMap;
import java.util.List;

import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pageseeder.app.simple.pageseeder.service.GroupService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;

/**
 * @author lkirkwood
 * @since 2021-10-29
 */
public class ListProjects implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(ListProjects.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("ListProjects begin.");
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    HashMap<String, String> params = new HashMap<>();
      params.put("archived",      StepUtils.getParameter(data, info, "archived",     "false"));
      params.put("for",           StepUtils.getParameter(data, info, "for",          "member"));
      params.put("groups",        StepUtils.getParameter(data, info, "groups",       "true"));
      params.put("relationship",  StepUtils.getParameter(data, info, "relationship", "children"));
      params.put("page",          StepUtils.getParameter(data, info, "page",          "1"));
      params.put("pagesize",      StepUtils.getParameter(data, info, "pagesize",      "1000"));

    DefaultResult result = new DefaultResult(model, data, info, null);

    List<PSGroup> groups = new GroupService().listProjects(
      item.getMember(), params, item.getToken(), PSOAuthConfigManager.get().getConfig());
    
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