package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.service.MemberService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
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
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vku
 * @since 12 October 2021
 */
public class EditMember extends PageseederStep {
  private static Logger LOGGER = LoggerFactory.getLogger(EditMember.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Edit Pageseeder Member");

    //Token item to get member and credentials. And the PSConfig
    TokensVaultItem item = super.getTokensVaultItem(data, info);
    PSConfig psConfig = super.getPSOAuthConfig(data, info).getConfig();

    //Find Member Parameters
    DefaultResult result = new DefaultResult(model, data, info, null);

    String currentPassword = StepUtils.getParameter(data, info, "current-password", "");
    String email = StepUtils.getParameter(data, info, "email", "");
    Boolean emailAttachments = "true".equals(StepUtils.getParameter(data, info, "email-attachments", ""));
    String firstName = StepUtils.getParameter(data, info, "first-name", "");
    String memberUsername = StepUtils.getParameter(data, info, "username", "");
    String memberUsernameNew = StepUtils.getParameter(data, info, "new-username", "");
    String memberPassword = StepUtils.getParameter(data, info, "password", "");
    Boolean onVacation = "true".equals(StepUtils.getParameter(data, info, "on-vacation", "false"));
    String surname = StepUtils.getParameter(data, info, "surname", "");

    //create service to find Projects;
    MemberService service = new MemberService();
    PSMember member = new PSMember(memberUsername);
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

    service.edit(member, memberUsernameNew, memberPassword, firstName, surname, email, writer, item.getToken(), psConfig);
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Edit Pageseeder Member");
    return result;
  }
}
