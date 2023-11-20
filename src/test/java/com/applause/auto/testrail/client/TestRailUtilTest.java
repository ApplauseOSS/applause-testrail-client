/*
*
* Copyright © 2023 Applause App Quality, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package com.applause.auto.testrail.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.applause.auto.testrail.client.enums.TestResultStatus;
import com.applause.auto.testrail.client.models.config.TestRailDto;
import org.junit.jupiter.api.Test;

public class TestRailUtilTest {

  @Test
  public void testValidateTestRailCaseIdNull() {
    assertFalse(TestRailUtil.validateTestRailCaseId(null), "should never allow a null value");
  }

  @Test
  public void testValidateTestRailCaseIdBlank() {
    assertFalse(TestRailUtil.validateTestRailCaseId(""), "should never allow a null value");
  }

  @Test
  public void testValidateTestRailCaseIdPositive() {
    assertTrue(TestRailUtil.validateTestRailCaseId("1"), "small number should work");
    assertTrue(
        TestRailUtil.validateTestRailCaseId("1234567890987654321"), "large number should work");
    assertTrue(TestRailUtil.validateTestRailCaseId("C1"), "small number with C prefix should work");
    assertTrue(
        TestRailUtil.validateTestRailCaseId("C1234567890987654321"),
        "big number with C prefix should work");
  }

  @Test
  public void testValidateTestRailCaseIdNegative() {
    assertFalse(
        TestRailUtil.validateTestRailCaseId("abc"), "non-number characters should not work");
    assertFalse(TestRailUtil.validateTestRailCaseId("123.456"), "floats should not work");
    assertFalse(TestRailUtil.validateTestRailCaseId("-17643"), "negative numbers should not work");

    assertFalse(TestRailUtil.validateTestRailCaseId("`"), "non-number character should not work");
    assertFalse(TestRailUtil.validateTestRailCaseId("FTX-680"), "JIRA callouts should not work");
    assertFalse(TestRailUtil.validateTestRailCaseId("[null]"), "fake null values should not work");
  }

  @Test
  public void testBuildCustomStatusMap() {
    final var statusPassed = 931;
    final var statusFailed = 446;
    final var statusSkipped = 1112;
    final var statusError = 12421;
    final var statusCanceled = 8465;

    final var testRailDto =
        new TestRailDto()
            .setMappedStatusPassed(statusPassed)
            .setMappedStatusFailed(statusFailed)
            .setMappedStatusSkipped(statusSkipped)
            .setMappedStatusError(statusError)
            .setMappedStatusCanceled(statusCanceled);

    final var returnedMap = TestRailUtil.buildCustomStatusMapping(testRailDto);

    assertEquals(
        statusPassed,
        returnedMap.get(TestResultStatus.PASSED).intValue(),
        "should have right property");
    assertEquals(
        statusFailed,
        returnedMap.get(TestResultStatus.FAILED).intValue(),
        "should have right property");
    assertEquals(
        statusSkipped,
        returnedMap.get(TestResultStatus.SKIPPED).intValue(),
        "should have right property");
    assertEquals(
        statusError,
        returnedMap.get(TestResultStatus.ERROR).intValue(),
        "should have right property");
    assertEquals(
        statusCanceled,
        returnedMap.get(TestResultStatus.CANCELED).intValue(),
        "should have right property");
  }
}
