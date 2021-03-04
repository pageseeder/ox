package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.model.StartLoadingParameter;
import net.pageseeder.app.simple.pageseeder.service.LoadingZoneService;
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
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author ccabral
 * @since 15 February 2021
 */
public class StartLoading implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(StartLoading.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Loading Process");
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());
    DefaultResult result = new DefaultResult(model, data, info, (File) null);
    LoadingZoneService service = new LoadingZoneService();
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
    PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", (String) null));
    try {
      LOGGER.debug("Start Loading");
      StartLoadingParameter startLoadingParameters = getStartLoadingParameters(data, info);
      service.startLoading(item.getMember(), group, startLoadingParameters, item.getToken(), PSOAuthConfigManager.get().getConfig(), writer);
      result.addExtraXML(new ExtraResultStringXML(writer.toString()));
    } catch (MalformedURLException e) {
      LOGGER.warn("String could not be transformed into URL");
      result.setError(e);
    }

    return result;
  }

  private StartLoadingParameter getStartLoadingParameters(PackageData data, StepInfo info) throws MalformedURLException {
    String assignedTo = StepUtils.getParameter(data, info, "assignedto", (String) null);
    Boolean createXrefs = "true".equals(StepUtils.getParameter(data, info, "createxrefs", "false"));
    String description = StepUtils.getParameter(data, info, "description", (String) null);
    String due = StepUtils.getParameter(data, info, "due", (String) null);
    String folder = StepUtils.getParameter(data, info, "folder", (String) null);
    Boolean index = "true".equals(StepUtils.getParameter(data, info, "index", "false"));
    String mode = StepUtils.getParameter(data, info, "mode", (String) null);
    String notificationContent = StepUtils.getParameter(data, info, "notification-content", (String) null);
    String notificationLabels	 = StepUtils.getParameter(data, info, "notification-labels", (String) null);
    String notificationSubject	 = StepUtils.getParameter(data, info, "notification-subject", (String) null);
    String notify = StepUtils.getParameter(data, info, "notify", (String) null);
    Boolean overwrite = "true".equals(StepUtils.getParameter(data, info, "overwrite", "false"));
    Boolean overwriteProperties = "true".equals(StepUtils.getParameter(data, info, "overwrite-properties", "false"));
    String priority = StepUtils.getParameter(data, info, "priority", (String) null);
    String status = StepUtils.getParameter(data, info, "status", (String) null);
    Boolean summary = "true".equals(StepUtils.getParameter(data, info, "summary", "false"));
    String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
    String urlString = StepUtils.getParameter(data, info, "url", null);
    URL url = null;
    if (!StringUtils.isBlank(urlString)) {
      url = new URL(urlString);
    }
    Boolean validate = "true".equals(StepUtils.getParameter(data, info, "validate", "false"));
    String workflowLabels = StepUtils.getParameter(data, info, "workflow-labels", (String) null);
    String workflowNotify	 = StepUtils.getParameter(data, info, "workflow-notify", (String) null);
    String xmlspec = StepUtils.getParameter(data, info, "xmlspec", (String) null);
    StartLoadingParameter parameter = new StartLoadingParameter(assignedTo, createXrefs, description, due, folder,
        index, mode, notificationContent, notificationLabels, notificationSubject, notify, overwrite, overwriteProperties,
        priority, status, summary, uploadId, url, validate, workflowLabels, workflowNotify, xmlspec);
    return parameter;
  }
}
