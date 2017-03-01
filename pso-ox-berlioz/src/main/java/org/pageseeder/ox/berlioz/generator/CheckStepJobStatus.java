/*
 *  Copyright (c) 2017 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.berlioz.generator;

import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.StepJob;
import org.pageseeder.ox.process.StepJobManager;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * <p>A generator to check the step job status.</p>
 *
 * <h3>Parameters</h3>
 * <ul>
 *  <li><var>id</var> the job id</li>
 * </li>
 *
 * @author Carlos Cabral
 * @version 27 February 2017
 */
public class CheckStepJobStatus implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String id = req.getParameter("id");
    if (id == null) {
      req.setStatus(ContentStatus.BAD_REQUEST);
      return;
    }

    StepJobManager manager = new StepJobManager();
    JobStatus status = manager.checkJobStatus(id);
    if (status == null) {
      req.setStatus(ContentStatus.NOT_FOUND);
      return;
    }

    // print the status xml
    status.toXML(xml);

    // print the job xml.
    StepJob job = manager.getJobId(id);
    job.toXML(xml);
    
    if (!status.hasCompleted()) {
      req.setStatus(ContentStatus.ACCEPTED);
    }
  }

}
