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

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ccabral
 * @since 19 January 2021
 */
public class StepUtilsTest {

  @Test
  public void getParameter_LiteralValueFromPackageData_Success(){
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test","Data test value");
    PackageData data = createPackageData(requestParameters);
    StepInfo info = createStepInfo(new HashMap<>());
    Assert.assertEquals("Data test value", StepUtils.getParameter(data, info,"test", ""));
  }

  @Test
  public void getParameter_LiteralValueFromStepInfo_Success(){
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test","Info test value");
    PackageData data = createPackageData(new HashMap<>());
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("Info test value", StepUtils.getParameter(data, info,"test", ""));
  }

  @Test
  public void getParameter_LiteralValueFromBoth_Success(){
    //Both means DataPAckage and StepInfo
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-data","Data test value");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-info","Info test value");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("Data test value", StepUtils.getParameter(data, info,"test-data", ""));
    Assert.assertEquals("Info test value", StepUtils.getParameter(data, info,"test-info", ""));
  }

  @Test
  public void getParameter_DynamicValueFromBoth_Success(){
    //Both means DataPAckage and StepInfo
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-data","{extra-info} data test value");
    requestParameters.put("extra","extra text");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-info","Info test value {extra}");
    stepParameters.put("extra-info","extra info text");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("extra info text data test value", StepUtils.getParameter(data, info,"test-data", ""));
    Assert.assertEquals("Info test value extra text", StepUtils.getParameter(data, info,"test-info", ""));
  }

  @Test
  public void getParameter_NonExistingDynamicValueFromBoth_Success(){
    //Both means DataPAckage and StepInfo
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-info-non-exist","Info test value {extra-non-exist}");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("Info test value ", StepUtils.getParameter(data, info,"test-info-non-exist", ""));
  }

  @Test
  public void getParameter_DefaultDynamicValueFromBoth_Success(){
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-info-non-exist","Info test value {extra-non-exist=default extra text}");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("Info test value default extra text", StepUtils.getParameter(data, info,"test-info-non-exist", ""));
  }

  @Test
  public void getParameter_NonExistingValueAndNullFallback_null(){
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test","Data test value");
    PackageData data = createPackageData(requestParameters);
    StepInfo info = createStepInfo(new HashMap<>());
    Assert.assertNull(StepUtils.getParameter(data, info,"non-existing", null));
  }

  @Test
  public void getParameterIntWithoutDynamicLogic_fromRequest_valid() {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("int", "1");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(1, StepUtils.getParameterIntWithoutDynamicLogic(data, info, "int", 0));
  }


  @Test
  public void getParameterIntWithoutDynamicLogic_fromRequest_invalid() {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("int", "a");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(0, StepUtils.getParameterIntWithoutDynamicLogic(data, info, "int", 0));
  }

  @Test
  public void getParameterIntWithoutDynamicLogic_fromStep_valid() {
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("int", "2");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(2, StepUtils.getParameterIntWithoutDynamicLogic(data, info, "int", 0));
  }

  @Test
  public void getParameterIntWithoutDynamicLogic_fromDefault_valid() {
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(0, StepUtils.getParameterIntWithoutDynamicLogic(data, info, "int", 0));
  }

  @Test
  public void getParameterLongWithoutDynamicLogic_fromRequest_valid() {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("long", "1");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(1L, StepUtils.getParameterLongWithoutDynamicLogic(data, info, "long", 0L));
  }


  @Test
  public void getParameterLongWithoutDynamicLogic_fromRequest_invalid() {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("long", "a");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(0L, StepUtils.getParameterLongWithoutDynamicLogic(data, info, "long", 0L));
  }

  @Test
  public void getParameterLongWithoutDynamicLogic_fromStep_valid() {
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("long", "2");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(2L, StepUtils.getParameterLongWithoutDynamicLogic(data, info, "long", 0L));
  }

  @Test
  public void getParameterLongWithoutDynamicLogic_fromDefault_valid() {
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(0, StepUtils.getParameterLongWithoutDynamicLogic(data, info, "long", 0L));
  }

  @Test
  public void applyDynamicParameterLogic_emptyString_empty() {
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    StepInfo info = createStepInfo(new HashMap<>());
    Assert.assertEquals("", StepUtils.applyDynamicParameterLogic(data, info, ""));
  }

  @Test
  public void applyDynamicParameterLogic_nullPackageData_empty () {
    PackageData data = null;
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-info","Info test value");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals(stepParameters.get("Info test value"), StepUtils.applyDynamicParameterLogic(data, info,stepParameters.get("Info test value")));
  }

  @Test
  public void applyDynamicParameterLogic_loopCountOne_success () {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-1","{test-2}");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-2","value-2");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("value-2", StepUtils.applyDynamicParameterLogic(data, info, requestParameters.get("test-1")));
  }

  @Test
  public void applyDynamicParameterLogic_loopCountTwo_success () {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-1","{test-2}");
    requestParameters.put("test-3","value-3");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-2","{test-3}");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("value-3", StepUtils.applyDynamicParameterLogic(data, info, requestParameters.get("test-1")));
  }

  @Test
  public void applyDynamicParameterLogic_loopCountMaxDefault_success () {
    // Maximum 2 {test-2} (first) -> {test-3} (second) -> {test-4} (third not allowed)
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-1","{test-2}");
    requestParameters.put("test-3","{test-4}");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-2","{test-3}");
    stepParameters.put("test-4","value-4");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("{test-4}", StepUtils.applyDynamicParameterLogic(data, info, requestParameters.get("test-1")));
  }

  @Test
  public void applyDynamicParameterLogic_loopCountEqualN_success () {
    // Maximum 2 {test-2} (first) -> {test-3} (second) -> {test-4} (third not allowed)
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-1","{test-2}");
    requestParameters.put("test-3","{test-4}");
    requestParameters.put("dynamic-param-max-cycle","3");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-2","{test-3}");
    stepParameters.put("test-4","value-4");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("value-4", StepUtils.applyDynamicParameterLogic(data, info, requestParameters.get("test-1")));
  }

  @Test
  public void applyDynamicParameterLogic_loopCountEqualNPlusOne_success () {
    // Maximum 2 {test-2} (first) -> {test-3} (second) -> {test-4} (third not allowed)
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test-1","{test-2}");
    requestParameters.put("test-3","{test-4}");
    requestParameters.put("dynamic-param-max-cycle","2");
    PackageData data = createPackageData(requestParameters);
    Map<String, String> stepParameters = new HashMap<>();
    stepParameters.put("test-2","{test-3}");
    stepParameters.put("test-4","value-4");
    StepInfo info = createStepInfo(stepParameters);
    Assert.assertEquals("{test-4}", StepUtils.applyDynamicParameterLogic(data, info, requestParameters.get("test-1")));
  }

  @Test
  public void applyDynamicParameterLogic_nullStepInfo_empty () {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("test","Data test value");
    PackageData data = createPackageData(requestParameters);
    StepInfo info = null;
    Assert.assertEquals(requestParameters.get("test"), StepUtils.applyDynamicParameterLogic(data, info, requestParameters.get("test")));
  }


  @Test
  public void applyDynamicParameterLogic_nullString_null() {
    Map<String, String> requestParameters = new HashMap<>();
    PackageData data = createPackageData(requestParameters);
    StepInfo info = createStepInfo(new HashMap<>());
    Assert.assertNull(StepUtils.applyDynamicParameterLogic(data, info, null));
  }

  private PackageData createPackageData(Map<String, String> requestParameters) {
    try {
      Path tempFile = Files.createTempFile("test", "unit");
      PackageData data = PackageData.newPackageData("fake", tempFile.toFile());
      if (requestParameters != null) {
        for (Map.Entry<String, String> parameter : requestParameters.entrySet()) {
          data.setParameter(parameter.getKey(), parameter.getValue());
        }
      }
      return data;
    } catch (IOException io) {
      Assert.fail(io.getMessage());
    }
    return null;
  }

  private StepInfo createStepInfo (Map<String, String> stepParameters) {
    return new StepInfoImpl("id", "name", "input", "output", stepParameters);
  }
}
