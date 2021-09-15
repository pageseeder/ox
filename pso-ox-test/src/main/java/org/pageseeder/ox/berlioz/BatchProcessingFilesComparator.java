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

import org.junit.Assert;
import org.pageseeder.ox.berlioz.model.JobResponse;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.process.PipelineJobManager;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.xml.utils.XMLComparator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The Class BatchProcessingFilesComparator.
 *
 * @author Carlos Cabral
 * @since 13 Apr. 2018
 */
public class BatchProcessingFilesComparator {

  /** The job. */
  private final JobResponse _job;

  /** The expected results base directory. */
  private final File _expectedResultsBaseDirectory;

  /** The job base directory. */
  private final File _jobBaseDirectory;

  /** The files to ignore. */
  private final List<File> _filesToIgnore;

  /**
   * Instantiates a new batch processing files comparator.
   *
   * @param job the job
   * @param expectedResultsBaseDirectory the expected results base directory
   * @param filesToIgnore the files to ignore
   */
  public BatchProcessingFilesComparator(JobResponse job, File expectedResultsBaseDirectory, List<File> filesToIgnore) {
    super();
    this._job = job;
    this._expectedResultsBaseDirectory = expectedResultsBaseDirectory;
    this._filesToIgnore = filesToIgnore;
    PipelineJobManager manager = new PipelineJobManager();
    PipelineJob pipelineJob = manager.getJobId(this._job.getId());
    this._jobBaseDirectory = pipelineJob.getPackageData().directory();
  }

  /**
   * Gets the job.
   *
   * @return the job
   */
  public JobResponse getJob() {
    return _job;
  }

  /**
   * Compare.
   */
  public void compare() {

    compareDirectory(this._expectedResultsBaseDirectory);
  }

  /**
   * Compare directory.
   *
   * @param expected the expected
   */
  public void compareDirectory(File expected) {
    if (!shouldIgnore(expected)) {
      File jobTarget = getEquivalentFileResult(expected);
      Assert.assertTrue("Job Target Directory does not exist: " + jobTarget.getAbsolutePath(), jobTarget.exists());

      for(File nextExpected:expected.listFiles()) {
        if (nextExpected.isDirectory()) {
          compareDirectory(nextExpected);
        } else {
          compareFile(nextExpected);
        }
      }
    }
  }

  /**
   * Compare file.
   *
   * @param expected the expected
   */
  public void compareFile(File expected) {
    if (!shouldIgnore(expected)) {
      File jobTarget = getEquivalentFileResult(expected);
      Assert.assertTrue("Job Target File does not exist: " + jobTarget.getAbsolutePath(), jobTarget.exists());
      String filename = jobTarget.getName();

      if (filename.endsWith("xml") || filename.endsWith("psml") || filename.endsWith("html") || filename.endsWith("xhtml") || filename.endsWith("htm")) {
        compareXMLFile(expected, jobTarget);
      } else {
        compareGenericFile(expected, jobTarget);
      }
    }
  }

  /**
   * Compare XML file.
   *
   * @param expected the expected
   * @param target the target
   */
  private void compareXMLFile(File expected, File target) {
    XMLComparator.compareXMLFile(expected, target);
  }

  /**
   * Compare generic file.
   *
   * @param expected the expected
   * @param target the target
   */
  private void compareGenericFile(File expected, File target) {
    long expectedSize = expected.length();
    long targetSize = target.length();
    System.out.println("Expected: " + expected.getAbsolutePath());
    System.out.println("Target: " + target.getAbsolutePath());
    Assert.assertEquals("The size are differents expected " + expectedSize + " target " + targetSize, expectedSize, targetSize);
  }

  /**
   * Should ignore.
   *
   * @param candidate the candidate
   * @return true, if successful
   */
  private boolean shouldIgnore(File candidate) {
    boolean shouldIgnore = false;
    if (this._filesToIgnore != null) {
      for (File file:this._filesToIgnore) {
        if (candidate.getAbsolutePath().equals(file.getAbsolutePath())) {
          shouldIgnore = true;
          break;
        }
      }
    }
    return shouldIgnore;
  }

  /**
   * Compare file.
   *
   * @param expected the expected
   * @throws IOException
   */
  public File getEquivalentFileResult(File expected) {
    String expectedFileName = expected.getAbsolutePath().replace(this._expectedResultsBaseDirectory.getAbsolutePath(), "");
    File jobTarget = null;
    if (expectedFileName.equals("")) {
      if (expected.isFile()) {
        jobTarget = new File(this._jobBaseDirectory, this._expectedResultsBaseDirectory.getName());
      } else {
        jobTarget = this._jobBaseDirectory;
      }
    } else {
      jobTarget = new File(this._jobBaseDirectory, expectedFileName);
    }
    return jobTarget;
  }


  /**
   * Compare file.
   *
   * @param expected the expected
   * @return the XML result
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getXMLResult(File expected) throws IOException {
    String xml = "";
    if (expected.isFile()) {
      File jobTarget = getEquivalentFileResult(expected);
      String filename = jobTarget.getName();
      if (filename.endsWith("xml") || filename.endsWith("psml") || filename.endsWith("html")) {
        xml = FileUtils.read(jobTarget);
      }
    }
    return xml;
  }

}
