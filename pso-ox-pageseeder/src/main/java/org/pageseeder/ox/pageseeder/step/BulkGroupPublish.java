/*
 * Copyright 2022 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.core.utils.SimpleXMLUtils;
import net.pageseeder.app.simple.pageseeder.model.PSPublish;
import net.pageseeder.app.simple.pageseeder.service.PublishService;
import net.pageseeder.app.simple.pageseeder.xml.PSPublishHandler;
import net.pageseeder.app.simple.vault.PSOAuthConfig;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.berlioz.auth.AuthException;
import org.pageseeder.bridge.berlioz.auth.PSAuthenticator;
import org.pageseeder.bridge.berlioz.auth.PSUser;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.model.GroupPublish;
import org.pageseeder.ox.pageseeder.xml.GroupPublishInputHandler;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author asantos
 * @since 12 July 2022
 */
public class BulkGroupPublish extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkGroupPublish.class);

  private float percentage = 0.0F;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    //Read request or step parameters
    String psconfigName = StepUtils.getParameter(data, info, "psconfig", VaultUtils.getDefaultPSOAuthConfigName());
    File inputXml = StepUtils.getInput(data, info);
    File output = StepUtils.getOutput(data, info, inputXml);
    long interval = StepUtils.getParameterLongWithoutDynamicLogic(data, info, "interval", 100L);

    //Initiate the result
    DefaultResult result = new DefaultResult(model, data, info, output);

    //Load the Pageseeder Configuration
    PSOAuthConfig psOAuthConfig = super.getPSOAuthConfig(psconfigName);
    PSConfig psConfig = psOAuthConfig.getConfig();
    PSAuthenticator psAuthenticator = super.getPSAuthenticator(psConfig);

    if (inputXml != null && inputXml.exists()) {
      PublishService publish = new PublishService();
      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

      //Start writing the xml publishes result
      writer.openElement("publishes");
      try {
        // Load credentials for the desired Pageseeder Configuration
        // The token can only be used in Pageseeder with version greater than 6. However as it still have
        // some 5. It is necessary to keep the compatibility.
        // TokensVaultItem item = TokensVaultManager.get(psconfigName);
        // PSMember member = item.getMember();
        // PSCredentials credentials = item.getToken();
        //TODO Start - While the publish does not accept token
        PSUser psUser = super.getAdminUser(psconfigName, psAuthenticator);
        PSCredentials session = psUser.getSession();
        //It needs the session member because it may differ of the token.
        PSMember sessionMember = psUser.toMember();
        //TODO END - While the publish does not accept token

        //Read input file and convert to a list of GroupPublish
        List<GroupPublish> groupPublishList = readXML(inputXml);

        //update percentage
        this.percentage = 5.0F;

        //It is 90% because 5% was left for getUser and another 5% to write output.
        float percentageIncrement = 90.0F/groupPublishList.size();

        //go through publish groups and call the publish and check the status till it is concluded(Completed or failed)
        for (GroupPublish gp : groupPublishList) {
          callPublish(gp, sessionMember, session, psConfig, interval, publish, writer);
          //Update Percentage
          this.percentage += percentageIncrement;
        }
        super.logout(psUser, psAuthenticator);
      } catch (IOException | AuthException ex) {
        LOGGER.error("Exception thrown while writing output to XML: {}", ex.getMessage());
        result.setError(ex);
      } finally {
        //Close publishes result
        writer.closeElement();//publishes

        //Add publishes result to the final result.
        result.addExtraXML(new ExtraResultStringXML(writer.toString()));

        //Writes the output.
        writeOutput(output, writer, result);
        this.percentage = 100.0F;
      }
    } else {
      LOGGER.error("Invalid input file.");
      result.setError(new FileNotFoundException("Cannot find the input file."));
    }
    return result;
  }

  private void callPublish(GroupPublish gp, PSMember sessionMember, PSCredentials session, PSConfig psConfig,
                           long interval, PublishService publish, XMLWriter writer) throws IOException {
    String errorMessage = "";
    String status = "";
    long startedAt = System.currentTimeMillis();
    try {
      //Preparer the parameters to call the publish
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

      //Get the first status.
      status = currentPublish.getStatus();

      //check status till completed or error or failed
      while (!isPublishCompleted(status)) {
        //Before checking, it will make a pause
        TimeUnit.MILLISECONDS.sleep(interval);
        //This service accepts the token.
        publish.checkPublish(sessionMember, psPublishHandler.get().getId(), session, psConfig, psPublishHandler);
        status = psPublishHandler.get().getStatus();
      }

      //Get messages if there are some.
      errorMessage = psPublishHandler.get().getMessage();

    } catch (IOException | InterruptedException e) {
      errorMessage = e.getMessage();
      status = "exception";
    } finally {
      writePublishResultXML (gp, writer, status, errorMessage, System.currentTimeMillis() - startedAt);
    }
  }

  private List<GroupPublish> readXML(File xml) throws IOException {
    GroupPublishInputHandler handler = new GroupPublishInputHandler();
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.getPublishes();
  }

  /**
   * @return <code>true</code> if the publish job is completed
   */
  private static boolean isPublishCompleted(String status) {
    return "complete".equals(status) || "cancel".equals(status) || "error".equals(status) || "fail".equals(status);
  }

  private void writePublishResultXML (GroupPublish gp, XMLWriter writer, String status, String errorMessage, long timeSpentMilliseconds) throws IOException {

    writer.openElement("publish");
    writer.attribute("project", gp.getProject());
    writer.attribute("group", gp.getGroup());
    writer.attribute("member", gp.getMember());
    writer.attribute("target", gp.getTarget());
    writer.attribute("type", gp.getType().name());
    writer.attribute("log-level", gp.getLogLevel() != null ? gp.getLogLevel().name() : "");

    //Extra parameters
    for (Map.Entry<String, String> entry : gp.getParameters().entrySet()) {
      writer.attribute(entry.getKey(), entry.getValue());
    }

    writer.attribute("status", status);

    writer.attribute("error-msg", SimpleStringUtils.isBlank(errorMessage) ? "" : errorMessage);

    writer.attribute("time-spent-milliseconds", String.valueOf(timeSpentMilliseconds));
    writer.closeElement();//publish
  }

  private void writeOutput(File output, XMLWriter writer, ResultBase result) {
    try {
      if (output != null) {
        Files.write(output.toPath(), writer.toString().getBytes(StandardCharsets.UTF_8));
      } else {
        LOGGER.info("There is not output to write");
      }
    } catch (IOException ex) {
      LOGGER.error("Exception thrown while writing output to XML: {}", ex.toString());
      result.setError(ex);
    }
  }

  public int percentage() {
    return (int)this.percentage;
  }
}
