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
package org.pageseeder.ox.berlioz.generator;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.ox.core.JobStatus;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.process.PipelineJobManager;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 * <p>A generator to check the pipleine job status.</p>
 *
 * <h3>Parameters</h3>
 * <ul>
 *  <li><var>id</var> the job id</li>
 * </ul>
 *
 * @author Ciber Cai
 * @version 28 November 2014
 */
public class CheckPipelineJobStatus extends ProfilerGenerator {

  @Override
  public void processSub(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String id = req.getParameter("id");
    if (id == null) {
      req.setStatus(ContentStatus.BAD_REQUEST);
      return;
    }

    PipelineJobManager manager = new PipelineJobManager();
    JobStatus status = manager.checkJobStatus(id);
    if (status == null) {
      req.setStatus(ContentStatus.NOT_FOUND);
      return;
    }

    // print the status xml
    status.toXML(xml);

    // print the job xml.
    PipelineJob job = manager.getJobId(id);
    job.toXML(xml);
  }

}
