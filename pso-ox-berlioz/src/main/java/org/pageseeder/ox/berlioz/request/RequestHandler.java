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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * The interface that will convert the request to a list of PackageData.
 *
 * @author ccabral
 * @since 15 May 2025
 */
public interface RequestHandler {

  public final static String HANDLER_TYPE_PARAMETER = "handler-type";

  public List<PackageData> receive(HttpServletRequest req) throws IOException, OXException;
}
