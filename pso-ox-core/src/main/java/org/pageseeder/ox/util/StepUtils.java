package org.pageseeder.ox.util;

import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.parameters.ParameterTemplate;

import java.io.File;
import java.util.HashMap;
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
      finput = data.getFile(applyDynamicParameterLogic(data, info, input));
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
      foutput = data.getFile(applyDynamicParameterLogic(data, info, output));
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
    return applyDynamicParameterLogic(data, info, parameterValue);
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

  /**
   * The a request or step parameter can have a dynamic value base in another one.
   *
   * Example:
   * Package data has parameter "root-folder"  with value "data"
   * parameterValue = /{root-folder}/file.xml  =>
   * this method returns /data/file.xml
   *
   *
   * @param data
   * @param info
   * @param parameterValue
   * @return
   */
  public static String applyDynamicParameterLogic(PackageData data, StepInfo info, String parameterValue) {

    Map<String, String> parameters = new HashMap<>();

    //Add request parameters
    if (data != null) {
      parameters.putAll(data.getParameters());
    }

    //Add step parameters
    if (info != null) {
      parameters.putAll(info.parameters());
    }
    return applyDynamicParameterLogic(parameterValue, parameters, 1);
  }

  /**
   * The a request or step parameter can have a dynamic value base in another one.
   *
   * Example:
   * Package data has parameter "root-folder"  with value "data"
   * parameterValue = /{root-folder}/file.xml  =>
   * this method returns /data/file.xml
   *
   * @param parameterValue
   * @param parameters A map with all parameters from PackageData and StepInfo.
   * @param loopCount The dynamic parameter logic allows to use dynamic values within dynamic values. The loop count
   *                  will how much time it has been doing it. As it will be limited to 2 times
   * @return
   */
  private static String applyDynamicParameterLogic(String parameterValue, Map<String, String> parameters, int loopCount) {
    String newValue = parameterValue;
    if (!StringUtils.isBlank(parameterValue)) {
      ParameterTemplate parameterTemplate = ParameterTemplate.parse(parameterValue);
      newValue = parameterTemplate.toString(parameters);
      loopCount++;
      if (loopCount <= 2) {
        newValue = applyDynamicParameterLogic(newValue, parameters, loopCount);
      }
    }
    return newValue;
  }
}
