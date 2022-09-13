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
import net.pageseeder.app.simple.pageseeder.model.CommentParameter;
import net.pageseeder.app.simple.pageseeder.service.CommentService;
import net.pageseeder.app.simple.vault.PSOAuthConfig;
import net.pageseeder.app.simple.vault.TokensVaultItem;
import net.pageseeder.app.simple.vault.VaultUtils;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.xml.PSCommentHandler;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.xml.CommentParameterHandler;
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

/**
 * @author ccabral
 * @since 2 September 2022
 */
public class BulkCommentChange extends PageseederStep implements Measurable {

  private static Logger LOGGER = LoggerFactory.getLogger(BulkCommentChange.class);

  private float percentage = 0.0F;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    //Read request or step parameters
    String psconfigName = StepUtils.getParameter(data, info, "psconfig", VaultUtils.getDefaultPSOAuthConfigName());
    File inputXml = StepUtils.getInput(data, info);
    File output = StepUtils.getOutput(data, info, inputXml);

    //Initiate the result
    DefaultResult result = new DefaultResult(model, data, info, output);

    //Load the Pageseeder Configuration
    PSOAuthConfig psOAuthConfig = super.getPSOAuthConfig(psconfigName);
    PSConfig psConfig = psOAuthConfig.getConfig();

    if (inputXml != null && inputXml.exists()) {
      CommentService commentService = new CommentService();
      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

      //Start writing the xml comments result
      writer.openElement("comments");
      try {
        TokensVaultItem item = super.getTokensVaultItem(psconfigName);
        PSMember tokenMember = item.getMember();
        PSCredentials token = item.getToken();

        //Read input file and convert to a list of CommentParameters
        List<CommentParameter> commentParameters = readXML(inputXml);
        //update percentage
        this.percentage = 5.0F;

        //It is 90% because 5% was left for getUser and another 5% to write output.
        float percentageIncrement = 90.0F / commentParameters.size();
        // Iterate through comment parameters and update comments
        for (CommentParameter cp : commentParameters) {
          callChange(cp, tokenMember, token, psConfig,commentService, writer);
          //Update Percentage
          this.percentage += percentageIncrement;
        }
      } catch (IOException ex) {
        LOGGER.error("Exception thrown while writing output to XML: {}", ex.getMessage());
        result.setError(ex);
      } finally {
        //Close comments result
        writer.closeElement();//comments

        //Add comments result to the final result.
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

  private void callChange(CommentParameter cp, PSMember tokenMember, PSCredentials token, PSConfig psConfig,
                           CommentService commentService, XMLWriter writer) throws IOException {
    String errorMessage = "";
    String status = "";
    long startedAt = System.currentTimeMillis();
    try {
      PSCommentHandler handler = new PSCommentHandler();
      commentService.editComment(cp.getId(), tokenMember, token, cp, psConfig, handler);
      status = "success";
    } catch (Exception ex) {
      status = "failed";
      errorMessage = ex.getMessage();
    } finally {
      writeChangesResultXML (cp, writer, status, errorMessage, System.currentTimeMillis() - startedAt);
    }
  }

  private List<CommentParameter> readXML(File xml) throws IOException {
    //Create a new handler
    CommentParameterHandler handler = new CommentParameterHandler();
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.getCommentParameters();
  }


  private void writeChangesResultXML (CommentParameter cp, XMLWriter writer, String status, String errorMessage, long timeSpentMilliseconds) throws IOException {
    writer.openElement("comment");
    writer.attribute("commentid", String.valueOf(cp.getId()));
    writer.attribute("title", cp.getTitle());
    if (cp.getContent() != null) {
      writer.attribute("content", cp.getContent().getContent());
    }
    if (cp.getContentRole() != null) {
      writer.attribute("contentType", cp.getContentRole());
    }
    if (cp.getLabels() != null) {
      writer.attribute("labels", cp.getLabels().toString());
    }
    if (cp.getProperties() != null) {
      writer.attribute("properties", cp.getProperties().toString());
    }
    if (cp.getNotify() != null) {
      writer.attribute("notify", cp.getNotify().parameter());
    }
    if (!SimpleStringUtils.isBlank(cp.getType())) {
      writer.attribute("type", cp.getType());
    }

    writer.attribute("status", status);
    writer.attribute("error-msg", errorMessage);
    writer.attribute("time-spent-milliseconds", String.valueOf(timeSpentMilliseconds));
    writer.closeElement();// close comment
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
