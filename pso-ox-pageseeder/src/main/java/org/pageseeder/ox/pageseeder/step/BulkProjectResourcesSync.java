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
import net.pageseeder.app.simple.pageseeder.service.ResourceService;
import net.pageseeder.app.simple.vault.PSOAuthConfig;
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
import org.pageseeder.ox.pageseeder.model.GroupResourcesSync;
import org.pageseeder.ox.pageseeder.xml.GroupAndGroupOptionHandler;
import org.pageseeder.ox.pageseeder.xml.GroupResourcesSyncHandler;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.tool.ExtraResultStringXML;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.StringUtils;
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
 * Export the project resources from one pageseeder and project and import to another PageSeeder and project or
 * to the same PageSeeder but different project
 *
 * input: The xml with the projects to export and with the destination projects.
 * output: The xml with the results fo this process
 * output-resources: the folder to store the resources zip files while exporting and importing. If it is not informed. It will
 * save in the folder resources.
 * from-psconfig: The Pageseeder where the resources will be exported. If empty it will use the default.
 * to-psconfig: The Pageseeder where the resources will be imported. If empty it will be the same of from-psconfig.
 *
 * @author Carlos Cabral
 * @since 26 November 2024
 */
public class BulkProjectResourcesSync extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkProjectResourcesSync.class);

  private float percentage = 0.0F;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    //Read request or step parameters
    String fromPSConfigName = StepUtils.getParameter(data, info, "from-psconfig", VaultUtils.getDefaultPSOAuthConfigName());
    String toPSConfigName = StepUtils.getParameter(data, info, "to-psconfig", fromPSConfigName);

    File inputXml = StepUtils.getInput(data, info);
    File outputXml = StepUtils.getOutput(data, info, inputXml);
    File outputResources = getOutput(data, info);


    //Initiate the result
    DefaultResult result = new DefaultResult(model, data, info, outputXml);

    if (inputXml != null && inputXml.exists()) {
      ResourceService resourceService = new ResourceService();

      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
      try {
        //Start writing the xml result
        writer.openElement("project-resources-sync");

        //Load the Pageseeder Configuration
        PSConfig fromPSConfig = super.getPSOAuthConfig(fromPSConfigName).getConfig();
        PSConfig toPSConfig = super.getPSOAuthConfig(toPSConfigName).getConfig();
        // Load credentials for the desired Pageseeder Configuration
        PSCredentials fromCredentials = TokensVaultManager.get(fromPSConfigName).getToken();
        PSCredentials toCredentials = TokensVaultManager.get(toPSConfigName).getToken();

        //Read input file and convert to a list
        List<GroupResourcesSync> groupResourcesList = readXML(inputXml);

        //update percentage
        this.percentage = 5.0F;

        //It is 90% because 5% was left for getUser and another 5% to write output.
        float percentageIncrement = 90.0F/groupResourcesList.size();

        //go through groups
        for (GroupResourcesSync groupResourcesSync : groupResourcesList) {
          callSync(groupResourcesSync, outputResources, fromCredentials, fromPSConfig, toCredentials, toPSConfig,
              resourceService, writer);
          //Update Percentage
          this.percentage += percentageIncrement;
        }
      } catch (Exception ex) {
        LOGGER.error("Exception thrown while writing output to XML: {}", ex.getMessage());
        result.setError(ex);
      } finally {
        //Close groups-sync result
        writer.closeElement();//project-resources-sync

        //Add synchronization result to the final result.
        result.addExtraXML(new ExtraResultStringXML(writer.toString()));

        //Writes the output.
        writeOutput(outputXml, writer, result);
        this.percentage = 100.0F;
      }
    } else {
      LOGGER.error("Invalid input file.");
      result.setError(new FileNotFoundException("Cannot find the input file."));
    }
    return result;
  }


  private void callSync(GroupResourcesSync groupResourcesSync, File outputFolder, PSCredentials fromCredentials,
                        PSConfig fromPSConfig, PSCredentials toCredentials, PSConfig toPSConfig,
                        ResourceService resourceService, XMLWriter writer) throws IOException {
    String errorMessage = "";
    String exportedStatus = "not-initiated";
    String importStatus = "not-initiated";
    String status = "not-initiated";
    File resourceZip = null;
    XMLStringWriter importWriter = new XMLStringWriter(XML.NamespaceAware.No);
    long startedAt = System.currentTimeMillis();
    try {
      status = "initiated";
      LOGGER.debug("Starts export from {}", groupResourcesSync.getFromProjectName());
      exportedStatus = "initiated";
      resourceZip = resourceService.export(groupResourcesSync.getFromProjectName(),
          groupResourcesSync.getFromPerspective(), outputFolder, fromPSConfig, fromCredentials);
      exportedStatus = "exported";
      LOGGER.debug("Finished export from {}", groupResourcesSync.getFromProjectName());

      LOGGER.debug("Starts import to {}", groupResourcesSync.getToProjectName());
      importStatus = "initiated";
      resourceService.importZip(groupResourcesSync.getToProjectName(), resourceZip, true, importWriter,
          toPSConfig, toCredentials);
      importStatus = "imported";

      status = "concluded";
    } catch (Exception e) {
      errorMessage = e.getMessage();
      status = "exception";
    } finally {
      writeResultXML(groupResourcesSync, resourceZip, writer, exportedStatus, importStatus, status, errorMessage,
          System.currentTimeMillis() - startedAt, importWriter.toString());
    }
  }

  private List<GroupResourcesSync> readXML(File xml) throws IOException {
    GroupResourcesSyncHandler handler = new GroupResourcesSyncHandler();
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.list();
  }

  private void writeResultXML(GroupResourcesSync groupResourcesSync, File exportedFile, XMLWriter writer,
                              String exportStatus, String importStatus, String status,
                              String errorMessage, long timeSpentMilliseconds, String xmlImport) throws IOException {


    writer.openElement("project");
    writer.attribute("from-name", groupResourcesSync.getFromProjectName());
    writer.attribute("from-ps-config-name", groupResourcesSync.getFromPSConfigName());
    writer.attribute("from-perspective", groupResourcesSync.getFromPerspective());
    writer.attribute("to-name", groupResourcesSync.getToProjectName());
    writer.attribute("to-ps-config-name", groupResourcesSync.getToPSConfigName());
    writer.attribute("filename", exportedFile != null ? exportedFile.getName() : "");

    writer.attribute("export-status", exportStatus);
    writer.attribute("import-status", importStatus);
    writer.attribute("status", status);

    writer.attribute("error-msg", SimpleStringUtils.isBlank(errorMessage) ? "" : errorMessage);

    writer.attribute("time-spent-milliseconds", String.valueOf(timeSpentMilliseconds));
    if (!SimpleStringUtils.isBlank(xmlImport)) {
      writer.writeXML(xmlImport);
    }
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

  /**
   * Get the output from step definition.
   * It accepts glob pattern.
   *
   * @param data
   * @param info
   * @return
   */
  public static File getOutput(PackageData data, StepInfo info) {
    // output file
    String output = info.getParameter("output-resources", "resources");
    File foutput = data.getFile(StepUtils.applyDynamicParameterLogic(data, info, output));

    if (foutput != null && !foutput.exists()) {
      boolean created = foutput.mkdirs();
      LOGGER.info("Created output directory {}: {}", created, foutput.getAbsolutePath());
    }

    return foutput;
  }

  public int percentage() {
    return (int)this.percentage;
  }
}
