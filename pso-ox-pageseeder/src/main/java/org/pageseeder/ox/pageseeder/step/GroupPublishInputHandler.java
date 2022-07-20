package org.pageseeder.ox.pageseeder.step;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class GroupPublishInputHandler extends DefaultHandler {

  List<GroupPublish> publishes; //TODO check the dependency

  public GroupPublishInputHandler() {
    this.publishes = new ArrayList<>();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (localName.equals("publish")) {
      this.publishes.add(new GroupPublish(
          attributes.getValue("project"),
          attributes.getValue("group"),
          attributes.getValue("target"),
          attributes.getValue("type"),
          attributes.getValue("log-level")
      ));
    }
  }

  public List<GroupPublish> getPublishes() {
    return this.publishes;
  }

}
