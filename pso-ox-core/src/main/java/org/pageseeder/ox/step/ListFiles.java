/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.step;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.FilesFinder;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A step return a list of the path files.
 * @param model the model
 * @param data the data
 * @param info the info
 * @param globParam Default: all recursive folders and files. If it is added you can use a glob pattern.
 * @return a XML output file with a list of paths*
 *            *
 * @author Adriano Akaishi
 * @author Carlos Cabral
 * @since  04/02/2021
 */
public final class ListFiles implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(ListFiles.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    DefaultResult result = null;
    File input = StepUtils.getInput(data, info);
    File output = StepUtils.getOutput(data, info, input);
    /* Default: all recursive folders and files. If it is added you can use a glob pattern. */
    String globParam = info.getParameter("glob-pattern", "**/*.*");

    /* Valid input : a folder */
    if (input.getName().indexOf(".") > -1) {
      LOGGER.error("Unable to read a list of PDF files. It is not a folder input. Path input: {}", input.getName());
    } else {
      /* Create a folder output */
      if (output.getName().indexOf(".") > -1) {
        output.getParentFile().mkdir();

        result = new DefaultResult(model, data, info, output);
        boolean isInputValid = input != null && input.exists();
        boolean isOutputValid = output != null;
        LOGGER.info("Input file {} and output {}. Input valid = {}. Output valid = {}", input, output, isInputValid, isOutputValid);

        // only process when input is exists
        if (isInputValid && isOutputValid) {

          XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
          writer.openElement("files");

          /* Find files and print a element for each file found */
          FilesFinder finder = new FilesFinder(globParam, input);
          List<File> files = finder.getFiles();
          for (File inputFile:files) {
            writer.openElement("file");
            writer.attribute("path", inputFile.getAbsolutePath());
            writer.attribute("sort-path", data.getPath(inputFile));
            writer.closeElement();//file
          }

          writer.closeElement();//files
          writer.close();
          try {
            FileUtils.write(writer.toString(), output);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }else{
        LOGGER.error("It is not a output file. Path output: {}", output.getName());
      }
    }
    return result;

  }

}

