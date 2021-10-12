package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.pageseeder.service.ExportService;
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author vku
 * @since 12 October 2021
 */
public class Export implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(Export.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Pageseeder Export");
    //Token item to get member and credentials
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());

    //Find Member Parameters
    DefaultResult result = new DefaultResult(model, data, info, null);
    PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", ""));
    String memberUsername = StepUtils.getParameter(data, info, "username", "");
    Boolean allUrls = "true".equals(StepUtils.getParameter(data, info, "allurls", "false"));
    String context = StepUtils.getParameter(data, info, "context", "");
    String path = StepUtils.getParameter(data, info, "path", "");
    String uris = StepUtils.getParameter(data, info, "uris", "");
    String compare = StepUtils.getParameter(data, info, "compare", "");
    String excludes = StepUtils.getParameter(data, info, "excludes", "");
    Boolean failOnError = "true".equals(StepUtils.getParameter(data, info, "fail-on-error", "true"));
    Integer forwardDepth = Integer.valueOf(StepUtils.getParameter(data, info, "forward-depth", "0"));
    String includes = StepUtils.getParameter(data, info, "includes", "");
    String publicationID = StepUtils.getParameter(data, info, "publication-id", "");
    String release = StepUtils.getParameter(data, info, "release", "");
    Integer reverseDepth = Integer.valueOf(StepUtils.getParameter(data, info, "reverse-depth", "0"));
    String xrefTypes = StepUtils.getParameter(data, info, "xref-types", "");



    Map<String, String> parameters = new HashMap<>();
    parameters.put("allurls", java.lang.Boolean.toString(allUrls));
    if (!SimpleStringUtils.isBlank(context)) parameters.put("context", context);
    if (!SimpleStringUtils.isBlank(path)) parameters.put("path", path);
    if (!SimpleStringUtils.isBlank(uris)) parameters.put("uris", context);
    if (!SimpleStringUtils.isBlank(compare)) parameters.put("compare", context);
    if (!SimpleStringUtils.isBlank(excludes)) parameters.put("excludes", context);
    parameters.put("fail-on-error", java.lang.Boolean.toString(failOnError));
    parameters.put("forward-depth", java.lang.Integer.toString(forwardDepth));
    if (!SimpleStringUtils.isBlank(includes)) parameters.put("includes", context);
    if (!SimpleStringUtils.isBlank(publicationID)) parameters.put("publicationid", context);
    if (!SimpleStringUtils.isBlank(release)) parameters.put("release", context);
    parameters.put("reverse-depth", java.lang.Integer.toString(reverseDepth));
    if (!SimpleStringUtils.isBlank(xrefTypes)) parameters.put("xref-types", context);

    //create service to find Projects;
    ExportService service = new ExportService();
    PSMember member = new PSMember(memberUsername);

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

    String etag = service.getGroupURIs(member, group, parameters, writer, item.getToken(), PSOAuthConfigManager.get().getConfig());
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));

    LOGGER.debug("Start Pageseeder Export");
    return result;
  }
}
