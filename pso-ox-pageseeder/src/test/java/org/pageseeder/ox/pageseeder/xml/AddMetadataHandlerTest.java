/*
 * Copyright 2023 Allette Systems (Australia)
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
package org.pageseeder.ox.pageseeder.xml;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.bridge.psml.Property;
import org.pageseeder.ox.pageseeder.model.AddMetadata;
import org.pageseeder.ox.util.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author ccabral
 * @since 12 January 2023
 */
public class AddMetadataHandlerTest {

  @Test
  public void testHandlerTrue(){
    final File inputXML = new File("src/test/resources/org/pageseeder/ox/pageseeder/xml/addmetadata/input-add-uri-metadata.xml");
    final List<AddMetadata> addMetadataList = parse(inputXML);
    Assert.assertNotNull(addMetadataList);
    Assert.assertEquals(3, addMetadataList.size());
    AddMetadata addMetadata = addMetadataList.get(0);
    validateAddMetadata(addMetadata, null, "Test 01", "", "restricted",
        Arrays.asList("2021", "2021-01", "2021-01-01"));

    addMetadata = addMetadataList.get(1);
    validateAddMetadata(addMetadata, 2l, "Test 02", "_chemoc", "restricted,embargo",
        Arrays.asList("2022", "2022-02", "2022-02-02"));

    addMetadata = addMetadataList.get(2);
    validateAddMetadata(addMetadata, 3l, "Test 03", "", "restricted",
        Arrays.asList("2023", "2023-03", "2023-03-03"));
  }

  private void validateAddMetadata (AddMetadata addMetadata, Long expectedUriid, String expectedTitle,
                                    String expectedDescription, String expectedLabels, List<String> propertyValues) {

    Assert.assertEquals(expectedUriid, addMetadata.getUriid());
    Assert.assertEquals(expectedTitle, addMetadata.getTitle());
    Assert.assertEquals(expectedDescription, addMetadata.getDescription());
    Assert.assertEquals(expectedLabels, String.join(",", addMetadata.getLabels()));
    Assert.assertNotNull(addMetadata.getProperties());
    Assert.assertEquals(propertyValues.size(), addMetadata.getProperties().size());
    List<Property> properties = addMetadata.getProperties();
    for (int i = 0; i < propertyValues.size(); i++) {
      Assert.assertEquals(propertyValues.get(i), properties.get(i).getValue());
    }

  }

  private List<AddMetadata> parse(File workbookXml) {

    final AddMetadataHandler handler = new AddMetadataHandler();

    try (FileInputStream fiStream  = new FileInputStream(workbookXml)) {
      XMLUtils.parseXML(fiStream, handler);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return handler.list();
  }
}
