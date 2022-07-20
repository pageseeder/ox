package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.pageseeder.service.PublishService;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

public class GroupPublish implements XMLWritable {

  private String project;
  private String group;
  private String target;
  private PublishService.Type type;
  private PublishService.LogLevel logLevel;

  public GroupPublish(String project, String group, String target, String type, String logLevel) {
    this.project = project;
    this.group = group;
    this.target = target;
    this.type = PublishService.Type.valueOf(type);
    this .logLevel = PublishService.LogLevel.valueOf(logLevel);
  }

  public String getProject() {
    return project;
  }

  public String getGroup() {
    return group;
  }

  public String getTarget() {
    return target;
  }

  public PublishService.Type getType() {return type; }

  public PublishService.LogLevel getLogLevel() {
    return logLevel;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("action");
    xml.attribute("project", this.getProject());
    xml.attribute("group", this.getGroup());
    xml.attribute("target", this.getTarget());
    xml.attribute("type", this.getType().toString());
    xml.attribute("log-level", this.getLogLevel().toString());
    xml.closeElement();
  }

}
