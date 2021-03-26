/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.servlet;

import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.process.PipelineJobManager;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>A servlet to check the job status.</p>
 * <h3>Parameters</h3>
 * <ul>
 *  <li><var>id</var> the job id.</li>
 * </ul>
 *
 * @author Ciber Cai
 * @version 10 November 2014
 */
public final class OXCheckStatus extends HttpServlet {

  private final static Logger LOGGER = LoggerFactory.getLogger(OXCheckStatus.class);

  /* UploadServlet.java */
  private static final long serialVersionUID = 6721151562078543731L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOGGER.debug("Starts OXCheckStatus");
    resp.setContentType("application/xml");
    XMLWriter xml = new XMLWriterImpl(resp.getWriter());
    xml.xmlDecl();

    PipelineJobManager manager = new PipelineJobManager();

    String id = req.getParameter("id");
    LOGGER.debug("JOB ID {}", id);
    if (id == null) {
      xml.emptyElement("not-job-id-found");
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    JobStatus status = manager.checkJobStatus(id);
    if (status == null) {
      xml.emptyElement("not-job-found");
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    xml.openElement("status");
    status.toXML(xml);
    PipelineJob job = manager.getJobId(id);
    job.toXML(xml);
    xml.closeElement();// status
    xml.flush();
    xml.close();

    resp.setContentLength(xml.toString().length());
    LOGGER.debug("Ends OXCheckStatus");
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

}
