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
package org.pageseeder.ox.pageseeder.model;

import net.pageseeder.app.simple.pageseeder.service.PublishService;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author asantos
 * @since 12 July 2022
 */
public class GroupPublish implements XMLWritable {

  private String project;
  private String group;

  private String member;
  private String target;
  private PublishService.Type type;
  private PublishService.LogLevel logLevel;
  private HashMap<String, String> parameters;

  private String errorMessage;

  public GroupPublish(String project, String group, String member, String target, String type, String logLevel, HashMap<String, String> parameters) {
    this.project = project;
    this.group = group;
    this.member = member;
    this.target = target;
    this.type = PublishService.Type.valueOf(type.toUpperCase());
    this.logLevel = PublishService.LogLevel.valueOf(logLevel.toUpperCase());
    this.parameters = parameters;
  }

  public String getProject() {
    return project;
  }

  public String getGroup() {
    return group;
  }

  public String getMember() {
    return member;
  }

  public String getTarget() {
    return target;
  }

  public PublishService.Type getType() {return type; }

  public PublishService.LogLevel getLogLevel() {
    return logLevel;
  }

  public HashMap<String, String> getParameters() { return parameters;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("action");
    xml.attribute("project", this.getProject());
    xml.attribute("group", this.getGroup());
    xml.attribute("target", this.getTarget());
    xml.attribute("type", this.getType().toString());
    xml.attribute("log-level", this.getLogLevel().toString());
    for (Map.Entry<String, String> entry : getParameters().entrySet()) {
      xml.attribute(entry.getKey(), entry.getValue());
    }
    xml.closeElement();
  }

}
