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
import org.pageseeder.ox.berlioz.request.*;
import org.pageseeder.ox.berlioz.servlet.OXHandleData;
import org.pageseeder.ox.berlioz.util.BerliozOXUtils;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.util.FileUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The Class BatchProcessingCallJob.
 *
 * @author Carlos Cabral
 * @since 28 Mar. 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BerliozOXUtils.class, RequestHandlerFactory.class, FileHandler.class, NoFileHandler.class, URLHandler.class} )
public class BatchProcessingCallJob {

  /** The model. */
  private final String _model;

  /** The pipeline. */
  private final String _pipeline;

  /** The parameters. */
  private Map<String, String> parameters;

  /** The input. */
  private final File _input;

  /**
   * Instantiates a new batch processing call job.
   *
   * @param model the model
   * @param pipeline the pipeline
   * @param parameters the parameters
   * @param input the input
   */
  public BatchProcessingCallJob(String model, String pipeline, Map<String, String> parameters, File input) {
    super();
    this._model = model;
    this._pipeline = pipeline;
    this.parameters = parameters;
    this._input = input;
  }

  /**
   * Execute.
   *
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws OXException the OX exception
   * @throws ServletException the servlet exception
   */
  public String execute() throws IOException, OXException, ServletException {
    StringWriter writer = new StringWriter();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    this.parameters.put("pipeline",this._pipeline);

    List<PackageData> packs = mockedPackageList();
    System.out.println(packs.get(0).directory());
    List<PipelineJob> jobs = BerliozOXUtils.toPipelineJobs(packs);
    when(response.getWriter()).thenReturn(new PrintWriter(writer));
    when(request.getMethod()).thenReturn("POST");
    when(request.getParameter("model")).thenReturn(this._model);
    when(request.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundary8REQtuY1QyQrEfzh");

    mockRequestHandler(request, packs);

    PowerMockito.mockStatic(BerliozOXUtils.class);
    PowerMockito.when(BerliozOXUtils.toPipelineJobs(packs)).thenReturn(jobs);

    // Call pipeline
    OXHandleData handler = new OXHandleData();
    handler.service(request, response);

    return writer.toString();
  }

  private void mockRequestHandler(HttpServletRequest request, List<PackageData> packs) throws OXException, IOException {
    String handlerTypeParam = this.parameters.get(RequestHandler.HANDLER_TYPE_PARAMETER);
    RequestHandlerType handlerType = BerliozOXUtils.getRequestHandlerType(handlerTypeParam);
    RequestHandler requestHandler = null;
    // Mock mockRequestHandlerFactory static method getInstance()
    PowerMockito.mockStatic(RequestHandlerFactory.class);
    // Create a mock instance of mockRequestHandlerFactory
    RequestHandlerFactory mockRequestHandlerFactory = mock(RequestHandlerFactory.class);
    // When getInstance is called, return the mock instance
    when(RequestHandlerFactory.getInstance()).thenReturn(mockRequestHandlerFactory);

    switch (handlerType) {
      case FILE:
        requestHandler = mockFileHandler(request, packs);
        break;
      case NOFILE:
        requestHandler = mockNoFileHandler(request, packs);
        break;
      case URL:
        requestHandler = mockURLHandler(request, packs);
        break;
    }

    when(mockRequestHandlerFactory.getRequestHandler(request)).thenReturn(requestHandler);
  }

  private RequestHandler mockFileHandler(HttpServletRequest request, List<PackageData> packs) throws OXException, IOException {
    // Mock static method getInstance()
    PowerMockito.mockStatic(FileHandler.class);

    // Create a mock instance of FileHandler
    FileHandler mockFileHandler = mock(FileHandler.class);

    // When getInstance is called, return the mock instance
    when(FileHandler.getInstance()).thenReturn(mockFileHandler);
    when(mockFileHandler.receive(request)).thenReturn(packs);
    return mockFileHandler;
  }

  private RequestHandler mockNoFileHandler(HttpServletRequest request, List<PackageData> packs) throws OXException, IOException {
    // Mock static method getInstance()
    PowerMockito.mockStatic(NoFileHandler.class);

    // Create a mock instance of NoFileHandler
    NoFileHandler mockNoFileHandler = mock(NoFileHandler.class);

    // When getInstance is called, return the mock instance
    when(NoFileHandler.getInstance()).thenReturn(mockNoFileHandler);
    when(mockNoFileHandler.receive(request)).thenReturn(packs);

    return mockNoFileHandler;
  }


  private RequestHandler mockURLHandler(HttpServletRequest request, List<PackageData> packs) throws OXException, IOException {
    // Mock static method getInstance()
    PowerMockito.mockStatic(URLHandler.class);

    // Create a mock instance of URLHandler
    URLHandler mockURLHandler = mock(URLHandler.class);

    // When getInstance is called, return the mock instance
    when(URLHandler.getInstance()).thenReturn(mockURLHandler);
    when(mockURLHandler.receive(request)).thenReturn(packs);
    return mockURLHandler;
  }

  /**
   * Mocked package list.
   *
   * @return the list of PackageData
   * @throws IOException when I/O error occur.
   */
  private List<PackageData> mockedPackageList() throws IOException{
    List<PackageData> packs = new ArrayList<PackageData>();
    File dir = File.createTempFile("ox.allette.berlioz", ".tmp").getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }

    File file = null;
    if (this._input != null) {
      file = new File(dir, this._input.getName());
      FileUtils.copy(this._input, file);
    }
    PackageData pack = PackageData.newPackageData(this._model, file);
    if (pack != null) {
//      pack.setProperty("contenttype", item.getContentType());
      if (this._input != null) {
        pack.setProperty("type", toType(this._input.getName()));
        pack.setProperty("name", toName(this._input.getName()));
      }
      pack.setParameter("model", this._model);
      pack.setParameter("pipeline", this._pipeline);
      for (Entry<String, String> parameter : parameters.entrySet()) {
        pack.setParameter(parameter.getKey(), parameter.getValue());
      }
      pack.saveProperties();
      packs.add(pack);
    }
    return packs;
  }

  /**
   * To type.
   *
   * @param filename the specified file
   * @return the type of specified file.
   */
  private String toType(String filename) {
    if (filename == null) { throw new NullPointerException("file name cannot be null"); }
    String lcfilename = filename.toLowerCase();
    if (lcfilename.endsWith("xml")) {
      return "xml";
    } else if (lcfilename.endsWith("docx")) {
      return "docx";
    } else if (lcfilename.endsWith("html")) {
      return "html";
    } else if (lcfilename.endsWith("htm")) {
      return "html";
    } else if (lcfilename.endsWith("psml")) {
      return "psml";
    } else if (lcfilename.endsWith("zip")) {
      return "zip";
    } else {
      return filename.substring(filename.lastIndexOf(".") + 1);
    }
  }

  /**
   * To name.
   *
   * @param filename the filename
   * @return the name of file without extension.
   */
  private String toName(String filename) {
    int dot = filename.lastIndexOf('.');
    return filename.substring(0, dot);
  }
}
