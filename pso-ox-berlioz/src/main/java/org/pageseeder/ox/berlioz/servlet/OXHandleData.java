/*
 * Copyright 2021 Allette Systems (Australia)
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
package org.pageseeder.ox.berlioz.servlet;

import org.apache.commons.fileupload.FileUploadBase;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.Errors;
import org.pageseeder.ox.berlioz.OXBerliozErrorMessage;
import org.pageseeder.ox.berlioz.request.RequestHandler;
import org.pageseeder.ox.berlioz.request.RequestHandlerFactory;
import org.pageseeder.ox.berlioz.util.BerliozOXUtils;
import org.pageseeder.ox.berlioz.request.FileHandler;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.process.PipelineJobManager;
import org.pageseeder.ox.process.StepJobManager;
import org.pageseeder.ox.process.StepJobQueue;
import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>A servlet to process the upload file by OX {@link PipelineJobManager}.</p>
 *
 * <h3>Parameters</h3>
 * <ul>
 *  <li><var>pipeline</var> the pipeline wants to use. if not specified it will run through the whole pipeline list.</li>
 * </ul>
 *
 * Note:
 * The form enctype must be "multipart/form-data".
 * Only accept the POST method.
 *
 * <h3>Configuration</h3>
 * <p>Use Berlioz config (ox2.threads.number) to define the number of threads. (default: 1) </p>
 * <p>Use Berlioz config (ox2.slow-mode.size) to define the size of file (kb) which applies the slow-mode. (default: -1, which means doesn't apply anything.) </p>
 *
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class OXHandleData extends HttpServlet {
  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXHandleData.class);


  /* UploadServlet.java */
  private static final long serialVersionUID = 6721151562078543731L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // not supported.
    notSupported(resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // not supported.
    notSupported(resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOGGER.debug("Started processing.");
    resp.setContentType("application/xml");
    XMLWriter xml = new XMLWriterImpl(resp.getWriter());
    xml.xmlDecl();

    try {
      LOGGER.debug("Model: {}", req.getParameter("model"));
      String contentType = req.getContentType();
      if (StringUtils.isBlank(contentType) || !contentType.startsWith(FileUploadBase.MULTIPART))
        throw new OXException(OXBerliozErrorMessage.REQUEST_IS_NOT_MULTIPART);

      // get packdata
      RequestHandlerFactory requestHandlerFactory = RequestHandlerFactory.getInstance();
      RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(req);
      List<PackageData> packs = requestHandler.receive(req);
      LOGGER.debug("Number os packs found: {}.", packs.size());

      if (packs == null || packs.isEmpty()) {
        xml.emptyElement("no-package-data");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      // get the list of pipelineJob
      List<PipelineJob> jobs = BerliozOXUtils.toPipelineJobs(packs);
      LOGGER.debug("Number of pipelines jobs: {}.", jobs.size());

      // get the pipeline manager
      PipelineJobManager manager = new PipelineJobManager(
          GlobalSettings.get("ox2.threads.number", StepJobManager.DEAULT_NUMBER_OF_THREAD),
          GlobalSettings.get("ox2.max-stored-completed-job", StepJobQueue.DEFAULT_MAX_STORED_COMPLETED_JOB));

      // add the job to que
      xml.openElement("jobs", true);
      for (PipelineJob job : jobs) {
        job.toXML(xml);
        manager.addJob(job);
        LOGGER.debug("Added Pipeline Job to Manager: {}.", job.getId());
      }
      xml.closeElement();

      if (jobs.isEmpty()) {
        xml.emptyElement("no-package-data");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (OXException ex) {
      Errors.oxExceptionHandler(xml, ex);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } finally {
      LOGGER.debug("Ended processing.");
    }
  }

  /**
   * To response that the request method doesn't allowed.
   * @param resp {@link HttpServletResponse}
   * @throws IOException
   */
  private static void notSupported(HttpServletResponse resp) throws IOException {
    XMLWriter xml = new XMLWriterImpl(resp.getWriter());
    xml.emptyElement("no-supported");
    xml.flush();
    xml.close();
    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }
}
