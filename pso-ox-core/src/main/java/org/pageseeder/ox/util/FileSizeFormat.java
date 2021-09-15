/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.util;

import java.text.DecimalFormat;

/**
 * The file size formatter is to format the specified size to a human readable format.
 *
 * @author Ciber Cai
 * @since 19 July 2016
 */
public class FileSizeFormat {

  public enum Unit {
    BYTE("Byte", 0),

    KILO_BYTE("KB", 1),

    MEGA_BYTE("MB", 2),

    GIGA_BYTE("GB", 3),

    TERA_BYTE("TB", 4);

    private final String _text;
    private final int _group;

    private Unit(String text, int group) {
      this._text = text;
      this._group = group;
    }

    /**
     * @return the digital group
     */
    public int getGroup() {
      return this._group;
    }

    @Override
    public String toString() {
      return this._text;
    }

    public static Unit getUnit(int group) {
      for (Unit u : values()) {
        if (u.getGroup() == group) return u;
      }
      return Unit.BYTE;
    }

  }

  public String format(long size, Unit unit) {
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, unit.getGroup())) + unit.toString();
  }

  public String format(long size) {
    int group = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, group)) + Unit.getUnit(group);
  }

}
