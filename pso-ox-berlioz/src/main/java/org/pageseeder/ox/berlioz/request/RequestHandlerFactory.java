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

import org.pageseeder.ox.berlioz.util.BerliozOXUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * The type Request handler factory.
 *
 * @author ccabral
 * @since 15 May 2025
 */
public class RequestHandlerFactory {

  private static final RequestHandlerFactory INSTANCE = new RequestHandlerFactory();

  private RequestHandlerFactory(){

  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static RequestHandlerFactory getInstance() {
    return INSTANCE;
  }

  /**
   * Gets request handler.
   *
   * @param req the req
   * @return the request handler
   */
  public RequestHandler getRequestHandler(HttpServletRequest req) {
    String parameterHandler = BerliozOXUtils.getParameterHandlerType(req);
    RequestHandlerType requestHandlerType = BerliozOXUtils.getRequestHandlerType(parameterHandler);
    RequestHandler requestHandler;
    switch (requestHandlerType) {
      case NOFILE:
        requestHandler = NoFileHandler.getInstance();
        break;
      case URL:
        requestHandler = URLHandler.getInstance();
        break;
      default:
        requestHandler = FileHandler.getInstance();
    }
    return requestHandler;
  }
}
