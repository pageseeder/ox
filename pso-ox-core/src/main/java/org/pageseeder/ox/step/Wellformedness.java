/* Copyright (c) 2014 Allette Systems pty. ltd. */
package org.pageseeder.ox.step;

import org.pageseeder.ox.OXErrors;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.tool.InvalidResult;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * <p>A step to validate the original input XML file is wellformedness </p>.
 *
 * <h3>Step Parameters</h3>
 * <ul>
 * <li><var>input</var> the xml file needs to validate, which is a relative path in {@link PackageData} .</li>
 * </ul>
 *
 * <h3>Return</h3>
 * <p>If <var>input</var> does not exist, it returns {@link InvalidResult }.</p>
 * <p>Otherwise return {@link Result} </p>
 *
 * @author Ciber Cai
 * @since  12 June 2014
 */
public final class Wellformedness implements Step {

  private static final Logger LOGGER = LoggerFactory.getLogger(Wellformedness.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    String xml = info.getParameter("input", info.input());
    File source = data.getFile(xml);

    // throw the error
    if (source == null || !source.exists()) { return new InvalidResult(model, data)
        .error(new FileNotFoundException("Cannot find the source file " + xml + ".")); }

    LOGGER.debug("xml file {} ", xml);
    WellformednessResult result = isWellformedness(model, data, source);
    return result;
  }

  /**
   * @param model the Model
   * @param data the PackageData
   * @param file the file require to validate
   * @return the Result of the well-formedness process.
   */
  private static WellformednessResult isWellformedness(Model model, PackageData data, File file) {
    WellformednessResult result = new WellformednessResult(model, data);
    if (file != null && file.exists()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      try (InputStream in = new FileInputStream(file)) {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(in);
      } catch (ParserConfigurationException | SAXException | IOException ex) {
        result.setError(ex);
        result.setStatus(ResultStatus.ERROR);
      }
      result.done();
    }
    return result;
  }

  /**
   * The result of wellformedness check.
   * @author Ciber Cai
   * @since  16 June 2014
   */
  private static class WellformednessResult extends ResultBase implements Result {

    private WellformednessResult(Model model, PackageData data) {
      super(model, data);
    }

    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("result");
      xml.attribute("name", "Wellformedness-Result");
      xml.attribute("id", data().id());
      xml.attribute("model", model().name());
      xml.attribute("status", status().toString().toLowerCase());
      xml.attribute("time", Long.toString(time()));
      xml.attribute("downloadable", String.valueOf(isDownloadable()));

      // Print the details of any error
      if (error() != null) {
        OXErrors.toXML(error(), xml, true);
      } else {
        xml.writeText("Well-form XML");
      }
      xml.closeElement();// result
    }

    @Override
    public boolean isDownloadable() {
      return false;
    }
  }
}
