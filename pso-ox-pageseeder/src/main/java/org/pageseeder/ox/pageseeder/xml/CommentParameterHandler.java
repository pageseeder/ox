/*
 * Copyright 2022 Allette Systems (Australia)
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
import net.pageseeder.app.simple.core.utils.SimpleStringUtils;
import org.pageseeder.bridge.model.PSNotify;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import net.pageseeder.app.simple.pageseeder.model.CommentParameter;
import net.pageseeder.app.simple.pageseeder.model.builder.CommentParameterBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eddy Shao
 * @since 07 September 2022
 */

public class CommentParameterHandler extends DefaultHandler {

  private final CommentParameterBuilder cpBuilder;
  List<CommentParameter> cp;

  public CommentParameterHandler() {
    this.cp = new ArrayList<>();
    this.cpBuilder = new CommentParameterBuilder();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (localName.equals("comment")) {
      cpBuilder.id(Long.valueOf(attributes.getValue("commentid")));
      cpBuilder.title(attributes.getValue("title"));

      if (!SimpleStringUtils.isBlank(attributes.getValue("content"))) {
        cpBuilder.content(attributes.getValue("content"));
      }
      if (!SimpleStringUtils.isBlank(attributes.getValue("contenttype"))) {
        cpBuilder.contentRole(attributes.getValue("contenttype"));
      }
      if (!SimpleStringUtils.isBlank(attributes.getValue("labels"))) {
        cpBuilder.labels(attributes.getValue("labels"));
      }
      if (!SimpleStringUtils.isBlank(attributes.getValue("properties"))) {
        cpBuilder.properties(attributes.getValue("properties"));
      }
      if (!SimpleStringUtils.isBlank(attributes.getValue("notify"))) {
        cpBuilder.notify(PSNotify.valueOf(attributes.getValue("notify")));
      } else {
        cpBuilder.notify(PSNotify.silent);
      }
      if (!SimpleStringUtils.isBlank(attributes.getValue("type"))) {
        cpBuilder.type(attributes.getValue("type"));
      }

      CommentParameter currCp = cpBuilder.buildComment();
      this.cp.add(currCp);
    }
  }

  public List<CommentParameter> getCommentParameters() {
    return this.cp;
  }
}
