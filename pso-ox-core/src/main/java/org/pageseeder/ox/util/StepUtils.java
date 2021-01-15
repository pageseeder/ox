package org.pageseeder.ox.util;

import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.PackageData;

import java.io.File;

/**
 * The type Step utils.
 *
 * @author ccabral
 * @since 15 January 2021
 */
public class StepUtils {
  /**
   * Get the input from the step definition, but if it is empty then it uses the file uploaded.
   * It accepts glob pattern.
   * @param data
   * @param info
   * @return
   */
  public static File getInput(PackageData data, StepInfo info) {
    // input file
    String input = info.getParameter("input", info.input());

    File finput = null;
    if (!StringUtils.isBlank(input)) {
      finput = data.getFile(input);
    }
    return finput;
  }

  /**
   * Get the output from step definition. If does not find it, then it gets the parent folder of the input.
   * If the input is null then get the package folder.
   * It accepts glob pattern.
   *
   * @param data
   * @param info
   * @param input
   * @return
   */
  public static File getOutput(PackageData data, StepInfo info, File input) {
    // output file
    String output = info.getParameter("output", info.output());
    File foutput = null;
    if (StringUtils.isBlank(output)) {
      if (input != null) {
        foutput = input.getParentFile();
      } else {
        foutput = data.directory();
      }
    } else {
      foutput = data.getFile(output);
    }
    return foutput;
  }
}
