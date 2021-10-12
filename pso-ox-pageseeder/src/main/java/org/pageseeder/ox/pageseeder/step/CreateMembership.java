package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.pageseeder.model.MembershipParameter;
import net.pageseeder.app.simple.pageseeder.service.MembershipService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.model.*;
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
public class CreateMembership implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(CreateMembership.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Create Pageseeder Membership");
    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    //Find Member Parameters
    DefaultResult result = new DefaultResult(model, data, info, null);
    PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", ""));
    String email = StepUtils.getParameter(data, info, "email", "");
    String memberUsername = StepUtils.getParameter(data, info, "username", "");
    Boolean autoActivate = "true".equals(StepUtils.getParameter(data, info, "auto-activate", "false"));
    String firstName = StepUtils.getParameter(data, info, "firstname", "");
    String invitation = StepUtils.getParameter(data, info, "invitation", "default");
    Boolean personalGroup = "true".equals(StepUtils.getParameter(data, info, "personal-group", "false"));
    String role = StepUtils.getParameter(data, info, "role", "");
    String surname = StepUtils.getParameter(data, info, "surname", "");
    Boolean welcomeEmail = "true".equals(StepUtils.getParameter(data, info, "welcome-email", "true"));
    String notification = StepUtils.getParameter(data, info, "notification", "");
    Boolean listed = "true".equals(StepUtils.getParameter(data, info, "listed", "false"));

//    String memberPassword = StepUtils.getParameter(data, info, "password", "");
//    Boolean notifyAsync = "true".equals(StepUtils.getParameter(data, info, "notify-async", "true"));

    //create service to find Projects;
    MembershipService service = new MembershipService();
    MembershipParameter parameter = new MembershipParameter();
    PSMember member = new PSMember(memberUsername);
    if (!SimpleStringUtils.isBlank(email)) member.setEmail(email);
    if (!SimpleStringUtils.isBlank(firstName)) member.setFirstname(firstName);
    if (!SimpleStringUtils.isBlank(surname)) member.setSurname(surname);
    if (!SimpleStringUtils.isBlank(surname)) parameter.setInvitation(MembershipParameter.Invitation.valueOf(invitation));
    parameter.setAutoActivate(autoActivate);
    parameter.setWelcomeEmail(welcomeEmail);
    parameter.setPersonalGroup(personalGroup);
    parameter.setListed(listed);
    if (!SimpleStringUtils.isBlank(surname)) parameter.setNotification(PSNotification.valueOf(notification));
    if (!SimpleStringUtils.isBlank(surname)) parameter.setRole(PSRole.valueOf(role));

    PSMembership newMember = service.create(member, group, parameter, item.getToken(), PSOAuthConfigManager.get().getConfig());


    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    writer.openElement("membership");
    writer.attribute("id", newMember.getId().toString());
    writer.attribute("group", newMember.getGroup().toString());
    writer.attribute("role", newMember.getRole().toString());
    writer.attribute("notification", newMember.getNotification().toString());
    writer.openElement("member");
    writer.attribute("id", newMember.getMember().getId().toString());
    writer.attribute("name", newMember.getMember().getUsername());
    writer.attribute("firstname", newMember.getMember().getFirstname());
    writer.attribute("surname", newMember.getMember().getSurname());
    writer.attribute("email", newMember.getMember().getEmail());
    writer.attribute("status", newMember.getMember().getStatus().toString());
    writer.closeElement(); //member
    writer.closeElement(); //membership
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Create Pageseeder Membership");
    return result;
  }
}
