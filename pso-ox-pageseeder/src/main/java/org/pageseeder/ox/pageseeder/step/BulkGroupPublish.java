package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.SimpleSiteException;
import net.pageseeder.app.simple.core.utils.SimpleXMLUtils;
import net.pageseeder.app.simple.pageseeder.model.PSPublish;
import net.pageseeder.app.simple.pageseeder.service.MemberService;
import net.pageseeder.app.simple.pageseeder.service.PublishService;
import net.pageseeder.app.simple.pageseeder.xml.PSPublishHandler;
import net.pageseeder.app.simple.vault.*;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.bridge.APIException;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.PSSession;
import org.pageseeder.bridge.berlioz.auth.Authenticator;
import org.pageseeder.bridge.berlioz.auth.PSAuthenticator;
import org.pageseeder.bridge.berlioz.auth.PSUser;
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
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    //TODO Start - While the publish does not accept token
    PSUser psUser = getUser(psconfigName);
    PSCredentials session = psUser.getSession();
    //It needs the session member because it may differ of the token.
    PSMember sessionMember = psUser.toMember();
    //TODO END - While the publish does not accept token

    writer.openElement("publishes");
    for (GroupPublish gp : groupPublishList) {
      try {
        callPublish(gp, member, credentials, sessionMember, session, psConfig, publish, writer);
      } catch (IOException e) {
        //TODO
      }
    }
    writer.closeElement();//publishes
    result.addExtraXML(new ExtraResultStringXML(writer.toString()));
    try {
      Files.write(
          StepUtils.getOutput(data, info, inputXml).toPath(), writer.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException exc) {
    LOGGER.error("Exception thrown while writing output to XML: {}", exc.toString());
  }
    return result;
  }

  private void callPublish(GroupPublish gp, PSMember member, PSCredentials credentials,
                           PSMember sessionMember, PSCredentials session, PSConfig psConfig,
                           PublishService publish, XMLWriter writer) throws IOException{
    String errorMessage = "";
    String status = "";
    try {
      writer.openElement("publish");
      String project = gp.getProject();
      PSGroup group = new PSGroup(project + "-" + gp.getGroup());
      PSMember member2 = gp.getMember().isEmpty() ? sessionMember : new PSMember(gp.getMember());
      String target = gp.getTarget();
      PublishService.Type type = gp.getType();
      PublishService.LogLevel logLevel = gp.getLogLevel();
      Map<String,String> scriptParameters = gp.getParameters();
      PSPublishHandler psPublishHandler = new PSPublishHandler();
      //This service needs the session instead of token
      publish.startGroupPublish(member2, group, project, target, type, logLevel, scriptParameters, session, psConfig,
            psPublishHandler);
      PSPublish currentPublish = psPublishHandler.get();

      status = currentPublish.getStatus();
      while (!isPublishCompleted(status)) {
        //check status till completed or error or failed
        //This service accepts the token.
        publish.checkPublish(member, psPublishHandler.get().getId(), credentials, psConfig, psPublishHandler);
        status = psPublishHandler.get().getStatus();
      }
      errorMessage = psPublishHandler.get().getMessage();
    } catch (IOException e) {
      errorMessage = e.getMessage();
      status = "exception";
    } finally {
      writer.attribute("project", gp.getProject());
      writer.attribute("group", gp.getGroup());
      writer.attribute("member", gp.getMember());
      writer.attribute("target", gp.getTarget());
      writer.attribute("type", gp.getType().name());
      writer.attribute("log-level", gp.getLogLevel() != null ? gp.getLogLevel().name() : "");
      writer.attribute("status", status);
      writer.attribute("error-msg", errorMessage);
      for (Map.Entry<String, String> entry : gp.getParameters().entrySet()) {
        writer.attribute(entry.getKey(), entry.getValue());
      }
      //extra param
      writer.closeElement();//publish
    }
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

  private PSUser getUser(String psconfigName){
    PSUser user = null;
    StringBuffer authProperties = new StringBuffer("bridge.");
    //If it is not the default, then append its name to the property
    if (!VaultUtils.getDefaultPSOAuthConfigName().equalsIgnoreCase(psconfigName)) {
      authProperties.append(psconfigName);
      authProperties.append(".");
    }
    authProperties.append("admin");
    String property = authProperties.toString();
    String username = GlobalSettings.get(property + ".username");
    String password = GlobalSettings.get(property + ".password");

    try {
      Authenticator<PSUser> authenticator = new PSAuthenticator();
      user = authenticator.login(username, password);
      if (user == null) {
        //todo to improve
        //LOGGER.warn("User config property '{}' not setup property - results in null user", property);
      }
    } catch (Exception ex) {
      //todo to improve
    }
    return user;
  }
}
