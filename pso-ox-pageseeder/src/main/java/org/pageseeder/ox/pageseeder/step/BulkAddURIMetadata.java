/*
 * Copyright 2023 Allette Systems (Australia)
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

import net.pageseeder.app.simple.core.utils.SimpleXMLUtils;
import net.pageseeder.app.simple.pageseeder.service.URIService;
import net.pageseeder.app.simple.vault.PSOAuthConfig;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.psml.Property;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.model.AddMetadata;
import org.pageseeder.ox.pageseeder.xml.AddMetadataHandler;
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
import java.util.concurrent.TimeUnit;

/**
 * Parameters
 *
 * - psconfig
 * - input *
 * - output *
 * - interval
 * - psgroup *
 * Note: * these fields are required
 *
 * @author ccabral
 * @since 11 January 2023
 */
public class BulkAddURIMetadata extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkAddURIMetadata.class);

  private float percentage = 0.0F;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    //Read request or step parameters
    String psconfigName = StepUtils.getParameter(data, info, "psconfig", VaultUtils.getDefaultPSOAuthConfigName());
    File inputXml = StepUtils.getInput(data, info);
    File output = StepUtils.getOutput(data, info, inputXml);
    long interval = StepUtils.getParameterLongWithoutDynamicLogic(data, info, "interval", 100);
    PSGroup psGroup = new PSGroup(StepUtils.getParameter(data, info, "psgroup", ""));
    //Initiate the result
    DefaultResult result = new DefaultResult(model, data, info, output);

    //Load the Pageseeder Configuration
    PSOAuthConfig psOAuthConfig = super.getPSOAuthConfig(psconfigName);
    PSConfig psConfig = psOAuthConfig.getConfig();

    if (inputXml != null && inputXml.exists()) {
      URIService uriService = new URIService();
      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

      //Start writing the xml metadatas result
      writer.openElement("metadatas");
      try {
        TokensVaultItem item = super.getTokensVaultItem(psconfigName);
        PSMember tokenMember = item.getMember();
        PSCredentials token = item.getToken();

        //Read input file and convert to a list of metadatas
        List<AddMetadata> addMetadataList = readXML(inputXml);
        //update percentage
        this.percentage = 5.0F;

        //It is 90% because 5% was left for getUser and another 5% to write output.
        float percentageIncrement = 90.0F / addMetadataList.size();
        // Iterate through metadata parameters and update metadatas
        for (AddMetadata addMetadata : addMetadataList) {
          callChange(addMetadata, tokenMember, psGroup, token, psConfig, uriService, writer);
          //Update Percentage
          this.percentage += percentageIncrement;
          if (interval > 0) {
            TimeUnit.MILLISECONDS.sleep(interval);
          }
        }
      } catch (IOException | InterruptedException ex) {
        LOGGER.error("Exception thrown while writing output to XML: {}", ex.getMessage());
        result.setError(ex);
      } finally {
        //Close metadatas result
        writer.closeElement();//metadatas

        //Add metadatas result to the final result.
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

  private void callChange(AddMetadata addMetadata, PSMember tokenMember, PSGroup psGroup, PSCredentials token,
                          PSConfig psConfig, URIService uriService, XMLWriter writer) throws IOException {
    String errorMessage = "";
    String status = "";
    long startedAt = System.currentTimeMillis();
    try {
      XMLStringWriter tempWriter = new XMLStringWriter(XML.NamespaceAware.No);
      uriService.addMetadata(addMetadata.getUriid(), tokenMember, psGroup,  addMetadata.getTitle(),
          addMetadata.getDescription(), addMetadata.getLabels(), addMetadata.getProperties(), token, psConfig, tempWriter);
      //Currently it does not use this information. So it will just log.
      LOGGER.debug(tempWriter.toString());
      status = "success";
    } catch (Exception ex) {
      status = "failed";
      errorMessage = ex.getMessage();
    } finally {
      writeChangesResultXML (addMetadata, writer, status, errorMessage, System.currentTimeMillis() - startedAt);
    }
  }

  private List<AddMetadata> readXML(File xml) throws IOException {
    //Create a new handler
    AddMetadataHandler handler = new AddMetadataHandler();
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.list();
  }

  private void writeChangesResultXML (AddMetadata addMetadata, XMLWriter writer, String status, String errorMessage, long timeSpentMilliseconds) throws IOException {
    writer.openElement("metadata");
    String uriid = addMetadata.getUriid() != null ? String.valueOf(addMetadata.getUriid()) : "-1";
    writer.element("uriid", uriid);
    writer.element("title", addMetadata.getTitle());
    writer.element("description", addMetadata.getDescription());
    writer.element("labels", String.join(",", addMetadata.getLabels()));

    writer.openElement("properties");
    for (Property property : addMetadata.getProperties()) {
      writer.openElement("property");
      writer.attribute("name", property.getName());
      writer.attribute("title", property.getTitle());
      writer.attribute("type", property.getType());
      writer.attribute("value", property.getValue());
      writer.closeElement();//close property
    }
    writer.closeElement();//close properties

    writer.element("status", status);
    writer.element("error-msg", errorMessage);
    writer.element("time-spent-milliseconds", String.valueOf(timeSpentMilliseconds));

    writer.closeElement();// close metadata
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
