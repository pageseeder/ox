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

import net.pageseeder.app.simple.core.utils.SimpleDateTimeUtils;
import net.pageseeder.app.simple.pageseeder.model.AddURIMetadata;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.psml.Property;
import org.pageseeder.ox.util.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * The type Add uri metadata handler test.
 *
 * @author ccabral
 * @since 12 January 2023
 */
public class AddURIMetadataHandlerTest {

  /**
   * Test handler.
   */
  @Test
  public void testHandler(){
    final File inputXML = new File("src/test/resources/org/pageseeder/ox/pageseeder/xml/addmetadata/input-add-uri-metadata.xml");
    final List<AddURIMetadata> addURIMetadataList = parse(inputXML);
    Assert.assertNotNull(addURIMetadataList);
    Assert.assertEquals(3, addURIMetadataList.size());
    AddURIMetadata addURIMetadata = addURIMetadataList.get(0);
    validateAddMetadata(addURIMetadata, null, Boolean.FALSE, Boolean.TRUE,
        SimpleDateTimeUtils.toDateTime(null),"Metadata note 01", Arrays.asList("label01", "label02"),
        "Note Tile", Boolean.TRUE, Arrays.asList("2021", "2021-01", "2021-01-01"));

    addURIMetadata = addURIMetadataList.get(1);
    validateAddMetadata(addURIMetadata, 2l, Boolean.TRUE, null,
        SimpleDateTimeUtils.toDateTime("2023-01-01T01:01:01"),"Metadata note 02", null,
        null, null, Arrays.asList("2022", "2022-02", "2022-02-02"));

    addURIMetadata = addURIMetadataList.get(2);
    validateAddMetadata(addURIMetadata, 3l, null, Boolean.FALSE, null,
        null, null, null, null,
        Arrays.asList("2023", "2023-03", "2023-03-03"));
  }

  private void validateAddMetadata (AddURIMetadata addURIMetadata, Long expectedUriid, Boolean expectedDraft,
                                    Boolean expectedHtml, LocalDateTime expectedLastModified, String expectedNote,
                                    List<String> expectedNoteLabels, String expectedNoteTitle,
                                    Boolean expectedTransclude, List<String> propertyValues) {

    Assert.assertEquals(Long.valueOf(1l), addURIMetadata.getMember().getId());
    Assert.assertEquals(Long.valueOf(1l), addURIMetadata.getGroup().getId());
    Assert.assertEquals(expectedUriid, addURIMetadata.getUriid());
    Assert.assertEquals(expectedDraft, addURIMetadata.getDraft());
    Assert.assertEquals(expectedHtml, addURIMetadata.getHtml());
    Assert.assertEquals(expectedLastModified, addURIMetadata.getLastModified());
    Assert.assertEquals(expectedNote, addURIMetadata.getNote());
    Assert.assertEquals(expectedNoteLabels, addURIMetadata.getNoteLabels());
    Assert.assertEquals(expectedNoteTitle, addURIMetadata.getNoteTitle());
    Assert.assertEquals(expectedTransclude, addURIMetadata.getTransclude());

    Assert.assertNotNull(addURIMetadata.getProperties());
    Assert.assertEquals(propertyValues.size(), addURIMetadata.getProperties().size());
    List<Property> properties = addURIMetadata.getProperties();
    for (int i = 0; i < propertyValues.size(); i++) {
      Assert.assertEquals(propertyValues.get(i), properties.get(i).getValue());
    }

  }

  private List<AddURIMetadata> parse(File workbookXml) {

    final AddURIMetadataHandler handler = new AddURIMetadataHandler(new PSMember(1l), new PSGroup(1l));

    try (FileInputStream fiStream  = new FileInputStream(workbookXml)) {
      XMLUtils.parseXML(fiStream, handler);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return handler.list();
  }
}
