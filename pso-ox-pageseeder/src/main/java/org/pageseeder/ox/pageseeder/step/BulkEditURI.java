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
import net.pageseeder.app.simple.pageseeder.model.EditURI;
import net.pageseeder.app.simple.pageseeder.service.URIService;
import net.pageseeder.app.simple.vault.PSOAuthConfig;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.xml.EditURIHandler;
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
 * @since 18 January 2023
 */
public class BulkEditURI extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkEditURI.class);

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

      //Start writing the xml edits result
      writer.openElement("edit-uris");
      try {
        TokensVaultItem item = super.getTokensVaultItem(psconfigName);
        PSMember tokenMember = item.getMember();
        PSCredentials token = item.getToken();

        //Read input file and convert to a list of EditURI
        List<EditURI> editURIList = readXML(tokenMember, psGroup, inputXml);
        //update percentage
        this.percentage = 5.0F;

        //It is 90% because 5% was left for getUser and another 5% to write output.
        float percentageIncrement = 90.0F / editURIList.size();
        // Iterate through metadata parameters and update metadatas
        for (EditURI editURI : editURIList) {
          callChange(editURI, token, psConfig, uriService, writer);
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
        //Close edits result
        writer.closeElement();//edit-uris

        //Add edits result to the final result.
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

  private void callChange(EditURI editURI, PSCredentials token, PSConfig psConfig, URIService uriService,
                          XMLWriter writer) throws IOException {
    String errorMessage = "";
    String status = "";
    long startedAt = System.currentTimeMillis();
    try {
      XMLStringWriter tempWriter = new XMLStringWriter(XML.NamespaceAware.No);
      uriService.edit(editURI, token, psConfig, tempWriter);
      //Currently it does not use this information. So it will just log.
      LOGGER.debug(tempWriter.toString());
      status = "success";
    } catch (Exception ex) {
      status = "failed";
      errorMessage = ex.getMessage();
    } finally {
      writeChangesResultXML (editURI, writer, status, errorMessage, System.currentTimeMillis() - startedAt);
    }
  }

  private List<EditURI> readXML(PSMember member, PSGroup group, File xml) throws IOException {
    //Create a new handler
    EditURIHandler handler = new EditURIHandler(member, group);
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.list();
  }

  private void writeChangesResultXML (EditURI editURI, XMLWriter writer, String status, String errorMessage,
                                      long timeSpentMilliseconds) throws IOException {
    writer.openElement("edit-uri");
    String uriid = editURI.getUriid() != null ? String.valueOf(editURI.getUriid()) : "-1";
    writer.element("uriid", uriid);
    SimpleXMLUtils.writeElement("description", editURI.getDescription(), writer);
    SimpleXMLUtils.writeElement("document-id", editURI.getDocumentId(), writer);
    SimpleXMLUtils.writeElement("labels", editURI.getLabels(), writer);
    SimpleXMLUtils.writeElement("file-name", editURI.getFileName(), writer);
    SimpleXMLUtils.writeElement("publication-id", editURI.getPublicationId(), writer);
    SimpleXMLUtils.writeElement("publication-type", editURI.getPublicationType(), writer);
    SimpleXMLUtils.writeElement("title", editURI.getTitle(), writer);

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
