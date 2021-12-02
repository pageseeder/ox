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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ciber Cai
 * @since 19 Jul 2016
 */
public class FileSizeFormatTest {

  @Test
  public void format() {
    FileSizeFormat fmt = new FileSizeFormat();
    Assert.assertEquals("1Byte", fmt.format(1, FileSizeFormat.Unit.BYTE));
    Assert.assertEquals("1KB", fmt.format(1024, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("1KB", fmt.format(1025, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("1.5KB", fmt.format(1500, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("2KB", fmt.format(2049, FileSizeFormat.Unit.KILO_BYTE));
    Assert.assertEquals("1MB", fmt.format(1024 * 1024, FileSizeFormat.Unit.MEGA_BYTE));

    Assert.assertEquals("1Byte", fmt.format(1));
    Assert.assertEquals("1KB", fmt.format(1024));
    Assert.assertEquals("1KB", fmt.format(1025));
    Assert.assertEquals("1.5KB", fmt.format(1500));
    Assert.assertEquals("2KB", fmt.format(2049));
    Assert.assertEquals("1MB", fmt.format(1024 * 1024));
  }

}
