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
import net.pageseeder.app.simple.pageseeder.model.EditURI;
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
 * @since 18 January 2023
 */
public class EditURIHandlerTest {

  /**
   * Test handler.
   */
  @Test
  public void testHandler(){
    final File inputXML = new File("src/test/resources/org/pageseeder/ox/pageseeder/xml/edituri/input-edit-uri.xml");
    final List<EditURI> editURIList = parse(inputXML);
    Assert.assertNotNull(editURIList);
    Assert.assertEquals(3, editURIList.size());
    EditURI editURI = editURIList.get(0);
    validateEditURI(editURI, null, null, null, null,
        null, null, null, "Test 01");

    editURI = editURIList.get(1);
    validateEditURI(editURI, 2l, "_chemoc", "doc-id",
        Arrays.asList("restricted", "embargo"), "Test_02", "pub-id",
        "type", "Test 02");

    editURI = editURIList.get(2);
    validateEditURI(editURI, 3l, null,null,
        Arrays.asList("restricted"), "Test_03",null, null,
        "Test 03");
  }

  private void validateEditURI(EditURI editURI, Long expectedUriid, String expectedDescription,
                               String expectedDocumentId, List<String> expectedLabels, String expectedFileName,
                               String expectedPublicationID, String expectedPublicationType, String expectedTitle) {

    Assert.assertEquals(Long.valueOf(1l), editURI.getMember().getId());
    Assert.assertEquals(Long.valueOf(1l), editURI.getGroup().getId());
    Assert.assertEquals(expectedUriid, editURI.getUriid());
    Assert.assertEquals(expectedDescription, editURI.getDescription());
    Assert.assertEquals(expectedDocumentId, editURI.getDocumentId());
    Assert.assertEquals(expectedLabels, editURI.getLabels());
    Assert.assertEquals(expectedFileName, editURI.getFileName());
    Assert.assertEquals(expectedPublicationID, editURI.getPublicationId());
    Assert.assertEquals(expectedPublicationType, editURI.getPublicationType());
    Assert.assertEquals(expectedTitle, editURI.getTitle());
  }

  private List<EditURI> parse(File workbookXml) {

    final EditURIHandler handler = new EditURIHandler(new PSMember(1l), new PSGroup(1l));

    try (FileInputStream fiStream  = new FileInputStream(workbookXml)) {
      XMLUtils.parseXML(fiStream, handler);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return handler.list();
  }
}
