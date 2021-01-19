package org.pageseeder.ox.util;

import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.parameters.ParameterTemplate;

import java.io.File;
import java.util.Map;

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
   * This method also implements dynamic parameter.
   * It means that a parameter can have a value like "starting-text{extra-text}ending-text". If the extra-text is a
   * existing parameter in the PackageData or StepInfo, then the {extra-text} will be replaced by its value.
   * If extra-text does not exist, then it will be replaced by an empty value.
   *
   * In addition, the dynamic parameter can have a dafault value {extra-text=default extra text}
   *
   * @param data PackageData
   * @param info StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback default value.
   * @return
   */
  public static String getParameter(PackageData data, StepInfo info, String parameterName, String fallback) {
    String parameterValue = info.getParameter(parameterName, data.getParameter(parameterName));
    if (StringUtils.isBlank(parameterValue)) {
      parameterValue = fallback;
    }

    //Add request parameters
    Map<String, String> parameters = data.getParameters();
    //Add step parameters
    parameters.putAll(info.parameters());

    ParameterTemplate parameterTemplate = ParameterTemplate.parse(parameterValue);
    parameterValue = parameterTemplate.toString(parameters);


    return parameterValue;
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
