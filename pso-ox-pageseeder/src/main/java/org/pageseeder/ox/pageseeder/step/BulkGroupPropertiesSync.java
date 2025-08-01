/*
 * Copyright 2024 Allette Systems (Australia)
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

import net.pageseeder.app.simple.core.SimpleSiteNotFoundException;
import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.core.utils.SimpleXMLUtils;
import net.pageseeder.app.simple.pageseeder.model.GroupOptions;
import net.pageseeder.app.simple.pageseeder.service.GroupService;
import net.pageseeder.app.simple.vault.PSOAuthConfig;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.TokensVaultManager;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.model.PSProject;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.model.GroupAndGroupOptionWrapper;
import org.pageseeder.ox.pageseeder.xml.GroupAndGroupOptionHandler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It creates a group or update its title, description.
 * @author Carlos Cabral
 * @since 19 November 2024
 */
public class BulkGroupPropertiesSync extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkGroupPropertiesSync.class);

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

    if (inputXml != null && inputXml.exists()) {
      GroupService groupService = new GroupService();
      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
      try {
        //Start writing the xml result
        writer.openElement("groups-sync");

        //Load the Pageseeder Configuration
        PSOAuthConfig psOAuthConfig = super.getPSOAuthConfig(psconfigName);
        PSConfig psConfig = psOAuthConfig.getConfig();

        // Load credentials for the desired Pageseeder Configuration
        TokensVaultItem item = TokensVaultManager.get(psconfigName);
        PSMember member = item.getMember();
        PSCredentials credentials = item.getToken();

        //Read input file and convert to a list
        List<GroupAndGroupOptionWrapper> groupList = readXML(inputXml);

        //update percentage
        this.percentage = 5.0F;

        //It is 90% because 5% was left for getUser and another 5% to write output.
        float percentageIncrement = 90.0F/groupList.size();

        //go through groups
        for (GroupAndGroupOptionWrapper gp : groupList) {
          callSync(gp, member, credentials, psConfig, groupService, writer);
          //Update Percentage
          this.percentage += percentageIncrement;
        }
      } catch (Exception ex) {
        LOGGER.error("Exception thrown while writing output to XML: {}", ex.getMessage());
        result.setError(ex);
      } finally {
        //Close groups-sync result
        writer.closeElement();//groups-sync

        //Add synchronization result to the final result.
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

  /**
   *
   * @param groupAndGroupOptionWrapper
   * @param editor
   * @param credentials
   * @param psConfig
   * @param groupService
   * @param writer
   * @throws IOException
   */
  private void callSync(GroupAndGroupOptionWrapper groupAndGroupOptionWrapper, PSMember editor, PSCredentials credentials,
                        PSConfig psConfig, GroupService groupService, XMLWriter writer) throws IOException {
    String errorMessage = "";
    String status = "";
    long startedAt = System.currentTimeMillis();
    PSGroup groupSynchronized = null;
    try {

      PSGroup group = groupAndGroupOptionWrapper.getGroup();
      GroupOptions options = groupAndGroupOptionWrapper.getGroupOptions();

      // Group/Project Exist
      PSGroup groupExists = getGroup(group, groupService, psConfig, credentials);
      if (groupExists != null) {
        //This group/project exists, then just update
        group.setId(groupExists.getId());
        groupSynchronized = groupService.edit(group, editor, options, credentials, psConfig);
        status = "updated";
      } else {
        //This group/project does not exist, then just create
        if(SimpleStringUtils.isBlank(group.getParentName())) {
          //It is project
          groupSynchronized = groupService.createProject(toProject(group), editor, toProjectParameters(options),
              credentials, psConfig);
        } else {
          //It is a group
          groupSynchronized = groupService.create(group, editor, options, credentials, psConfig);
        }
        status = "created";
      }

    } catch (Exception e) {
      errorMessage = e.getMessage();
      status = "exception";
    } finally {
      writeResultXML(groupSynchronized, writer, status, errorMessage, System.currentTimeMillis() - startedAt);
    }
  }

  private PSGroup getGroup(PSGroup group, GroupService service, PSConfig psConfig, PSCredentials credentials) {
    PSGroup groupFound = null;
    try {
      groupFound = service.get(group.getName(), credentials, psConfig);
    } catch (SimpleSiteNotFoundException e) {
      LOGGER.info("Group not found: {}", group.getName());
    }
    return groupFound;
  }

  private List<GroupAndGroupOptionWrapper> readXML(File xml) throws IOException {
    GroupAndGroupOptionHandler handler = new GroupAndGroupOptionHandler();
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.list();
  }

  private void writeResultXML(PSGroup group, XMLWriter writer, String status, String errorMessage, long timeSpentMilliseconds) throws IOException {


    writer.openElement("group");
    writer.attribute("id", Long.toString(group.getId()));
    writer.attribute("name", group.getName());
    writer.attribute("short-name", group.getShortName());
    writer.attribute("title", group.getTitle());
    writer.attribute("description", group.getDescription());
    writer.attribute("owner", group.getOwner());

    writer.attribute("status", status);

    writer.attribute("error-msg", SimpleStringUtils.isBlank(errorMessage) ? "" : errorMessage);

    writer.attribute("time-spent-milliseconds", String.valueOf(timeSpentMilliseconds));
    writer.closeElement();//group
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

  private PSProject toProject(PSGroup group) {
    PSProject project = new PSProject();
    project.setId(group.getId());
    project.setName(group.getName());
    project.setDescription(group.getDescription());
    project.setOwner(group.getOwner());
    project.setDefaultNotification(group.getDefaultNotification());
    project.setDefaultRole(group.getDefaultRole());
    project.setDetailsType(group.getDetailsType());
    project.setTemplate(group.getTemplate());
    project.setTitle(group.getTitle());
    return project;
  }

  private Map<String, String> toProjectParameters(GroupOptions options) {
    Map<String, String> parameters = new HashMap<>();
    if (options.getAccess() != null) {
      parameters.put("access", options.getAccess());
    }

    if (options.getMessage() != null) {
      parameters.put("message", options.getMessage());
    }

    if (options.getRelatedurl() != null) {
      parameters.put("relatedurl", options.getRelatedurl());
    }

    if (options.getVisibility() != null) {
      parameters.put("visibility", options.getVisibility());
    }

    return parameters;
  }

  public int percentage() {
    return (int)this.percentage;
  }
}
