/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.servlet;

import org.pageseeder.ox.process.PipelineJobManager;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>A servlet to check all jobs status.</p>
 *
 * @author Carlos Cabral
 * @version 08 May 2018
 */
public final class OXListJobs extends HttpServlet {

  /* UploadServlet.java */
  private static final long serialVersionUID = 6721151562078543731L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/xml");
    XMLWriter xml = new XMLWriterImpl(resp.getWriter());
    xml.xmlDecl();

    PipelineJobManager manager = new PipelineJobManager();
    manager.toXML(xml);

    xml.flush();
    xml.close();

    resp.setContentLength(xml.toString().length());

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
