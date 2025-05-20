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
package org.pageseeder.ox.berlioz.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.BatchProcessingFilesComparator;
import org.pageseeder.ox.berlioz.BatchProcessingSimulator;
import org.pageseeder.ox.berlioz.model.JobResponse;
import org.pageseeder.ox.berlioz.request.FileHandler;
import org.pageseeder.ox.berlioz.request.NoFileHandler;
import org.pageseeder.ox.berlioz.request.RequestHandlerFactory;
import org.pageseeder.ox.berlioz.request.URLHandler;
import org.pageseeder.ox.berlioz.util.BerliozOXUtils;
import org.pageseeder.ox.util.FileUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Test all steps for the complete right.
 *
 * @author Carlos Cabral
 * @since 28 Mar. 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BerliozOXUtils.class, RequestHandlerFactory.class, FileHandler.class, NoFileHandler.class, URLHandler.class} )
public class NoFileHandlerExampleTest {
  private final static String MODEL = "test";
  //private final static File _expectedResultsBaseDirectory = new File("src/test/resources/org/pageseeder/ox/berlioz/basic/target");
  private static JobResponse jobStatus;

  @BeforeClass
  public static void setupServlet() throws IOException, OXException, ServletException, InterruptedException {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/berlioz/basic/model");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
    Map<String, String> parameters = new HashMap<>();
    parameters.put("handler-type", "nofile");
    String pipeline = "no-file";
    BatchProcessingSimulator simulator = new BatchProcessingSimulator(MODEL, pipeline, parameters);
    jobStatus = simulator.simulate(null);
  }

  /**
   *
   */
  @Test
  public void testNopStep () {
//    File expected = new File("src/test/resources/org/pageseeder/ox/berlioz/basic/target/source/source.xml");
//    List<File> filesToIgnore = new ArrayList<>();
//    BatchProcessingFilesComparator compareFiles = new BatchProcessingFilesComparator(jobStatus, _expectedResultsBaseDirectory, filesToIgnore);
//    compareFiles.compareFile(expected);
    System.out.println("Job Status: " + jobStatus.toString());
    //Assert.assertEquals("ok", jobStatus.getStatus().toLowerCase());
    //Assert.assertNull(jobStatus.getInput());
  }
}
