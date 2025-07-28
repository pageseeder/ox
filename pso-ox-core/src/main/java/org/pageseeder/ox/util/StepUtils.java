/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.util;

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
   *
   * @param data the data
   * @param info the info
   * @return input input
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
   * @param data  the data
   * @param info  the info
   * @param input the input
   * @return output output
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
   * <p>
   * This method also implements dynamic parameter.
   * It means that a parameter can have a value like "starting-text{extra-text}ending-text". If the extra-text is a
   * existing parameter in the PackageData or StepInfo, then the {extra-text} will be replaced by its value.
   * If extra-text does not exist, then it will be replaced by an empty value.
   * <p>
   * In addition, the dynamic parameter can have a dafault value {extra-text=default extra text}
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter parameter
   */
  public static String getParameter(PackageData data, StepInfo info, String parameterName, String fallback) {
    String parameterValue = getParameterWithoutDynamicLogic(data, info, parameterName, fallback);
    return applyDynamicParameterLogic(data, info, parameterValue);
  }


  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise, returns the fallback.
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter without dynamic logic
   */
  public static String getParameterWithoutDynamicLogic(PackageData data, StepInfo info, String parameterName, String fallback) {
    String parameterValue = null;

    if (info != null) {
      parameterValue = info.getParameter(parameterName);
    }

    if (StringUtils.isBlank(parameterValue) && data != null) {
      parameterValue = data.getParameter(parameterName);
    }

    if (StringUtils.isBlank(parameterValue)) {
      parameterValue = fallback;
    }
    return parameterValue;
  }

  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter int
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
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter int without dynamic logic
   */
  public static int getParameterIntWithoutDynamicLogic(PackageData data, StepInfo info, String parameterName, int fallback) {
    int value = fallback;
    String parameter = getParameterWithoutDynamicLogic(data, info, parameterName, null);

    if (!StringUtils.isBlank(parameter)) {
      try {
        value = Integer.parseInt(parameter);
      } catch (NumberFormatException ex) {
        value = fallback;
      }
    }
    return value;
  }

  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter long
   */
  public static long getParameterLong(PackageData data, StepInfo info, String parameterName, long fallback) {
    String parameter = getParameter(data, info, parameterName, String.valueOf(fallback));
    try {
      return Long.parseLong(parameter);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }

  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter long without dynamic logic
   */
  public static long getParameterLongWithoutDynamicLogic(PackageData data, StepInfo info, String parameterName, long fallback) {
    long value = fallback;

    String parameter = getParameterWithoutDynamicLogic(data, info, parameterName, null);

    if (!StringUtils.isBlank(parameter)) {
      try {
        value = Long.parseLong(parameter);
      } catch (NumberFormatException ex) {
        value = fallback;
      }
    }
    return value;
  }

  /**
   * Get the parameter from step definition, if it is not found then gets from the request parameter.
   * Otherwise returns the fallback.
   *
   * @param data          PackageData
   * @param info          StepInfo
   * @param parameterName The name of the parameter to get from step info or package data
   * @param fallback      default value.
   * @return parameter char
   */
  public static char getParameterChar(PackageData data, StepInfo info, String parameterName, char fallback) {
    String parameter = getParameter(data, info, parameterName, String.valueOf(fallback));
    if (StringUtils.isBlank(parameter)) {
      return fallback;
    } else {
      return parameter.trim().charAt(0);
    }
  }

  /**
   * The a request or step parameter can have a dynamic value base in another one.
   * <p>
   * Example:
   * Package data has parameter "root-folder"  with value "data"
   * parameterValue = /{root-folder}/file.xml
   * this method returns /data/file.xml
   *
   * @param data           the data
   * @param info           the info
   * @param parameterValue the parameter value
   * @return string string
   */
  public static String applyDynamicParameterLogic(PackageData data, StepInfo info, String parameterValue) {

    Map<String, String> parameters = new HashMap<>();

    //Add request parameters
    if (data != null) {
      parameters.putAll(data.getParameters());
      parameters.put("_uploaded_file", data.getProperty("_original_file", ""));
      parameters.put("_package_id", data.id());
    }

    //Add step parameters
    if (info != null) {
      parameters.putAll(info.parameters());
      parameters.put("_input", info.input());
    }

    int maxLoopAllowed = getParameterIntWithoutDynamicLogic(data, info, "dynamic-param-max-cycle", 2);


    return applyDynamicParameterLogic(parameterValue, parameters, 1, maxLoopAllowed);
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
   *                  store which cycle it is. As it will be limited by maxLoopAllowed.
   * @param maxLoopAllowed
   * @return
   */
  private static String applyDynamicParameterLogic(String parameterValue, Map<String, String> parameters, int loopCount, int maxLoopAllowed) {
    String newValue = parameterValue;
    if (!StringUtils.isBlank(parameterValue)) {
      ParameterTemplate parameterTemplate = ParameterTemplate.parse(parameterValue);
      newValue = parameterTemplate.toString(parameters);
      loopCount++;
      if (loopCount <= maxLoopAllowed) {
        newValue = applyDynamicParameterLogic(newValue, parameters, loopCount, maxLoopAllowed);
      }
    }
    return newValue;
  }
}
