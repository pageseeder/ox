/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.psml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.BatchProcessingFilesComparator;
import org.pageseeder.ox.berlioz.BatchProcessingSimulator;
import org.pageseeder.ox.berlioz.model.JobResponse;
import org.pageseeder.ox.berlioz.util.FileHandler;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
@PrepareForTest(FileHandler.class)
public class TestPSMLValidation {
  private final static String MODEL = "psml";
  private final static File _input = new File("src/test/resources/org/pageseeder/ox/psml/basic/source/source.psml");
  private final static File _expectedResultsBaseDirectory = new File("src/test/resources/org/pageseeder/ox/psml/basic/target");
  private static JobResponse jobStatus;

  @BeforeClass
  public static void setupServlet() throws IOException, OXException, ServletException, InterruptedException {
    File modelDir = new File("src/test/resources/org/pageseeder/ox/psml/basic/model");
    OXConfig config = OXConfig.get();
    config.setModelsDirectory(modelDir);
    Map<String, String> parameters = new HashMap<>();
    parameters.put("_xslt-indent", "yes");
    String pipeline = "psml-validate";
    BatchProcessingSimulator simulator = new BatchProcessingSimulator(MODEL, pipeline, parameters);
    jobStatus = simulator.simulate(_input);
  }

  /**
   *
   */
  @Test
  public void testAllFiles () {
    List<File> filesToIgnore = new ArrayList<>();
    BatchProcessingFilesComparator compareFiles = new BatchProcessingFilesComparator(jobStatus, _expectedResultsBaseDirectory, filesToIgnore);
    compareFiles.compare();
  }
}
