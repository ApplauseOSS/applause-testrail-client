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

import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.internal.TestRailRunsAndInvalidCases;
import com.applause.auto.testrail.client.models.internal.TestRailStatusComment;
import com.applause.auto.testrail.client.models.internal.TestRailValidateRequest;
import com.applause.auto.testrail.client.models.testrail.PlanDto;
import com.applause.auto.testrail.client.models.testrail.TestCaseDto;
import com.applause.auto.testrail.client.models.testrail.TestDto;
import com.applause.auto.testrail.client.models.testrail.TestRunDto;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/** Handles reporting results to TestRail */
@AllArgsConstructor
@Slf4j
public class TestRailResultLogger {
  private final TestRailClient testRailClient;

  /**
   * Executes the requests against TestRail. This will set up or verify that all the objects on
   * TestRail side, so we can post the results. All the initialization should be performed in
   * resultsToLogByRunNameAndCaseId.
   *
   * @param resultsToLogByRunNameAndCaseId Multi-key map of Results by runName and caseId
   * @param validateRequest the object with params required for posting to testrail. project, suite,
   *     etc
   * @return the invalid testcaseIds
   * @throws TestRailException if execution fails
   */
  public Set<Long> execute(
      @NonNull final Table<String, Long, TestRailStatusComment> resultsToLogByRunNameAndCaseId,
      @NonNull final TestRailValidateRequest validateRequest)
      throws TestRailException {
    log.debug(
        "Starting TestRail reporting for suite {} plan {} planName {}",
        validateRequest.suiteId(),
        validateRequest.planId(),
        validateRequest.planName());
    List<TestCaseDto> testCases =
        this.testRailClient.getTestCasesForSuite(
            validateRequest.projectId(), validateRequest.suiteId());

    log.trace("Verifying TestRail setup.");
    // Verify that all the plans / runs are set up correctly on TestRail's end
    final var runsAndInvalidCases =
        this.verifyAndInitializeSetup(testCases, resultsToLogByRunNameAndCaseId, validateRequest);

    for (final var runEntry : runsAndInvalidCases.getRunDtosByName().entrySet()) {
      String runEntryKey = runEntry.getKey();
      Long runEntryValueId = runEntry.getValue().getId();
      this.testRailClient.addResults(
          runEntryValueId, resultsToLogByRunNameAndCaseId.row(runEntryKey));
    }

    // Let the caller know if there were some case ids we filtered out
    // these will end up error out, so we don't try to re-log them again
    return runsAndInvalidCases.getInvalidCaseIds();
  }

  private TestRailRunsAndInvalidCases verifyAndInitializeSetup(
      @NonNull final List<TestCaseDto> testCases,
      @NonNull final Table<String, Long, TestRailStatusComment> resultsToLogByRunNameAndCaseId,
      @NonNull final TestRailValidateRequest validateRequest)
      throws TestRailException {
    final TestRailRunsAndInvalidCases runsAndInvalidCases = new TestRailRunsAndInvalidCases();

    log.trace(
        "Filtering out bad caseIds for project "
            + validateRequest.projectId()
            + " and suite "
            + validateRequest.suiteId());
    // Collect the set of all case ids in the suite, and all the ones we are trying to add
    final var caseIdsForSuite =
        testCases.stream().map(TestCaseDto::getId).collect(Collectors.toSet());
    final var invalidCaseIds =
        this.filterOutBadCaseIds(caseIdsForSuite, resultsToLogByRunNameAndCaseId);
    runsAndInvalidCases.setInvalidCaseIds(invalidCaseIds);

    // Verify Plan
    final PlanDto planDto =
        this.verifyOrCreatePlan(
            validateRequest.projectId(), validateRequest.planId(), validateRequest.planName());

    final Map<String, TestRunDto> runDtosByName = new HashMap<>();

    // Each driver gets mapped to a different run.
    for (final String runName : resultsToLogByRunNameAndCaseId.rowKeySet()) {
      // Verify Runs
      final TestRunDto runDto =
          this.verifyOrCreateRun(validateRequest, resultsToLogByRunNameAndCaseId, planDto, runName);
      runDtosByName.put(runName, runDto);

      // Verify Case Ids
      this.verifyCaseIdsAreSetupForRun(
          resultsToLogByRunNameAndCaseId, planDto.getId(), runDto, runName);
    }
    runsAndInvalidCases.setRunDtosByName(runDtosByName);
    return runsAndInvalidCases;
  }

  private Set<Long> filterOutBadCaseIds(
      @NonNull final Set<Long> caseIdsForSuite,
      @NonNull final Table<String, Long, TestRailStatusComment> resultsToLogByRunNameAndCaseId) {
    final var requestedCaseIds = resultsToLogByRunNameAndCaseId.columnKeySet();

    // Get a list of all requested case ids that do not belong to the given suite
    final var invalidCaseIds = new HashSet<>(requestedCaseIds);
    invalidCaseIds.removeAll(caseIdsForSuite);

    // If they are empty, we don't need to do anything else.
    if (invalidCaseIds.isEmpty()) {
      log.trace("No invalid caseIds");
      return Collections.emptySet();
    }

    // In this case we have at least one bad caseId, we need to remove them from the results
    // map, so they don't get logged to TestRail.
    for (final var resultSet : resultsToLogByRunNameAndCaseId.rowMap().values()) {
      log.trace("Removing invalid caseIds " + StringUtils.join(invalidCaseIds, ","));
      resultSet.keySet().removeAll(invalidCaseIds);
    }

    return invalidCaseIds;
  }

  private TestRunDto verifyOrCreateRun(
      @NonNull final TestRailValidateRequest validateRequest,
      @NonNull final Table<String, Long, TestRailStatusComment> resultsToLogByRunNameAndCaseId,
      @NonNull final PlanDto planDtoMaybeIncomplete,
      @NonNull final String runName)
      throws TestRailException {
    log.trace(
        "Verifying Runs are setup for runName "
            + runName
            + " for plan "
            + planDtoMaybeIncomplete.getId());

    try {
      // If the entries aren't populated, we need to do a second lookup to populate them.
      // This may happen if we just created the run.
      final PlanDto completePlan =
          Objects.nonNull(planDtoMaybeIncomplete.getEntries())
              ? planDtoMaybeIncomplete
              : this.testRailClient.getTestPlan(planDtoMaybeIncomplete.getId());

      final var resultsToLog = resultsToLogByRunNameAndCaseId.row(runName);

      // Try to see if a run already exists with the same name. Otherwise, create one
      final var existingRun =
          completePlan.getEntries().stream()
              .flatMap(e -> e.getRuns().stream())
              .filter(r -> !r.getIsCompleted())
              .filter(r -> runName.equals(r.getName()))
              .findFirst();
      if (existingRun.isPresent()) {
        return existingRun.get();
      }
      return this.testRailClient
          .createNewPlanEntry(
              runName,
              validateRequest.suiteId(),
              completePlan.getId(),
              validateRequest.includeAll(),
              resultsToLog.keySet())
          .getRuns()
          .get(0);
    } catch (TestRailException e) {
      log.error(
          "Caught Error for Project/Plan [{}/{}]",
          validateRequest.projectId(),
          validateRequest.planId(),
          e);
      throw e;
    }
  }

  /**
   * Get or Create TestRail TestPlan from TestRail provider
   *
   * @param projectId projectId containing plan
   * @param planId The planId if available
   * @param planName The planName to search or create
   * @return The TestRail PlanDto
   * @throws TestRailException If plan creation fails
   */
  public PlanDto verifyOrCreatePlan(
      @NonNull final Long projectId, @Nullable final Long planId, @NonNull final String planName)
      throws TestRailException {
    log.trace("VerifyingOrCreate Plan is setup for plan " + planId);
    // If the plan id is non-null, then the plan has already been set up by a previous iteration.
    // Just fetch the plan, so we can look at the runs later.
    if (Objects.nonNull(planId)) {
      return this.testRailClient.getTestPlan(planId);
    }

    final var cleanTestPlanName = TestRailUtil.cleanTestRailPlanName(planName);

    // First, attempt to find a pre-created plan by name. Otherwise, create a new one
    var existingPlan = this.testRailClient.findExistingTestPlan(projectId, cleanTestPlanName);
    if (existingPlan.isPresent()) {
      return existingPlan.get();
    }
    return this.testRailClient.createTestPlan(cleanTestPlanName, projectId);
  }

  private void verifyCaseIdsAreSetupForRun(
      @NonNull final Table<String, Long, TestRailStatusComment> resultsToLogByRunNameAndCaseId,
      @NonNull final Long planId,
      @NonNull final TestRunDto testRunDto,
      @NonNull final String runName)
      throws TestRailException {
    log.trace("Verifying caseIds are setup for run " + runName);

    // Fetch all the TestResults for the given run
    final var testResults = this.testRailClient.getTestResultsForRun(testRunDto.getId(), null);
    final var resultsToLog = resultsToLogByRunNameAndCaseId.row(runName);

    // Get a list of all existing case ids in the results
    Set<Long> existingCaseIds =
        testResults.stream().map(TestDto::getCaseId).collect(Collectors.toSet());

    // Compare this to the results we are trying to log. In some cases, we have already added
    // a case id to the plan (Ex. retries)
    Set<Long> newCaseIds = new HashSet<>(resultsToLog.keySet());
    newCaseIds.removeAll(existingCaseIds);

    // if test case isn't part of run (and so isn't part of plan entry either), add it to plan entry
    if (!newCaseIds.isEmpty()) {
      log.debug(
          "couldn't find matching test case IDs "
              + StringUtils.join(newCaseIds, ", ")
              + " in run "
              + testRunDto.getId()
              + ". Adding test cases to run");

      // Add back the existing caseIds, so we don't remove results
      newCaseIds.addAll(existingCaseIds);

      // call update endpoint with existing case ID's and new ones
      this.testRailClient.updateExistingPlanEntry(planId, testRunDto.getEntryId(), newCaseIds);
    }
  }
}
