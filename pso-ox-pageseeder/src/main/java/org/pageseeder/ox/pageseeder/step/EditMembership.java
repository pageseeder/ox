package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.service.MembershipService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
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
 * @since 12 October 2021
 */
public class EditMembership implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(EditMember.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Edit Pageseeder Membership");
    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    //Find Member Parameters
    DefaultResult result = new DefaultResult(model, data, info, null);

    PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", ""));
    String memberUsername = StepUtils.getParameter(data, info, "username", "");

    //create service to find Projects;
    MembershipService service = new MembershipService();
    PSMember member = new PSMember(memberUsername);
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

    String etag = service.removeMember(group, member, writer, item.getToken(), PSOAuthConfigManager.get().getConfig());
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Edit Pageseeder Membership");
    return result;
  }
}
