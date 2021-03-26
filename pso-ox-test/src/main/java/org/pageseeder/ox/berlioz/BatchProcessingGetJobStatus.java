/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.berlioz;

import org.junit.runner.RunWith;
import org.pageseeder.ox.berlioz.servlet.OXCheckStatus;
import org.pageseeder.ox.berlioz.util.FileHandler;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Carlos Cabral
 * @since 28 Mar. 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileHandler.class)
public class BatchProcessingGetJobStatus {

  public String getJobStatus(String jobid) throws IOException, ServletException {
    StringWriter writer = new StringWriter();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(writer));
    when(request.getParameter("id")).thenReturn(jobid);
    when(request.getMethod()).thenReturn("GET");
    OXCheckStatus statusChecker = new OXCheckStatus();
    statusChecker.service(request, response);
    return writer.toString();
  }
}
