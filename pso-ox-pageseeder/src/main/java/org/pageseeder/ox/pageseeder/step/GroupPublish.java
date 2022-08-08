package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.service.PublishService;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GroupPublish implements XMLWritable {

  private String project;
  private String group;

  private String member;
  private String target;
  private PublishService.Type type;
  private PublishService.LogLevel logLevel;
  private HashMap<String, String> parameters;

  private String errorMessage;

  public GroupPublish(String project, String group, String target, String type, String logLevel) {
    this.project = project;
    this.group = group;
    this.target = target;
    this.type = PublishService.Type.valueOf(type);
    this.logLevel = PublishService.LogLevel.valueOf(logLevel);
  }

  public GroupPublish(String project, String group, String member, String target, String type, String logLevel, HashMap<String, String> parameters) {
    this.project = project;
    this.group = group;
    this.member = member;
    this.target = target;
    this.type = PublishService.Type.valueOf(type);
    this.logLevel = PublishService.LogLevel.valueOf(logLevel);
    this.parameters = parameters;
  }

  public GroupPublish(String project, String group, String target, String type, String logLevel, HashMap<String, String> parameters) {
    this.project = project;
    this.group = group;
    this.target = target;
    this.type = PublishService.Type.valueOf(type);
    this.logLevel = PublishService.LogLevel.valueOf(logLevel);
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

//  public void addErrorMessages(XMLWriter writer) {
//    for (String message : status.getMessages()) {
//      if (StringUtils.isBlank(message)) {
//        writer.element("message", message);
//      }
//    }
//  }

}
