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

import com.applause.auto.testrail.client.enums.TestResultStatus;
import com.applause.auto.testrail.client.models.config.TestRailDto;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/** A Utility class for interacting with TestRail */
@UtilityClass
@Slf4j
public class TestRailUtil {
  // TestRail default statuses. see http://docs.gurock.com/testrail-api2/reference-statuses
  /** The TestRail passed status */
  public static final int TESTRAIL_PASSED_STATUS_ID = 1;

  /** The TestRail blocked status */
  public static final int TESTRAIL_BLOCKED_STATUS_ID = 2;

  /** The TestRail untested status */
  public static final int TESTRAIL_UNTESTED_STATUS_ID = 3;

  /** The TestRail retest status */
  public static final int TESTRAIL_RETEST_STATUS_ID = 4;

  /** The TestRail failed status */
  public static final int TESTRAIL_FAILED_STATUS_ID = 5;

  /** A default status mapping */
  public static final Map<TestResultStatus, Integer> DEFAULT_STATUS_MAPPING;

  static {
    DEFAULT_STATUS_MAPPING = new HashMap<>();
    DEFAULT_STATUS_MAPPING.put(TestResultStatus.PASSED, TESTRAIL_PASSED_STATUS_ID);
    DEFAULT_STATUS_MAPPING.put(TestResultStatus.FAILED, TESTRAIL_FAILED_STATUS_ID);
    DEFAULT_STATUS_MAPPING.put(TestResultStatus.SKIPPED, TESTRAIL_BLOCKED_STATUS_ID);
    DEFAULT_STATUS_MAPPING.put(TestResultStatus.CANCELED, TESTRAIL_BLOCKED_STATUS_ID);
    DEFAULT_STATUS_MAPPING.put(TestResultStatus.ERROR, TESTRAIL_BLOCKED_STATUS_ID);
  }

  /**
   * Helps to extract out the TestRail test case id from a String
   *
   * @param testCaseId The test case id string
   * @return The parsed test case id
   */
  public static long extractTestCaseId(@NonNull final String testCaseId) {
    return Long.parseLong(testCaseId.replace("C", ""));
  }

  /**
   * Checks to make sure the test rails case id passed in is of a valid format
   *
   * @param testCaseId string to check
   * @return true if valid (or blank/null), false if not
   */
  public static boolean validateTestRailCaseId(final String testCaseId) {
    if (StringUtils.isBlank(testCaseId)) {
      return false;
    }
    // It's valid if it's a number or a number preceded by the character 'C'
    return Pattern.matches("C?\\d+", testCaseId);
  }

  /**
   * Returns the customer's custom TestRail status mappings
   *
   * @param testRailDto company's TestRail account
   * @return map from Applause TestResultStatus -> TestRail Test Result Status numeric ID
   */
  public static Map<TestResultStatus, Integer> buildCustomStatusMapping(
      @NonNull final TestRailDto testRailDto) {
    var mapping = new HashMap<TestResultStatus, Integer>();
    mapping.put(
        TestResultStatus.PASSED,
        Optional.ofNullable(testRailDto.getMappedStatusPassed())
            .orElse(getDefaultStatus(TestResultStatus.PASSED)));
    mapping.put(
        TestResultStatus.FAILED,
        Optional.ofNullable(testRailDto.getMappedStatusFailed())
            .orElse(getDefaultStatus(TestResultStatus.FAILED)));
    mapping.put(
        TestResultStatus.SKIPPED,
        Optional.ofNullable(testRailDto.getMappedStatusSkipped())
            .orElse(getDefaultStatus(TestResultStatus.SKIPPED)));
    mapping.put(
        TestResultStatus.ERROR,
        Optional.ofNullable(testRailDto.getMappedStatusError())
            .orElse(getDefaultStatus(TestResultStatus.ERROR)));
    mapping.put(
        TestResultStatus.CANCELED,
        Optional.ofNullable(testRailDto.getMappedStatusCanceled())
            .orElse(getDefaultStatus(TestResultStatus.CANCELED)));
    return mapping;
  }

  /**
   * Maps the Applause Test Result Status to TestRail status id. 1 = Passed 2 = Blocked 3 = Untested
   * (not allowed when adding a result) 4 = Retest 5 = Failed ...plus optional custom per-company
   * statuses. See "status_id" under Request Fields <a
   * href="http://docs.gurock.com/testrail-api2/reference-results#add_result">here</a>
   *
   * @param status our status enum
   * @param statusMapping optional custom applause -> TestRail result status mapping
   * @return appropriate TestRail ID enum
   */
  public static int getStatus(
      @NonNull final TestResultStatus status, final Map<TestResultStatus, Integer> statusMapping) {
    if (null == statusMapping) {
      log.warn("no custom status map passed in, going with default");
      return getDefaultStatus(status);
    }
    var customTestRailStatus = statusMapping.get(status);
    if (null == customTestRailStatus) {
      var defaultStatus = getDefaultStatus(status);
      log.warn("no value found for {} in the mapping, using default {}", status, defaultStatus);
      return defaultStatus;
    } else {
      log.debug(
          "return custom TestRail status id {} for {}", customTestRailStatus, status.getValue());
      return customTestRailStatus;
    }
  }

  /**
   * Returns the "default" mapped Applause -> TestRail test status
   *
   * @param status our status enum
   * @return appropriate TestRail ID enum
   */
  public static int getDefaultStatus(@NonNull final TestResultStatus status) {
    var defaultStatus = TestRailUtil.DEFAULT_STATUS_MAPPING.get(status);
    if (null == defaultStatus) {
      throw new IllegalArgumentException("Status " + status + " does not have a default");
    } else {
      return defaultStatus;
    }
  }

  /**
   * Cleans Plan Name passed in from Jenkins into a TestRail friendly looking name.
   *
   * @param originalPlanName the original plan name passed in from Jenkins
   * @return The cleaned testrail plan name
   */
  public static String cleanTestRailPlanName(final String originalPlanName) {
    // strip UTC from build timestamp that gets passed into the plan name
    String planName = originalPlanName.replace(" UTC", "");

    // replace underscores - only used for jenkins and reads better without
    planName = planName.replace("_", " - ");

    return planName;
  }
}
