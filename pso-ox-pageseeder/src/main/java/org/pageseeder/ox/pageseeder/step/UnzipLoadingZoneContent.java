package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.model.UnzipParameter;
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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author ccabral
 * @since 15 February 2021
 */
public class UnzipLoadingZoneContent implements Step {
  private static Logger LOGGER = LoggerFactory.getLogger(UnzipLoadingZoneContent.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Unzip Loading zone Content");

    TokensVaultItem item = TokensVaultManager.get(VaultUtils.getDefaultPSOAuthConfigName());
    DefaultResult result = new DefaultResult(model, data, info, (File) null);
    LoadingZoneService service = new LoadingZoneService();

    try {
      PSGroup group = new PSGroup(StepUtils.getParameter(data, info, "group", (String) null));
      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

      //Unzip
      UnzipParameter unzipParameter = getUnzipParameters(data, info);
      service.unzip(item.getMember(), group, unzipParameter, item.getToken(), PSOAuthConfigManager.get().getConfig(), writer);
      result.addExtraXML(new ExtraResultStringXML(writer.toString()));
    } catch (Exception e){
      LOGGER.error("Exception: {}", e);
      result.setError(e);
    }

    return result;
  }

  private UnzipParameter getUnzipParameters(PackageData data, StepInfo info) {
    String pathString = StepUtils.getParameter(data, info, "path", (String) null);
    Path path = Paths.get(pathString);
    Boolean deleteOriginal = "true".equals(StepUtils.getParameter(data, info, "deleteoriginal", "false"));
    String uploadId = StepUtils.getParameter(data, info, "uploadid", (String) null);
    String xLinkIdString = StepUtils.getParameter(data, info, "xlinkid", (String) null);
    Long xLinkId = null;
    if (!StringUtils.isBlank(xLinkIdString)) {
      xLinkId = Long.parseLong(xLinkIdString);
    }
    UnzipParameter parameter = new UnzipParameter(path, deleteOriginal, uploadId, xLinkId);
    return parameter;
  }
}
