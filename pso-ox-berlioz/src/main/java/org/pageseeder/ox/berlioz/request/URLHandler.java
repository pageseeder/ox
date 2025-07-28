/*
 * Copyright 2025 Allette Systems (Australia)
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
package org.pageseeder.ox.berlioz.request;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.core.PackageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * This can be used when a pipeline needs a URL to start.
 * To use it, the parameter {@link RequestHandler#HANDLER_TYPE_PARAMETER} should have the value
 * {@link RequestHandlerType#URL}.
 *
 * @author ccabral
 * @since 15 May 2025
 */
public class URLHandler implements RequestHandler {

  /** The logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(URLHandler.class);

  private final static URLHandler INSTANCE = new URLHandler();

  private URLHandler(){
    LOGGER.info("URLHandler created");
  }

  public static URLHandler getInstance() {
    return INSTANCE;
  }

  @Override
  public List<PackageData> receive(HttpServletRequest req) throws IOException, OXException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
