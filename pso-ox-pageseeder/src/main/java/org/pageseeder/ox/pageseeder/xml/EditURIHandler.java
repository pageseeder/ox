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

import net.pageseeder.app.simple.core.SimpleSiteException;
import net.pageseeder.app.simple.core.utils.SimpleDateTimeUtils;
import net.pageseeder.app.simple.core.utils.SimpleNumberUtils;
import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.pageseeder.model.AddURIMetadata;
import net.pageseeder.app.simple.pageseeder.model.EditURI;
import net.pageseeder.app.simple.pageseeder.model.builder.AddURIMetadataBuilder;
import net.pageseeder.app.simple.pageseeder.model.builder.EditURIBuilder;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.psml.Property;
import org.pageseeder.bridge.xml.BasicHandler;
import org.xml.sax.Attributes;

/**
 * The typeedit uri handler.
 *
 * @author Carlos Cabral
 * @since 18 January 2023
 */
public class EditURIHandler extends BasicHandler<EditURI> {

  private EditURIBuilder builder = null;
  private final PSMember member;
  private final PSGroup psGroup;

  /**
   * Instantiates a new edit uri handler.
   *
   * @param member  the member
   * @param psGroup the ps group
   */
  public EditURIHandler(PSMember member, PSGroup psGroup) {
    this.member = member;
    this.psGroup = psGroup;
  }

  public void startElement(String element, Attributes attributes) {
    if (attributes == null) throw new SimpleSiteException("Attributes is null");

    switch (element) {
      case "edit-uri":
        builder = new EditURIBuilder();
        break;
      case "uriid":
      case "description" :
      case "document-id" :
      case "labels" :
      case "file-name" :
      case "publication-id" :
      case "publication-type" :
      case "title" :
        super.newBuffer();
        break;
    }
  }

  public void endElement(String element) {
    switch (element) {
      case "edit-uri":
        if (this.builder != null) {
          this.builder.member(this.getMember());
          this.builder.group(this.getPsGroup());
          super.add(builder.build());
        }
        //Reset
        this.builder = null;
        break;
      default:
        if (this.builder != null) {
          //If builder is different of null then it is inside the edit-uri element.
          //The reason we check if because it maybe used for xml files with another information that can conflict.
          endElementInsideEditURI(element);
        }
    }
  }


  /**
   * End element inside edit uri.
   *
   * @param element the element
   */
  public void endElementInsideEditURI(String element) {
    final String text = super.buffer(Boolean.TRUE);
    switch (element) {
      case "uriid":
        builder.uriid(SimpleNumberUtils.toLong(text, null));
        break;
      case "description" :
        if (text != null) {//It allows empty values in case wants to clean a value.
          builder.description(text);
        }
        break;
      case "document-id" :
        if (text != null) {//It allows empty values in case wants to clean a value.
          builder.documentId(text);
        }
        break;
      case "labels" :
        if (text != null) {
          builder.labels(SimpleStringUtils.toList(text));
        }
        break;
      case "file-name" :
        if (text != null) {
          builder.fileName(text);
        }
        break;
      case "publication-id" :
        if (text != null) {
          builder.publicationId(text);
        }
        break;
      case "publication-type" :
        if (text != null) {
          builder.publicationType(text);
        }
        break;
      case "title" :
        if (text != null) {
          builder.title(text);
        }
        break;
    }
  }

  /**
   * Gets member.
   *
   * @return the member
   */
  public PSMember getMember() {
    return member;
  }

  /**
   * Gets ps group.
   *
   * @return the ps group
   */
  public PSGroup getPsGroup() {
    return psGroup;
  }
}
