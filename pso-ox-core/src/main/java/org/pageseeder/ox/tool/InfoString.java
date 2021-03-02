package org.pageseeder.ox.tool;

import java.util.Map;

/**
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoString extends Info {

  private String value;

  public InfoString(String name,  Map<String, String> extraAttributes, String value) {
    super(name, InfoType.string, extraAttributes);
    this.value = value;
  }

  @Override
  public String getValue() {
    return this.value;
  }
}
