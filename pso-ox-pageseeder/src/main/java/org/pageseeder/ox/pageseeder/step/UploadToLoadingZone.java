package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.model.LoadingZoneUploadParameter;
import net.pageseeder.app.simple.pageseeder.service.LoadingZoneService;
import net.pageseeder.app.simple.vault.PSOAuthConfigManager;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author ccabral
 * @since 15 February 2021
 */
public class UploadToLoadingZone implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(UploadToLoadingZone.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    //Upload file
    LOGGER.debug("Uploading to Loading Zone");

    File input = StepUtils.getInput(data, info);
//    File output = StepUtils.getOutput(data, info, input);
//    output.mkdirs();
    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());
    DefaultResult result = new DefaultResult(model, data, info, (File) null);
    LoadingZoneService service = new LoadingZoneService();
    int delayInMilleseconds = StepUtils.getParameterInt(data, info, "thread-delay-milleseconds", 500);
    PSConfig psConfig = PSOAuthConfigManager.get().getConfig();
    try {
      XMLStringWriter loadingWriter = new XMLStringWriter(XML.NamespaceAware.No);
      XMLStringWriter threadWriter = new XMLStringWriter(XML.NamespaceAware.No);
      try {
        LoadingZoneUploadParameter uploadParameter = getLoadingZoneParameters(data, info);
        int httpCode = service.upload(input, uploadParameter, item.getToken(), psConfig, loadingWriter);
        result.addExtraXML(new ExtraResultStringXML(loadingWriter.toString()));

        if (httpCode == 202) {
          GroupThreadProgressScheduleExecutorRunnable executorRunnable = new
              GroupThreadProgressScheduleExecutorRunnable(loadingWriter.toString(), threadWriter, item.getToken(), psConfig,
              delayInMilleseconds);
          executorRunnable.run();
        }
      } finally {
        result.addExtraXML(new ExtraResultStringXML(threadWriter.toString()));
      }
    } catch (MalformedURLException e) {
      LOGGER.error("String could not be transformed into URL: {}", e);
      result.setError(e);
    } catch (IOException e) {
      LOGGER.error("Exception: {}", e);
      result.setError(e);
    }


    return result;
  }

  private LoadingZoneUploadParameter getLoadingZoneParameters(PackageData data, StepInfo info) throws MalformedURLException {
    String group = StepUtils.getParameter(data, info, "group", (String) null);
    String filename = StepUtils.getParameter(data, info, "filename", (String) null);
    String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
    String xLinkId = StepUtils.getParameter(data, info, "xlinkid", (String) null);
    String member = StepUtils.getParameter(data, info, "member", (String) null);
    Boolean overwrite = "true".equals(StepUtils.getParameter(data, info, "overwrite", "false"));
    Boolean autoload = "true".equals(StepUtils.getParameter(data, info, "autoload", "false"));
    String folder = StepUtils.getParameter(data, info, "folder", (String) null);
    String urlString = StepUtils.getParameter(data, info, "url", null);
    URL url = null;
    if (!StringUtils.isBlank(urlString)) {
      url = new URL(urlString);
    }
    Boolean overwriteProperties = "true".equals(StepUtils.getParameter(data, info, "overwrite-properties", "false"));
    String title = StepUtils.getParameter(data, info, "title", (String) null);
    String description = StepUtils.getParameter(data, info, "description", (String) null);
    String labels = StepUtils.getParameter(data, info, "labels", (String) null);
    String docId = StepUtils.getParameter(data, info, "docid", (String) null);
    LoadingZoneUploadParameter parameter = new LoadingZoneUploadParameter(group, filename, uploadId, xLinkId, member,
        overwrite, autoload, folder, url, overwriteProperties, title, description, labels, docId);
    return parameter;
  }
}
