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

  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data PackageData
   * @param info StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback default value.
   * @return
   */
  public static String getParameter(PackageData data, StepInfo info, String parameterName, String fallback) {
    String parameter = info.getParameter(parameterName, data.getParameter(parameterName));

    return !StringUtils.isBlank(parameter) ? parameter : fallback;
  }

  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data PackageData
   * @param info StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback default value.
   * @return
   */
  public static int getParameterInt(PackageData data, StepInfo info, String parameterName, int fallback) {
    String parameter = getParameter(data, info, parameterName, String.valueOf(fallback));
    try {
      return Integer.parseInt(parameter);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }
}
