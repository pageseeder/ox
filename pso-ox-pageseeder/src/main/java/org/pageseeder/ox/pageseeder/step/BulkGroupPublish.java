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

import net.pageseeder.app.simple.core.utils.SimpleXMLUtils;
import net.pageseeder.app.simple.pageseeder.service.PublishService;
import net.pageseeder.app.simple.pageseeder.xml.PSPublishHandler;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.PSCredentials;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.pageseeder.model.PublishRequest;
import org.pageseeder.ox.pageseeder.xml.PublishRequestInputHandler;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Pipeline step executing batch group publishing requests specified in an input XML file.
 *
 * @author Carlos Cabral
 * @since 1 October  2026
 */
public class BulkGroupPublish extends AbstractPublishStep {

  private PublishRequest currentBatchRequest;

  @Override
  protected void executePublish(
      Model model,
      PackageData data,
      StepInfo info,
      PSMember sessionMember,
      PSCredentials session,
      PSConfig psConfig,
      long interval,
      PublishService publishService,
      XMLWriter writer
  ) throws Exception {
    File inputXml = StepUtils.getInput(data, info);
    if (inputXml == null || !inputXml.exists()) {
      throw new FileNotFoundException("Input XML configuration file not found.");
    }

    List<PublishRequest> requests = readXml(inputXml);
    if (requests.isEmpty()) {
      return;
    }

    float percentageIncrement = 90.0F / requests.size();
    for (PublishRequest request : requests) {
      this.currentBatchRequest = request;
      performPublishAndPoll(request, sessionMember, session, psConfig, interval, publishService, writer);
      this.percentage += percentageIncrement;
    }
  }

  @Override
  protected void startPublishOperation(
      PublishRequest request,
      PSMember sessionMember,
      PSCredentials session,
      PSConfig psConfig,
      PublishService publishService,
      PSPublishHandler handler
  ) throws Exception {
    PublishRequest req = (request != null) ? request : this.currentBatchRequest;
    PSGroup group = new PSGroup(req.getGroup());
    PSMember member = req.getMember().isBlank() ? sessionMember : new PSMember(req.getMember());

    publishService.startGroupPublish(
        member,
        group,
        req.getProject(),
        req.getTarget(),
        req.getType(),
        req.getLogLevel(),
        req.getParameters(),
        session,
        psConfig,
        handler
    );
  }

  private List<PublishRequest> readXml(File xml) throws IOException {
    PublishRequestInputHandler handler = new PublishRequestInputHandler();
    SimpleXMLUtils.parseXML(new FileInputStream(xml), handler);
    return handler.getPublishes();
  }
}