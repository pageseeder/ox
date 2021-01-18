/* Copyright (c) 2018 Allette Systems pty. ltd. */
package org.pageseeder.ox.tool;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * The Class DefaultResult.
 *
 * @author Carlos Cabral
 * @since 08th August 2018
 */
public class DefaultResult extends ResultBase implements Result {

  /** The info. */
  private final StepInfo _info;

  /** The downloadable output, it can only be a zip or a file or null */
  private final File _output;

  /** Extra Information for this result. */
  private ExtraResultInfo extraResultInfo;

  /**
   * Instantiates a new Default Base.
   * The input information is get from step parameter
   *
   * @param model  the model
   * @param data   the data
   * @param info   the info
   * @param output the output (it can only be a zip or a file or null)
   */
  public DefaultResult(@NonNull Model model, @NonNull PackageData data, @NonNull StepInfo info, @Nullable File output) {
    super(model, data);
    this._info = info;
    this._output = output;
  }


  /* (non-Javadoc)
   * @see org.pageseeder.xmlwriter.XMLWritable#toXML(org.pageseeder.xmlwriter.XMLWriter)
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result");
    writeResultAttributes(xml);

    //Children Elements
    writeResultElements(xml);

    xml.closeElement();
  }

  /**
   * Write result attributes.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeResultAttributes(XMLWriter xml) throws IOException {
    xml.attribute("model", model().name());
    xml.attribute("id", data().id());
    xml.attribute("status", status().toString().toLowerCase());
    xml.attribute("time", Long.toString(time()));
    xml.attribute("downloadable", String.valueOf(isDownloadable()));
    xml.attribute("path", downloadPath());
    xml.attribute("input", input());
  }

  /**
   * Write result elements.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeResultElements(XMLWriter xml) throws IOException {
    //Step Info Perameters
    writeParameters(xml);

    //Extra Info
    writeExtraInfo(xml);

    // The details of any error
    writeError(xml);
  }

  /**
   * Parameters XML.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeParameters(XMLWriter xml) throws IOException {
    xml.openElement("parameters");

    for (Entry<String, String> entry : this.data().getParameters().entrySet() ) {
      valueXML(xml, "parameter", entry.getKey(), entry.getValue());
    }

    for (Entry<String, String> entry : this._info.parameters().entrySet() ) {
      valueXML(xml, "parameter", entry.getKey(), entry.getValue());
    }
    xml.closeElement();//parameters
  }


  /**
   * Parameters XML.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeExtraInfo(XMLWriter xml) throws IOException {
    if (this.getExtraResultInfo() != null) {
      this.getExtraResultInfo().toXML(xml);
    }
  }

  /**
   * Value XML.
   *
   * @param xml         the xml
   * @param elementName the element name
   * @param name        the name
   * @param value       the value
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void valueXML(XMLWriter xml, String elementName, String name, String value) throws IOException {
    xml.openElement(elementName);
    xml.attribute("name", name);
    xml.attribute("value", value);
    xml.closeElement();
  }

  /**
   * Write error.
   *
   * @param xml the xml
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeError(XMLWriter xml) throws IOException {
    if (this.error() != null) {
      OXErrors.toXML(error(), xml, true);
    }
  }

  /**
   * Download path.
   *
   * @return the string
   */
  public String downloadPath() {
    if (isDownloadable()) {
      return data().getPath(this._output);
    } else {
      return "";
    }
  }

  /* (non-Javadoc)
   * @see org.pageseeder.ox.tool.ResultBase#isDownloadable()
   */
  @Override
  public boolean isDownloadable() {
    return this._output != null && this._output.exists();
  }

  /**
   * Info.
   *
   * @return the step info
   */
  public StepInfo info() {
    return this._info;
  }


  /**
   * Input.
   *
   * @return the file
   */
  public String input() {
    return this._info.getParameter("input", this._info.input());
  }


  /**
   * Output.
   *
   * @return the file
   */
  public File output() {
    return this._output;
  }

  /**
   * Gets extra result info.
   *
   * @return the extra result info
   */
  public ExtraResultInfo getExtraResultInfo() {
    return extraResultInfo;
  }

  /**
   * Sets extra result info.
   *
   * @param extraResultInfo the extra result info
   */
  public void setExtraResultInfo(ExtraResultInfo extraResultInfo) {
    this.extraResultInfo = extraResultInfo;
  }
}
