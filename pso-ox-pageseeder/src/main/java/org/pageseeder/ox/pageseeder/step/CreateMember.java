package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.pageseeder.service.MemberService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.model.MemberOptions;
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
 * @since 07 October 2021
 */
public class CreateMember implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(CreateMember.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Create Pageseeder Member");
    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    //Find Member Parameters
    DefaultResult result = new DefaultResult(model, data, info, null);

    String email = StepUtils.getParameter(data, info, "email", "");
    String memberUsername = StepUtils.getParameter(data, info, "username", "");
    Boolean autoActivate = "true".equals(StepUtils.getParameter(data, info, "auto-activate", "false"));
    String firstName = StepUtils.getParameter(data, info, "firstname", "");
    Boolean personalGroup = "true".equals(StepUtils.getParameter(data, info, "personal-group", "false"));
    String surname = StepUtils.getParameter(data, info, "surname", "");
    Boolean welcomeEmail = "true".equals(StepUtils.getParameter(data, info, "welcome-email", "true"));

    //create service to find Projects;
    MemberService service = new MemberService();
    MemberOptions options = new MemberOptions();
    PSMember member = new PSMember(memberUsername);
    if (!SimpleStringUtils.isBlank(email)) member.setEmail(email);
    if (!SimpleStringUtils.isBlank(firstName)) member.setFirstname(firstName);
    if (!SimpleStringUtils.isBlank(surname)) member.setSurname(surname);
    options.setAutoActivate(autoActivate);
    options.setWelcomeEmail(welcomeEmail);
    options.setPersonalGroup(personalGroup);

    PSMember newMember = service.create(member, options, item.getToken(), PSOAuthConfigManager.get().getConfig());


    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    writer.openElement("member");
    writer.attribute("id", newMember.getId().toString());
    writer.attribute("name", newMember.getUsername());
    writer.attribute("firstname", newMember.getFirstname());
    writer.attribute("surname", newMember.getSurname());
    writer.attribute("email", newMember.getEmail());
    writer.attribute("status", newMember.getStatus().toString());
    writer.closeElement(); //member
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Create Pageseeder Member");
    return result;
  }
}
