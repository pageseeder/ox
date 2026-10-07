/*
 * Copyright 2026 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
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
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.ox.api.Measurable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.model.PublishRequest;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for PageSeeder publishing pipeline steps.
 * Provides unified authentication, status polling, XML generation, and error handling.
 *
 * @author Carlos Cabral
 * @since 1 October
 */
public abstract class AbstractPublishStep extends PageseederStep implements Measurable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPublishStep.class);

  protected float percentage = 0.0F;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    String psconfigName = StepUtils.getParameter(data, info, "psconfig", VaultUtils.getDefaultPSOAuthConfigName());
    File input = StepUtils.getInput(data, info);
    File output = StepUtils.getOutput(data, info, input);
    long interval = StepUtils.getParameterLongWithoutDynamicLogic(data, info, "interval", 100L);

    DefaultResult result = new DefaultResult(model, data, info, output);

    PSOAuthConfig psOAuthConfig = super.getPSOAuthConfig(psconfigName);
    PSConfig psConfig = psOAuthConfig.getConfig();
    PSAuthenticator psAuthenticator = super.getPSAuthenticator(psConfig);

    PublishService publishService = new PublishService();
    XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);

    writer.openElement("publishes");
    PSUser psUser = null;
    try {
      // Load credentials for the desired Pageseeder Configuration
      // The token can only be used in Pageseeder with version greater than 6. However as it still have
      // some 5. It is necessary to keep the compatibility.
      // TokensVaultItem item = TokensVaultManager.get(psconfigName);
      // PSMember member = item.getMember();
      // PSCredentials credentials = item.getToken();
      //TODO Start - While the publish does not accept token
      psUser = super.getAdminUser(psconfigName, psAuthenticator);
      PSCredentials session = psUser.getSession();
      //It needs the session member because it may differ of the token.
      PSMember sessionMember = psUser.toMember();
      //TODO END - While the publish does not accept token

      this.percentage = 5.0F;

      executePublish(model, data, info, sessionMember, session, psConfig, interval, publishService, writer);

    } catch (Exception ex) {
      LOGGER.error("Error executing publish step: {}", ex.getMessage(), ex);
      result.setError(ex);
    } finally {
      if (psUser != null) {
        try {
          super.logout(psUser, psAuthenticator);
        } catch (AuthException ex) {
          LOGGER.error("Error logging out user: {}", ex.getMessage(), ex);
        }
      }
      writer.closeElement(); // </publishes>

      result.addExtraXML(new ExtraResultStringXML(writer.toString()));
      writeOutput(output, writer, result);
      this.percentage = 100.0F;
    }

    return result;
  }

  /**
   * Template method to perform step-specific publishing operations.
   */
  protected abstract void executePublish(
      Model model,
      PackageData data,
      StepInfo info,
      PSMember sessionMember,
      PSCredentials session,
      PSConfig psConfig,
      long interval,
      PublishService publishService,
      XMLWriter writer
  ) throws Exception;

  /**
   * Initiates the publishing request, polls until completion, and writes XML results.
   */
  protected void performPublishAndPoll(
      PublishRequest request,
      PSMember sessionMember,
      PSCredentials session,
      PSConfig psConfig,
      long interval,
      PublishService publishService,
      XMLWriter writer
  ) throws IOException {
    String errorMessage = "";
    String status = "";
    long startedAt = System.currentTimeMillis();

    try {
      PSPublishHandler psPublishHandler = new PSPublishHandler();

      startPublishOperation(request, sessionMember, session, psConfig, publishService, psPublishHandler);

      PSPublish currentPublish = psPublishHandler.get();
      status = currentPublish != null ? currentPublish.getStatus() : "unknown";

      while (!isPublishCompleted(status)) {
        TimeUnit.MILLISECONDS.sleep(interval);
        if (currentPublish != null) {
          publishService.checkPublish(sessionMember, currentPublish.getId(), session, psConfig, psPublishHandler);
          currentPublish = psPublishHandler.get();
          status = currentPublish != null ? currentPublish.getStatus() : "unknown";
        } else {
          status = "error";
          errorMessage = "Received null publish response during polling.";
          break;
        }
      }

      if (currentPublish != null && SimpleStringUtils.isBlank(errorMessage)) {
        errorMessage = currentPublish.getMessage();
      }

    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      errorMessage = "Publish operation was interrupted: " + ie.getMessage();
      status = "interrupted";
    } catch (Exception e) {
      LOGGER.error("Failed executing publish operation: {}", e.getMessage(), e);
      errorMessage = e.getMessage();
      status = "exception";
    } finally {
      writePublishResultXML(request, writer, status, errorMessage, System.currentTimeMillis() - startedAt);
    }
  }

  /**
   * Hook for subclasses to execute their specific `startGroupPublish` or `startUriPublish` call.
   */
  protected abstract void startPublishOperation(
      PublishRequest request,
      PSMember sessionMember,
      PSCredentials session,
      PSConfig psConfig,
      PublishService publishService,
      PSPublishHandler handler
  ) throws Exception;

  protected static boolean isPublishCompleted(String status) {
    return "complete".equals(status) || "cancel".equals(status) || "error".equals(status) || "fail".equals(status);
  }

  protected void writePublishResultXML(
      PublishRequest request,
      XMLWriter writer,
      String status,
      String errorMessage,
      long timeSpentMs
  ) throws IOException {
    writer.openElement("publish");
    writer.attribute("project", request.getProject());
    writer.attribute("group", request.getGroup());
    writer.attribute("member", request.getMember());
    writer.attribute("target", request.getTarget());
    writer.attribute("type", request.getType().name());
    writer.attribute("log-level", request.getLogLevel() != null ? request.getLogLevel().name() : "");

    if (!request.getUriId().isBlank()) {
      writer.attribute("uriid", request.getUriId());
    }
    if (!request.getDocumentType().isBlank()) {
      writer.attribute("documenttype", request.getDocumentType());
    }

    for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
      writer.attribute(entry.getKey(), entry.getValue());
    }

    writer.attribute("status", status);
    writer.attribute("error-msg", SimpleStringUtils.isBlank(errorMessage) ? "" : errorMessage);
    writer.attribute("time-spent-milliseconds", String.valueOf(timeSpentMs));
    writer.closeElement(); // </publish>
  }

  protected void writeOutput(File output, XMLWriter writer, ResultBase result) {
    try {
      if (output != null) {
        Files.write(output.toPath(), writer.toString().getBytes(StandardCharsets.UTF_8));
      } else {
        LOGGER.info("No output target specified; skipping file writing.");
      }
    } catch (IOException ex) {
      LOGGER.error("Failed writing output XML file: {}", ex.getMessage(), ex);
      result.setError(ex);
    }
  }

  @Override
  public int percentage() {
    return (int) this.percentage;
  }
}