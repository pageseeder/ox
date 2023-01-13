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
import net.pageseeder.app.simple.core.utils.SimpleNumberUtils;
import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import net.pageseeder.app.simple.pageseeder.model.CommentParameter;
import net.pageseeder.app.simple.pageseeder.model.builder.CommentParameterBuilder;
import org.pageseeder.bridge.model.PSNotify;
import org.pageseeder.bridge.psml.Property;
import org.pageseeder.bridge.search.Page;
import org.pageseeder.bridge.xml.BasicHandler;
import org.pageseeder.ox.pageseeder.model.AddMetadata;
import org.pageseeder.ox.pageseeder.model.AddMetadataBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Carlos Cabral
 * @since 11 January 2023
 */

public class AddMetadataHandler extends BasicHandler<AddMetadata> {

  private AddMetadataBuilder builder = null;

  public void startElement(String element, Attributes attributes) {
    if (attributes == null) throw new SimpleSiteException("Attributes is null");

    switch (element) {
      case "metadata":
        builder = new AddMetadataBuilder();
        break;
      case "uriid":
      case "title" :
      case "description" :
      case "labels" :
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
          super.add(builder.build());
        }
        //Reset
        this.builder = null;
        break;
      case "uriid":
        builder.uriid(SimpleNumberUtils.toLong(super.buffer(Boolean.TRUE), null));
        break;
      case "title":
        builder.title(super.buffer(true));
        break;
      case "description" :
        builder.description(super.buffer(true));
        break;
      case "labels" :
        builder.labels(SimpleStringUtils.toList(super.buffer(true)));
        break;
    }
  }
}
