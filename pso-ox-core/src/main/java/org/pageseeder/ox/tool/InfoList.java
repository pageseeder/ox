package org.pageseeder.ox.tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * info type list. The value has semicolon (';') separated list
 *
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoList extends Info {

  private final List<String> _values;

  public InfoList(String name, Map<String, String> extraAttributes, List<String> values) {
    super(name, InfoType.list, extraAttributes);
    Objects.requireNonNull(values);
    this._values = values;
  }

  public InfoList(String name, List<String> values) {
    super(name, InfoType.list);
    Objects.requireNonNull(values);
    this._values = values;
  }

  @Override
  public String getValue() {
    return String.join(";", this._values);
  }
}
