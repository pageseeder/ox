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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Carlos Cabral
 * @since 05 Feb. 2018
 */
public class StringUtilsTest {


  @Test
  public void testIsCommaSeparateFileList() {
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("c:\\path\\file.java,/root/file2.html,c:/path/file 3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("c:\\path\\file_1.java,/root/file-2.html,c:/path/file 3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("file.java,file2.html,file3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList(" file_1.java, file-2.html, file 3.csv"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("c:\\path\\file.java,/root/file2.html"));
    Assert.assertTrue(StringUtils.isCommaSeparateFileList("file.java,file2.html"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java,file2.html,"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList(",file.java,file2.html"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java,"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList(",file.java"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java"));
    Assert.assertFalse(StringUtils.isCommaSeparateFileList("file.java,,file.java"));
  }

  @Test
  public void testConvertToStringList(){
    String [] stringValuesTest = {"xml,html,psml", "", null, "docx", "html,", "xml,,html"};
    for (String stringValues: stringValuesTest) {
      System.out.println(stringValues);
      List<String> expectedValues = !StringUtils.isBlank(stringValues)? Arrays.asList(stringValues.replaceAll(",,", ",").split(",")) : new ArrayList<>();
      List<String> values = StringUtils.convertToStringList(stringValues);
      Assert.assertEquals(expectedValues.size(), values.size());
      for(int i = 0 ; i < expectedValues.size() ; i ++) {
        Assert.assertEquals(expectedValues.get(i), values.get(i));
      }
    }
  }
}
