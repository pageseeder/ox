/* Copyright (c) 2018 Allette Systems pty. ltd. */
package org.pageseeder.ox.tool;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The Class MultipleFilesResult.
 *
 * It for steps when they receive multiple files as input (by using a folder, Glob pattern or zip) and for file there
 * is a separate process.
 *
 * @author Carlos Cabral
 * @since 08th August 2018
 */
public class MultipleFilesResult<T extends FileResultInfo> extends DefaultResult implements Result {

  private static final Logger LOGGER = LoggerFactory.getLogger(MultipleFilesResult.class);

  /** The file result infos. */
  private final List<T> _fileResultInfos;

  /**
   * Instantiates a new multiple files result.
   *
   * @param model the model
   * @param data the data
   * @param info the info
   * @param output the output (it can only be a zip or a file or null)
   * @param fileResultInfos the file result infos
   */
  public MultipleFilesResult(@NonNull Model model, @NonNull PackageData data, @NonNull StepInfo info,
      @Nullable File output, @NonNull List<T> fileResultInfos) {
    super(model, data, info, output);
    this._fileResultInfos = fileResultInfos;
  }

  @Override
  protected void writeResultAttributes(XMLWriter xml) throws IOException {
    super.writeResultAttributes(xml);
    xml.attribute("inputs-number", this._fileResultInfos.size());
  }

  @Override
  protected void writeResultElements(XMLWriter xml) throws IOException {
    super.writeResultElements(xml);
    writeFileResultInfos(xml);
  }

  /**
   * Parameters XML.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeFileResultInfos(XMLWriter xml) throws IOException {
    xml.openElement("result-files");
    this._fileResultInfos.forEach(fileResultInfo -> writeFileResultInfo (xml, fileResultInfo));
    xml.closeElement();//parameters
  }

  /**
   * Parameters XML.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeFileResultInfo(XMLWriter xml, T fileResultInfo) {
    try {
      xml.openElement("result-file");
      xml.attribute("input", data().getPath(fileResultInfo.getInput()));
      xml.attribute("output", data().getPath(fileResultInfo.getOutput()));
      xml.attribute("status", fileResultInfo.getStatus().toString());
      xml.closeElement();//parameters
    } catch (IOException io) {
      LOGGER.error("Unable to generate file result info for {}-{}-{}", fileResultInfo.getInput(), fileResultInfo.getOutput(), fileResultInfo.getStatus());
    }
  }

  /**
   * Gets the file result infos.
   *
   * @return the file result infos
   */
  public List<? extends FileResultInfo> getFileResultInfos() {
    return _fileResultInfos;
  }
}
