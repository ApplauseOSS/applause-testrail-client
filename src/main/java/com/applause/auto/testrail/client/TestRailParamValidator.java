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

import com.applause.auto.testrail.client.errors.TestRailErrorStatus;
import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.config.TestRailStatusMaps;
import com.applause.auto.testrail.client.models.testrail.ProjectDto;
import com.applause.auto.testrail.client.models.testrail.StatusDto;
import com.applause.auto.testrail.client.models.testrail.TestCaseDto;
import com.applause.auto.testrail.client.models.testrail.TestSuiteDto;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/** Helper for validating the TestRail parameters */
@AllArgsConstructor
public class TestRailParamValidator {
  private final TestRailClient testRailClient;

  /**
   * Validates the given TestRail Project
   *
   * @param testRailProjectId The id of the project to validate
   * @return The TestRail Project
   * @throws TestRailException if we can't connect to TestRail or validation fails
   */
  public ProjectDto validateTestRailProjectId(long testRailProjectId) throws TestRailException {
    // Check that the project exists in TestRail and that we have access to it.
    ProjectDto project = this.testRailClient.getProject(testRailProjectId);

    // Verify that the project isn't marked as completed.
    if (project.isCompleted()) {
      throw new TestRailException(
          "Test Rail Project is Completed.", TestRailErrorStatus.ACCESS_DENIED);
    }

    return project;
  }

  /**
   * Validate a test suite
   *
   * @param suiteId The id of the test suite
   * @param testRailProjectId The project id that the suite maps to
   * @return The TestSuite
   * @throws TestRailException if the validation fails or the suite could not be grabbed.
   */
  public TestSuiteDto validateTestRailSuite(final long suiteId, final long testRailProjectId)
      throws TestRailException {
    // Try to grab the test suite from TestRail, verifying that it exists, and we have access to
    // view
    // it.
    TestSuiteDto suite = this.testRailClient.getTestSuite(suiteId);

    // Verify that the suite has not been marked as completed.
    if (suite.isCompleted()) {
      throw new TestRailException(
          "Test Rail Suite is marked as completed", TestRailErrorStatus.ACCESS_DENIED);
    }

    // Verify that the suite is for the projectId that was passed in.
    if (suite.getProjectId() != testRailProjectId) {
      throw new TestRailException(
          "Test Rail Suite does not match the given project", TestRailErrorStatus.ACCESS_DENIED);
    }

    return suite;
  }

  /**
   * Perform some internal validation of TestRail status codes, which have some peculiar logic.
   *
   * <p>The biggest 'gotcha' being that TestRail gives us an 'Untested' status but doesn't actually
   * allow it to be used. 2nd 'gotcha' is that TestRail configuration is changed outside of
   * Applause's-TestRail configuration - so they may get out of sync. (eg: we have old status codes)
   *
   * @return List of strings reporting failed validation issues. Empty list of none.
   * @throws TestRailException Failed validation issues.
   */
  public List<String> verifyResultStatusCodes(final TestRailStatusMaps statusMaps)
      throws TestRailException {
    List<String> results = new ArrayList<>();
    var statusList = this.testRailClient.getCustomStatuses();

    Set<Integer> statusIds = statusList.stream().map(StatusDto::getId).collect(Collectors.toSet());

    if (!statusIds.containsAll(statusMaps.getTestRailStatusIds())) {
      results.add(
          "Applause TestRail Status codes do not match TestRail status codes. Expect problems adding results to TestRail.");
    }

    // UNTESTED is internal to TestRail, and they disallow setting status as a result type
    if (statusMaps.getTestRailStatusIds().contains(TestRailUtil.TESTRAIL_UNTESTED_STATUS_ID)) {
      results.add(
          "Applause TestRail configured for invalid status code 3 (Untested). Expect problems adding results to TestRail.");
    }
    return results;
  }

  /**
   * Validates the given TestRail Parameters
   *
   * @param testRailProjectId The id of the TestRailProject
   * @param testRailSuiteId The id of the TestRail suite
   * @param testRailPlanName The name of the plan to use/create
   * @param testRailRunName The name of the run to create
   * @throws TestRailException if pre-validation fails
   */
  public void validateTestRailParams(
      @Nullable final Long testRailProjectId,
      @Nullable final Long testRailSuiteId,
      @Nullable final String testRailPlanName,
      @Nullable final String testRailRunName)
      throws TestRailException {

    // Otherwise, check all parameters
    final List<String> preValidationErrors = new ArrayList<>();

    if (StringUtils.isBlank(testRailPlanName)) {
      preValidationErrors.add("Invalid testRailPlanName: " + testRailPlanName);
    }
    if (StringUtils.isBlank(testRailRunName)) {
      preValidationErrors.add("Invalid testRailRunName: " + testRailRunName);
    }
    if (testRailProjectId == null || testRailProjectId < 0) {
      preValidationErrors.add("Invalid testRailProjectId: " + testRailProjectId);
    }
    if (testRailSuiteId == null || testRailSuiteId < 0) {
      preValidationErrors.add("Invalid testRailSuiteId: " + testRailSuiteId);
    }
    if (!preValidationErrors.isEmpty()) {
      throw new TestRailException(
          String.join(", ", preValidationErrors), TestRailErrorStatus.BAD_REQUEST);
    }
    this.validateTestRailProjectId(testRailProjectId);
    this.validateTestRailSuite(testRailSuiteId, testRailProjectId);
  }

  /**
   * Validates the provided TestRail configuration
   *
   * @param testRailProjectId The project ID to validate
   * @param testRailSuiteId The suite id to validate
   * @param testRailPlanName The plan name to validate
   * @param testRailRunName The run name to validate
   * @param statusMaps The TestRail status mapping
   * @throws TestRailException If validation fails
   */
  public void validateTestrailConfiguration(
      @Nullable final Long testRailProjectId,
      @Nullable final Long testRailSuiteId,
      @Nullable final String testRailPlanName,
      @Nullable final String testRailRunName,
      @Nullable final TestRailStatusMaps statusMaps)
      throws TestRailException {
    validateTestRailParams(testRailProjectId, testRailSuiteId, testRailPlanName, testRailRunName);
    if (statusMaps != null) {
      verifyResultStatusCodes(statusMaps);
    }
  }

  /**
   * Validates that the provided case ids belong to the suite
   *
   * @param projectId The TestRail project ID
   * @param suiteId The TestRail suite ID
   * @param testRailCaseIds A set of TestRail case ids
   * @throws TestRailException If validation fails
   */
  public void validateTestCaseIds(
      final long projectId, final long suiteId, @NonNull final Set<String> testRailCaseIds)
      throws TestRailException {
    final ImmutableSet<Long> testCasesInTestRail =
        this.testRailClient.getTestCasesForSuite(projectId, suiteId).stream()
            .map(TestCaseDto::getId)
            .collect(ImmutableSet.toImmutableSet());
    for (final String caseId : testRailCaseIds) {
      if (caseId == null) {
        continue;
      }
      // make sure its valid first
      if (!TestRailUtil.validateTestRailCaseId(caseId)) {
        throw new TestRailException(
            "Test case ID " + caseId + " is invalid", TestRailErrorStatus.CASE_ID);
      }
      // trim it
      long filteredTestCaseId = TestRailUtil.extractTestCaseId(caseId);
      // make sure case exists on testrail
      if (!testCasesInTestRail.contains(filteredTestCaseId)) {
        throw new TestRailException(
            "Test Case ID " + filteredTestCaseId + " has no matching case in Testrail",
            TestRailErrorStatus.CASE_ID);
      }
    }
  }
}
