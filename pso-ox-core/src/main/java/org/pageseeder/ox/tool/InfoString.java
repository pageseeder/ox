package org.pageseeder.ox.tool;

import java.util.Map;
import java.util.Objects;

/**
 * @author ccabral
 * @since 02 March 2021
 */
public class InfoString extends Info {

  private final String _value;

  public InfoString(String name,  Map<String, String> extraAttributes, String value) {
    super(name, InfoType.string, extraAttributes);
    Objects.requireNonNull(value);
    this._value = value;
  }

  public InfoString(String name,  String value) {
    super(name, InfoType.string);
    Objects.requireNonNull(value);
    this._value = value;
  }

  @Override
  public String getValue() {
    return this._value;
  }
}
