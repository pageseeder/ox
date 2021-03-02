package org.pageseeder.ox.tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Info type map. The value contains a map, each item of this map is separated by semicolon (';'),
 * the key and the value is separated by two points (':') and value also can contains a comma separated list.
 *
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoMap extends Info {

  private final Map<String, List<String>> _mapValues;

  public InfoMap(String name, Map<String, String> extraAttributes, Map<String, List<String>> valuesMap) {
    super(name, InfoType.map, extraAttributes);
    Objects.requireNonNull(valuesMap);
    this._mapValues = valuesMap;
  }

  @Override
  public String getValue() {
    return this._mapValues.entrySet()
        .stream()
        .map(e -> e.getKey() + ":" + String.join(",", e.getValue()))
        .collect(Collectors.joining(";"));
  }
}
