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
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.xmlwriter.XMLWriter;

import java.util.Map;

/**
 * Pipeline step executing a single PageSeeder Start Group Publish request.
 *
 * @author Carlos Cabral
 * @since 1 October  2026
 */
public class CallGroupPublish extends AbstractPublishStep {

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
    PublishRequest request = loadRequestFromStepParameters(data, info);
    performPublishAndPoll(request, sessionMember, session, psConfig, interval, publishService, writer);
    this.percentage = 90.0F;
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
    PSGroup group = new PSGroup(request.getGroup());
    PSMember member = request.getMember().isBlank() ? sessionMember : new PSMember(request.getMember());

    publishService.startGroupPublish(
        member,
        group,
        request.getProject(),
        request.getTarget(),
        request.getType(),
        request.getLogLevel(),
        request.getParameters(),
        session,
        psConfig,
        handler
    );
  }

  private PublishRequest loadRequestFromStepParameters(PackageData data, StepInfo info) {
    String project = StepUtils.getParameter(data, info, "project", "");
    String group = StepUtils.getParameter(data, info, "group", "");
    String member = StepUtils.getParameter(data, info, "member", "");
    String target = StepUtils.getParameter(data, info, "target", "");
    String type = StepUtils.getParameter(data, info, "type", "PROCESS");
    String logLevel = StepUtils.getParameter(data, info, "log-level", "INFO");
    Map<String, String> scriptParams = StepUtils.getParametersStartingWith(data, info, "ps-param-");

    return new PublishRequest.Builder()
        .project(project)
        .group(group)
        .member(member)
        .target(target)
        .type(type)
        .logLevel(logLevel)
        .parameters(scriptParams)
        .build();
  }
}