package org.pageseeder.ox.pageseeder.step;

import org.pageseeder.ox.pageseeder.model.GroupPublish;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

public class GroupPublishInputHandler extends DefaultHandler {

  List<GroupPublish> publishes;

  public GroupPublishInputHandler() {
    this.publishes = new ArrayList<>();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (localName.equals("publish")) {
      this.publishes.add(new GroupPublish(
          attributes.getValue("project"),
          attributes.getValue("group"),
          attributes.getValue("member"),
          attributes.getValue("target"),
          attributes.getValue("type"),
          attributes.getValue("log-level"),
          mapParametersFromAttributes(attributes)
      ));
    }
  }

  private static HashMap<String, String> mapParametersFromAttributes(Attributes attributes) {
    HashMap<String, String> parameters = new HashMap<>();
    List<String> attributesToExclude = Arrays.asList("project", "group", "target", "type","log-level", "member");
    for (int i = 0; i < attributes.getLength(); i++) {
      String aname = attributes.getLocalName(i) == null ? attributes.getQName(i) : attributes.getLocalName(i);
      if(!attributesToExclude.contains(aname)) {
        parameters.put(aname, attributes.getValue(i));
      }
    }
    return parameters;
  }

  public List<GroupPublish> getPublishes() {
    return this.publishes;
  }

}
