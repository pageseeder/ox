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
package org.pageseeder.ox.berlioz;

import org.junit.runner.RunWith;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.model.JobResponse;
import org.pageseeder.ox.berlioz.util.FileHandler;
import org.pageseeder.ox.berlioz.xml.JobResponseHandler;
import org.pageseeder.ox.util.XMLUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author Carlos Cabral
 * @since 28 Mar. 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileHandler.class)
public class BatchProcessingSimulator {
  private final String _model;
  private final String _pipeline;
  private final Map<String, String> parameters;

  public BatchProcessingSimulator(String model, String pipeline, Map<String, String> parameters) {
    super();
    this._model = model;
    this._pipeline = pipeline;
    this.parameters = parameters;
  }


  public JobResponse simulate(File input) throws IOException, OXException, ServletException, InterruptedException {
    BatchProcessingCallJob callJob = new BatchProcessingCallJob(this._model, this._pipeline, this.parameters, input);
    String jobXMLResponse = callJob.execute();
    JobResponseHandler handler = new JobResponseHandler();
    XMLUtils.parseXML(jobXMLResponse, handler);
    JobResponse job = handler.getJob();
    String jobid = job.getId();

    JobResponse jobStatus =null;
    BatchProcessingGetJobStatus getJobStatus = new  BatchProcessingGetJobStatus();
    do {
      Thread.sleep(1000l);
      String jobStatusXMLResponse = getJobStatus.getJobStatus(jobid);
      JobResponseHandler statusHandler = new JobResponseHandler();
      XMLUtils.parseXML(jobStatusXMLResponse, statusHandler);
      jobStatus = statusHandler.getJob();
    } while(jobStatus != null && (jobStatus.getStatus().equals("PROCESSING") || jobStatus.getStatus().equals("STOP")));
    return jobStatus;
  }


  public void validate(JobResponse jobStatus, File expectedResultsBaseDirectory, List<File> filesToIgnore) throws IOException, OXException, ServletException, InterruptedException {
    BatchProcessingFilesComparator compareFiles = new BatchProcessingFilesComparator(jobStatus, expectedResultsBaseDirectory, filesToIgnore);
    compareFiles.compare();
  }
}
