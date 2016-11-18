/*
 *  Copyright (c) 2014 Allette Systems pty. ltd.
 */
package org.pageseeder.ox.diffx.step;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.diffx.tool.DiffTextCommand;
import org.pageseeder.ox.step.Wellformedness;
import org.pageseeder.ox.tool.InvalidResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A Text Comparison processing step</p>
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>source</var> It could be DOCX, HTML or PSML and will be compared with the target</li>
 *  <li><var>target</var> It could be DOCX, HTML or PSML and will be compared by the source. It the target is not send
 *  It gets the input.</li>
 *  <li><var>xsl-psml</var> the specified transformation file for manipulated the psml before diff it. (Optional) </li>
 *  <li><var>xsl-html</var> the specified transformation file for manipulated the html before diff it. (Optional) </li>
 *  <li><var>xsl-docx</var> the specified transformation file for manipulated the docx before diff it. (Optional) </li>
 * </ul>
 *
 *
 * <h3>How it works</h3>
 * <p>In order to identify what kind of file it is. It will use the extension.
 * <p>However when the docx is simplified it will be saved in the folder simplified, therefore every time when it
 * receives the folder simplified it will treat as docx.
 * <p> To send the DOCX you can send the whole file docx or the simplified folder created by the {@link Simplify} service.
 *
 * @author Carlos Cabral
 * @version 12 April 2015
 */
public class DiffText implements Step {

  private static final Logger LOGGER = LoggerFactory.getLogger(Wellformedness.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Diff Text.");
    String sourcePath = info.getParameter("source");
    String targetPath = info.getParameter("target");
    if (isBlank(targetPath)) {
      targetPath = info.input();
    }
    File source = data.getFile(sourcePath);
    File target = data.getFile(targetPath);

    // Check the source first
    if (!valid(source)) return new InvalidResult(model, data).error(new IllegalArgumentException("The source is invalid"));

    // Then validate the target
    if (!valid(target)) return new InvalidResult(model, data).error(new IllegalArgumentException("The target is invalid"));

    // only process when it exists
    DiffTextCommand command = new DiffTextCommand(model, sourcePath, targetPath);

    // set the templates for psml
    if (info.getParameter("xsl-psml") != null) {
      try {
        Templates templates = model.getTemplates(info.getParameter("xsl-psml"));
        command.setPSMLTransformation(templates);
      } catch (IOException | TransformerConfigurationException ex) {
        LOGGER.error("Cannot find the template {} defined in xsl-psml parameter. ", info.getParameter("xsl-psml"), ex);
      }

    }
    // set the templates for html
    if (info.getParameter("xsl-html") != null) {
      try {
        Templates templates = model.getTemplates(info.getParameter("xsl-html"));
        command.setHTMLTransformation(templates);
      } catch (IOException | TransformerConfigurationException ex) {
        LOGGER.error("Cannot find the template {} defined in xsl-html parameter. ", info.getParameter("xsl-html"), ex);
      }
    }
    // set the templates for docx
    if (info.getParameter("xsl-docx") != null) {
      try {
        Templates templates = model.getTemplates(info.getParameter("xsl-docx"));
        command.setDOCXTransformation(templates);
      } catch (IOException | TransformerConfigurationException ex) {
        LOGGER.error("Cannot find the template {} defined in xsl-docx parameter. ", info.getParameter("xsl-docx"), ex);
      }
    }

    return command.process(data);
  }

  private static boolean valid(File file) {
    if (file == null || !file.exists()) {
      LOGGER.warn("Cannot find file {} exist {} .", file);
      return false;
    }
    return true;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
