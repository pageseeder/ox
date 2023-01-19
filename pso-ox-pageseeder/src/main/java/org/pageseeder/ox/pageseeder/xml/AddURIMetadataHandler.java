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
import net.pageseeder.app.simple.pageseeder.model.builder.AddURIMetadataBuilder;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSMember;
import org.pageseeder.bridge.psml.Property;
import org.pageseeder.bridge.xml.BasicHandler;
import org.xml.sax.Attributes;

/**
 * The type Add uri metadata handler.
 *
 * @author Carlos Cabral
 * @since 11 January 2023
 */
public class AddURIMetadataHandler extends BasicHandler<AddURIMetadata> {

  private AddURIMetadataBuilder builder = null;
  private final PSMember member;
  private final PSGroup psGroup;

  /**
   * Instantiates a new Add uri metadata handler.
   *
   * @param member  the member
   * @param psGroup the ps group
   */
  public AddURIMetadataHandler(PSMember member, PSGroup psGroup) {
    this.member = member;
    this.psGroup = psGroup;
  }

  public void startElement(String element, Attributes attributes) {
    if (attributes == null) throw new SimpleSiteException("Attributes is null");

    switch (element) {
      case "metadata":
        builder = new AddURIMetadataBuilder();
        break;
      case "uriid":
      case "draft" :
      case "html" :
      case "last-modified" :
      case "note" :
      case "note-labels" :
      case "note-title" :
      case "transclude" :
        super.newBuffer();
        break;
      case "property":
        Property property = new Property(attributes.getValue("name"));
        property.setType(attributes.getValue("type"));
        property.setTitle(attributes.getValue("title"));
        property.setValue(attributes.getValue("value"));
        builder.property(property);
        break;
    }
  }

  public void endElement(String element) {
    switch (element) {
      case "metadata":
        if (this.builder != null) {
          this.builder.member(this.member);
          this.builder.group(this.psGroup);
          super.add(builder.build());
        }
        //Reset
        this.builder = null;
        break;
      default:
        if (this.builder != null) {
          //If builder is different of null then it is inside the metadata element.
          //The reason we check if because it maybe used for xml files with another information that can conflict.
          endElementInsideMetadata(element);
        }
    }
  }

  private void endElementInsideMetadata (String element) {
    final String text = super.buffer(Boolean.TRUE);
    switch (element) {
      case "uriid":
        builder.uriid(SimpleNumberUtils.toLong(text, null));
        break;
      case "draft" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.draft("true".equalsIgnoreCase(text));
        }
        break;
      case "html" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.html("true".equalsIgnoreCase(text));
        }
        break;
      case "last-modified" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.lastModified(SimpleDateTimeUtils.toDateTime(text));
        }
        break;
      case "note" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.note(text);
        }
        break;
      case "note-labels" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.noteLabels(SimpleStringUtils.toList(text));
        }
        break;
      case "note-title" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.noteTitle(text);
        }
        break;
      case "transclude" :
        if (!SimpleStringUtils.isBlank(text)) {
          builder.transclude(!"false".equalsIgnoreCase(text));
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
