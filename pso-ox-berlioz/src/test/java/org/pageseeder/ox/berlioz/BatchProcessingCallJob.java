/*
 * Copyright (c) 2018 Allette systems pty. ltd.
 */
package org.pageseeder.ox.berlioz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.RunWith;
import org.pageseeder.ox.OXException;
import org.pageseeder.ox.berlioz.servlet.OXHandleData;
import org.pageseeder.ox.berlioz.util.FileHandler;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.PipelineJob;
import org.pageseeder.ox.util.FileUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * @author Carlos Cabral
 * @since 28 Mar. 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileHandler.class)
public class BatchProcessingCallJob {
  private final String _model;
  private final String _pipeline;
  private Map<String, String> parameters;
  private final File _input;
  
  public BatchProcessingCallJob(String model, String pipeline, Map<String, String> parameters, File input) {
    super();
    this._model = model;
    this._pipeline = pipeline;
    this.parameters = parameters;
    this._input = input;
  }

  public String execute() throws IOException, OXException, ServletException {
    StringWriter writer = new StringWriter();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    
    this.parameters.put("pipeline",this._pipeline);
    
    List<PackageData> packs = mockedPackageList();
    System.out.println(packs.get(0).directory());
    List<PipelineJob> jobs = FileHandler.toPipelineJobs(this._model, packs);
    when(response.getWriter()).thenReturn(new PrintWriter(writer));
    when(request.getMethod()).thenReturn("POST");
    when(request.getParameter("model")).thenReturn(this._model);
    when(request.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundary8REQtuY1QyQrEfzh");
    
    PowerMockito.mockStatic(FileHandler.class);    
    PowerMockito.when(FileHandler.receive(this._model, request)).thenReturn(packs);
    PowerMockito.when(FileHandler.toPipelineJobs(this._model, packs)).thenReturn(jobs);      
    
    // Call pipeline
    OXHandleData handler = new OXHandleData();
    handler.service(request, response);
    
    return writer.toString();
  }
  
  /**
   * @param model the model name
   * @param req the ContentRequest
   * @return the list of PackageData
   * @throws IOException when I/O error occur.
   * @throws OXException
   */
  private List<PackageData> mockedPackageList() throws IOException{
    List<PackageData> packs = new ArrayList<PackageData>();
    File dir = File.createTempFile("ox.allette.berlioz", ".tmp").getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    
    File file = new File(dir, this._input.getName());
    FileUtils.copy(this._input, file);
    PackageData pack = PackageData.newPackageData(this._model, file);
    if (pack != null) {
//      pack.setProperty("contenttype", item.getContentType());
      pack.setProperty("type", toType(this._input.getName()));
      pack.setProperty("name", toName(this._input.getName()));
      pack.saveProperties();
      for (Entry<String, String> parameter : parameters.entrySet()) {
        pack.setParameter(parameter.getKey(), parameter.getValue());
        pack.saveProperties();
      }
      packs.add(pack);
    }
    return packs;
  }
  
  /**
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
   * @return the name of file without extension.
   */
  private String toName(String filename) {
    int dot = filename.lastIndexOf('.');
    return filename.substring(0, dot);
  }
}
