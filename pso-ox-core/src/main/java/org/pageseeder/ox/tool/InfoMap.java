package org.pageseeder.ox.tool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoMap extends Info {

  private Map<String, List<String>> mapValues;

  public InfoMap(String name, Map<String, String> extraAttributes, Map<String, List<String>> valuesMap) {
    super(name, InfoType.map, extraAttributes);
    this.mapValues = valuesMap;
  }

  @Override
  public String getValue() {
//    return String.join(";", this.values);
    return this.mapValues.entrySet()
        .stream()
        .map(e -> e.getKey() + ":" + String.join(",", e.getValue()))
        .collect(Collectors.joining(";"));
  }
}
