package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.pageseeder.service.GroupService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.model.GroupOptions;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
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

/**
 * @author vku
 * @since 07 October 2021
 */
public class CreateGroup implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(CreateGroup.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Create Pageseeder Group");
    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    //Find Projects Parameters
    String groupName = StepUtils.getParameter(data, info, "group", "");
    PSGroup newGroup = new PSGroup(groupName);
    String description = StepUtils.getParameter(data, info, "description", "");
    String projectName = StepUtils.getParameter(data, info, "project-name", "");
    String shortName = StepUtils.getParameter(data, info, "short-name", "");
    Boolean addMember = "true".equals(StepUtils.getParameter(data, info, "add-member", "true"));
    String title = StepUtils.getParameter(data, info, "title", "");


    DefaultResult result = new DefaultResult(model, data, info, null);

    //create service to find Projects
    GroupService service = new GroupService();
    GroupOptions options = new GroupOptions();
    if (!SimpleStringUtils.isBlank(description)) newGroup.setDescription(description);
//    if (!SimpleStringUtils.isBlank(projectName)) newGroup.set
//    if (!SimpleStringUtils.isBlank(description)) newGroup.setDescription(description);
    options.setAddCreatorAsMember(addMember);
    if (!SimpleStringUtils.isBlank(title)) newGroup.setTitle(title);
    PSGroup group = service.create(newGroup, item.getMember(), options, item.getToken(), PSOAuthConfigManager.get().getConfig());


    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
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
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Create Pageseeder Group");
    return result;

  }
}
