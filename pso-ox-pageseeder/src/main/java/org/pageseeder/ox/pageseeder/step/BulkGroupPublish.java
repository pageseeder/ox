package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.SimpleSiteException;
import net.pageseeder.app.simple.core.utils.SimpleXMLUtils;
import net.pageseeder.app.simple.pageseeder.model.PSPublish;
import net.pageseeder.app.simple.pageseeder.service.PublishService;
import net.pageseeder.app.simple.pageseeder.xml.PSPublishHandler;
import net.pageseeder.app.simple.vault.*;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkGroupPublish implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkGroupPublish.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    String psconfigName = StepUtils.getParameter(data, info, "psconfig", VaultUtils.getDefaultPSOAuthConfigName());
    TokensVaultItem item = TokensVaultManager.get(psconfigName);

    DefaultResult result = new DefaultResult(model, data, info, null);

    PSOAuthConfig psOAuthConfig = PSOAuthConfigManager.get(psconfigName);
    PSConfig psConfig = psOAuthConfig.getConfig();

    File inputXml = StepUtils.getInput(data, info);

    PublishService publish = new PublishService();

    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

    GroupPublishInputHandler groupPublishInputHandler = new GroupPublishInputHandler();

    List<GroupPublish> groupPublishList = readXML(inputXml, groupPublishInputHandler);


    PSMember member = item.getMember();
    PSCredentials credentials = item.getToken();

    writer.openElement("publishes");
    for (GroupPublish gp : groupPublishList) {
      writer.openElement("publish");
      String project = gp.getProject();
      PSGroup group = new PSGroup(project + "-" + gp.getGroup());
      String target = gp.getTarget();
      PublishService.Type type = gp.getType();
      PublishService.LogLevel logLevel = gp.getLogLevel();
      Map<String,String> scriptParameters = new HashMap<>();
      PSPublishHandler psPublishHandler = new PSPublishHandler();
      try {
        publish.startGroupPublish(member, group, project, target, type, logLevel, scriptParameters, credentials, psConfig,
            psPublishHandler);
      } catch (IOException e) {
        throw new SimpleSiteException("Failed to initiate group publish.");
      }
      PSPublish currentPublish = psPublishHandler.get();

      //INITIALISED
      //INPROGRESS,
      String status = currentPublish.getStatus();
      while (!isPublishCompleted(status)) {
        //check status till completed or error or failed
        try {
          publish.checkPublish(member, psPublishHandler.get().getId(), credentials, psConfig, groupPublishInputHandler); //TODO: add new attributes into groupPublishInputHandler
        } catch (IOException e) {
          throw new SimpleSiteException("Failed to check group publish.");
        }
        //result.addExtraXML(new ExtraResultStringXML(groupPublishHandler)); //TODO: add the new attributes into the xml result
//        writer.attribute("status", groupPublishHandler.get().getStatus());
      }
      writer.closeElement();//publish
    }
    writer.closeElement();//publishes

    return null;
  }

  private List<GroupPublish> readXML(File xml, GroupPublishInputHandler handler) {
    //GroupPublishInputHandler handler = new GroupPublishInputHandler();
    try {
      SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    } catch (IOException exc) {
      throw new SimpleSiteException("Failed to parse XML.");
    }
    return handler.getPublishes();
  }

  /**
   * @return <code>true</code> if the publish job is completed
   */
  private static boolean isPublishCompleted(String status) {
    return "complete".equals(status) || "cancel".equals(status) || "error".equals(status) || "fail".equals(status);
  }
}
