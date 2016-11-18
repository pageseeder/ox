/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.Requests;
import org.pageseeder.ox.berlioz.util.FileHandler;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.process.PipelineJobManager;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;

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
    XMLWriter xml = new XMLWriterImpl(resp.getWriter());

    // get the model
    String modelName = req.getParameter("model");
    if (modelName == null || modelName.isEmpty()) {
      // get default model
      Requests.ensureConfigured();
      Model model = Model.getDefault();
      if (model == null) {
        xml.emptyElement("no-model");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      modelName = model.name();
    }

    // get packdata
    List<PackageData> packs = null;
    try {
      packs = FileHandler.receive(modelName, req);
    } catch (OXException ex) {
      xml.openElement("invalid-data");
      xml.writeText(ex.getMessage());
      xml.closeElement();
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (packs == null || packs.isEmpty()) {
      xml.emptyElement("no-package-data");
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // get the list of pipelineJob
    List<PipelineJob> jobs = FileHandler.toPipelineJobs(modelName, packs);

    // get the pipeline manager
    PipelineJobManager manager = new PipelineJobManager(GlobalSettings.get("ox2.threads.number", 1));

    // add the job to que
    xml.openElement("jobs", true);
    for (PipelineJob job : jobs) {
      job.toXML(xml);
      manager.addJob(job);
    }
    xml.closeElement();

    if (jobs.isEmpty()) {
      xml.emptyElement("no-package-data");
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
