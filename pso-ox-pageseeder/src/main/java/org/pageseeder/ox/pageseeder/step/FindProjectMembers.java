package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.service.MemberService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
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
 * @since 05 October 2021
 */
public class FindProjectMembers implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(FindProjects.class);


  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Find Pageseeder Project Member");
    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    //Find Projects Parameters
    PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", ""));
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);


    DefaultResult result = new DefaultResult(model, data, info, null);

    //create service to find Projects
    MemberService service = new MemberService();
    String etag = service.list(group, writer, item.getToken(),PSOAuthConfigManager.get().getConfig() );

    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("End Find Pageseeder Projects Members");
    return result;
  }
}
