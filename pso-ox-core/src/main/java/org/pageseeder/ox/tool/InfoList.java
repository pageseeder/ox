package org.pageseeder.ox.tool;

import java.util.List;
import java.util.Map;

/**
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoList extends Info {

  private List<String> values;

  public InfoList(String name, Map<String, String> extraAttributes, List<String> values) {
    super(name, InfoType.list, extraAttributes);
    this.values = values;
  }

  @Override
  public String getValue() {
    return String.join(";", this.values);
  }
}
