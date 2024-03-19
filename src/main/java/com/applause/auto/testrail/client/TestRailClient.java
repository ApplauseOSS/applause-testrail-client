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
import com.applause.auto.testrail.client.models.internal.TestRailStatusComment;
import com.applause.auto.testrail.client.models.testrail.*;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

/** The TestRail client that wraps the TestRail API with exception handling */
@AllArgsConstructor
@Slf4j
public class TestRailClient {
  private static final int TESTRAIL_PAGE_LIMIT = 250;

  private final TestRailApi apiClient;

  /**
   * Fetch a test plan
   *
   * @param planId The id of the plan to fetch from TestRail
   * @return The plan object
   * @throws TestRailException if there is an error response from TestRail
   */
  public PlanDto getTestPlan(final long planId) throws TestRailException {
    log.debug("Request getPlan from TestRail for planId [ " + planId + " ]");
    var result = this.makeCall(client -> client.getPlan(planId));

    if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Invalid or unknown test plan: " + planId, TestRailErrorStatus.BAD_REQUEST);
    }
    if (result.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
              "No access to TestRail project for planId: " + planId,
              TestRailErrorStatus.ACCESS_DENIED)
          .setRetryable(false);
    }
    throwForCommonErrorStatuses(result, "Could not get TestRail test plan: " + planId);
    return result.body();
  }

  /**
   * Checks a TestRail response for common error statuses. These can happen even if all the
   * parameters were correct.
   */
  private static void throwForCommonErrorStatuses(
      @NonNull final Response<?> res, @NonNull final String detailMessage)
      throws TestRailException {
    if (res.isSuccessful()) {
      return;
    }
    // try to get error body
    var errorBody =
        Optional.ofNullable(res.errorBody())
            .map(
                thing -> {
                  try {
                    return thing.string();
                  } catch (IOException e) {
                    log.warn("error thrown during attempt to read TestRail error response body", e);
                    return "";
                  }
                })
            .orElse("[unavailable]");

    var errorDetailMsg = detailMessage + " Error from testrail: " + errorBody;

    switch (Status.fromStatusCode(res.code())) {
      case CONFLICT -> throw new TestRailException(errorDetailMsg, TestRailErrorStatus.MAINTENANCE);
      case UNAUTHORIZED -> throw new TestRailException(
          errorDetailMsg, TestRailErrorStatus.AUTHENTICATION_FAILED);
      case TOO_MANY_REQUESTS -> throw new TestRailException(
          errorDetailMsg, TestRailErrorStatus.HIT_RATE_LIMIT);
      case NOT_FOUND -> {
        try (var rawRes = res.raw()) {
          throw new TestRailException(
              "Invalid Route: %s Error from testrail: %s"
                  .formatted(rawRes.request().url().url().getPath(), errorBody),
              TestRailErrorStatus.INVALID_ROUTE);
        }
      }
      default -> throw new TestRailException(errorDetailMsg, TestRailErrorStatus.UNKNOWN_ERROR);
    }
  }

  /**
   * Retrieve the statuses for the given TestRail account
   *
   * @return A list of the statuses for the given TestRail account
   * @throws TestRailException if there is an error response from TestRail
   */
  public List<StatusDto> getCustomStatuses() throws TestRailException {
    log.debug("Requesting getStatuses from TestRail");
    var result = this.makeCall(client -> client.getStatuses());
    debugLogPostResponse(result.code(), "getStatuses");

    throwForCommonErrorStatuses(result, "Could not get TestRail statuses");
    return result.body();
  }

  /**
   * Fetch a given project
   *
   * @param projectId The id of the project to fetch from TestRail
   * @return The project object
   * @throws TestRailException if there is an error response from TestRail
   */
  public ProjectDto getProject(final long projectId) throws TestRailException {
    log.debug("Requesting getProject from TestRail for projectId [ " + projectId + " ]");
    var result = this.makeCall(client -> client.getProject(projectId));
    debugLogPostResponse(result.code(), "getProject");

    if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Invalid or unknown project: " + projectId, TestRailErrorStatus.BAD_REQUEST);
    }
    if (result.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
              "No access to TestRail project: " + projectId, TestRailErrorStatus.ACCESS_DENIED)
          .setRetryable(false);
    }
    throwForCommonErrorStatuses(result, "Could not get TestRail project: " + projectId);

    return result.body();
  }

  /**
   * Get a given test suite
   *
   * @param suiteId The suiteId to fetch
   * @return The test suite
   * @throws TestRailException if there is an error response from TestRail
   */
  public TestSuiteDto getTestSuite(final long suiteId) throws TestRailException {
    log.debug("Requesting getSuite from TestRail for suiteId [ " + suiteId + " ]");
    var result = this.makeCall(client -> client.getSuite(suiteId));
    debugLogPostResponse(result.code(), "getSuite");

    if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Invalid or unknown test suite: " + suiteId, TestRailErrorStatus.BAD_REQUEST);
    }
    if (result.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
              "No access to TestRail project for suiteId: " + suiteId,
              TestRailErrorStatus.ACCESS_DENIED)
          .setRetryable(false);
    }
    throwForCommonErrorStatuses(result, "Could not get TestRail test suite: " + suiteId);

    return result.body();
  }

  /**
   * Creates a test plan in TestRail
   *
   * @param testPlanName TestRail plan name
   * @param testRailProjectId TestRail project id to create plan for
   * @return the created TestRail plan
   * @throws TestRailException if there is an error response from TestRail
   */
  public PlanDto createTestPlan(@NonNull final String testPlanName, final long testRailProjectId)
      throws TestRailException {
    var dto = AddPlanDto.builder().name(testPlanName).build();

    log.debug(
        "Request addPlan from TestRail for projectId [ "
            + testRailProjectId
            + " ] planName [ "
            + testPlanName
            + " ]");
    var result = this.makeCall(client -> client.addPlan(testRailProjectId, dto));
    debugLogPostResponse(result.code(), "addPlan");

    if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Invalid or unknown project: " + testRailProjectId, TestRailErrorStatus.BAD_REQUEST);
    }
    if (result.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
              "No permission to add test plans or no access to TestRail project.",
              TestRailErrorStatus.ACCESS_DENIED)
          .setRetryable(false);
    }
    throwForCommonErrorStatuses(
        result, "Could not add TestRail plan for project id: " + testRailProjectId);
    return result.body();
  }

  /**
   * Adds a result for a given testId
   *
   * @param testRailRunId The id of the test run to add the results for
   * @param results A map of caseId to status and comment
   * @return the result for the test
   * @throws TestRailException if there is an error response from TestRail
   */
  public List<TestResultDto> addResults(
      final long testRailRunId, @NonNull final Map<Long, TestRailStatusComment> results)
      throws TestRailException {
    final AddTestResultsForCaseDto testResults = new AddTestResultsForCaseDto();
    for (Map.Entry<Long, TestRailStatusComment> result : results.entrySet()) {
      testResults.add(
          AddTestResultForCaseDto.builder()
              .caseId(result.getKey())
              .statusId(result.getValue().statusId())
              .comment(result.getValue().comment())
              .build());
    }

    log.debug(
        "Sending addResultsForCases request to TestRail for run [{}] with test results count [{}]",
        testRailRunId,
        results.size());
    var res = this.makeCall(client -> client.addResultsForCases(testRailRunId, testResults));
    debugLogPostResponse(res.code(), "addResultsForCases");

    if (res.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Invalid or unknown run id: " + testRailRunId, TestRailErrorStatus.BAD_REQUEST);
    }
    if (res.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
          "No permission to add test result or no access to TestRail project.",
          TestRailErrorStatus.ACCESS_DENIED);
    }
    throwForCommonErrorStatuses(res, "Could not add TestRail result for run id: " + testRailRunId);
    return res.body();
  }

  /**
   * Adds a result for a given testId
   *
   * @param projectId The id of the project in TestRail
   * @param suiteId The id of the suite in TestRail
   * @return the result for the test
   * @throws TestRailException if there is an error response from TestRail
   */
  public List<TestCaseDto> getTestCasesForSuite(final long projectId, final long suiteId)
      throws TestRailException {
    log.debug(
        "Requesting getCasesForSuite from TestRail for project [ "
            + projectId
            + " ] suiteId [ "
            + suiteId
            + " ]");
    int offset = 0;
    boolean nextPage = true;
    List<TestCaseDto> cases = new ArrayList<>();
    while (nextPage) {
      final int currentOffset = offset;
      var res =
          this.makeCall(
              client ->
                  client.getCasesForSuite(projectId, suiteId, currentOffset, TESTRAIL_PAGE_LIMIT));
      nextPage = false;
      offset += TESTRAIL_PAGE_LIMIT;
      debugLogPostResponse(res.code(), "getCasesForSuite");

      if (res.code() == Status.BAD_REQUEST.getStatusCode()) {
        throw new TestRailException(
            "Invalid or unknown project [%d] or suite [%d]".formatted(projectId, suiteId),
            TestRailErrorStatus.BAD_REQUEST);
      }
      if (res.code() == Status.FORBIDDEN.getStatusCode()) {
        throw new TestRailException(
            "No permission to get this suite or no access to TestRail project",
            TestRailErrorStatus.ACCESS_DENIED);
      }

      throwForCommonErrorStatuses(res, "Could not fetch test case for suite: " + suiteId);
      var body = res.body();
      if (body != null && body.cases() != null && !body.cases().isEmpty()) {
        cases.addAll(body.cases());
      }
      if (body != null
          && body._links() != null
          && body._links().next() != null
          && body.size() > 0) {
        nextPage = true;
      }
    }
    return cases;
  }

  /**
   * Create a new plan entry in the current plan, and add a test case. This equates to a test run
   *
   * @param testRailPlanId TestRail plan name
   * @param testRunName TestRail run name
   * @param testSuiteId TestRail suite id
   * @param includeAll Whether to add all tests to the new plan entry
   * @param testCaseIds A set of ids to create the plan with
   * @return the new plan entry
   * @throws TestRailException if there is an error response from TestRail
   */
  public PlanEntryDto createNewPlanEntry(
      @NonNull final String testRunName,
      final long testSuiteId,
      final long testRailPlanId,
      final boolean includeAll,
      @NonNull final Set<Long> testCaseIds)
      throws TestRailException {

    if (testCaseIds.isEmpty()) {
      throw new TestRailException(
              "TestcaseId invalid. PlanEntry creation must contain at least 1 valid Testcase Id.",
              TestRailErrorStatus.BAD_REQUEST)
          .setRetryable(false);
    }

    final var dto =
        AddPlanEntryDto.builder()
            .suiteId(testSuiteId)
            .includeAll(includeAll)
            .caseIds(testCaseIds)
            .name(testRunName)
            .build();

    log.debug(
        "Requesting addPlanEntry from TestRail for plan [{}], suite [{}], run [{}], and test case id count [{}]",
        testRailPlanId,
        testSuiteId,
        testRunName,
        testCaseIds.size());

    var result = this.makeCall(client -> client.addPlanEntry(testRailPlanId, dto));
    debugLogPostResponse(result.code(), "addPlanEntry");

    if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Invalid or unknown test plan id: " + testRailPlanId, TestRailErrorStatus.BAD_REQUEST);
    }
    if (result.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
              "No permission to modify test plans or no access to TestRail project.",
              TestRailErrorStatus.ACCESS_DENIED)
          .setRetryable(false);
    }
    throwForCommonErrorStatuses(
        result, "Could not add TestRail plan entry for test plan id: " + testRailPlanId);
    return result.body();
  }

  /**
   * Updates an existing TestRail plan entry
   *
   * @param planId The plan id for the plan entry you want to update
   * @param planEntryId The id of the plan entry to update
   * @param caseIds A list of caseIds for all the cases
   * @return The Updated TestRail Plan Entry
   * @throws TestRailException if there is an error response from TestRail
   */
  public PlanEntryDto updateExistingPlanEntry(
      final long planId, @NonNull final String planEntryId, @Nullable final Set<Long> caseIds)
      throws TestRailException {
    log.debug(
        "Requesting updatePlanEntry from TestRail for planId [ "
            + planId
            + " ] planEntryId [ "
            + planEntryId
            + " ] caseId count [ "
            + (caseIds != null ? caseIds.size() : 0)
            + " ]");
    var result =
        this.makeCall(
            client ->
                client.updatePlanEntry(
                    planId, planEntryId, UpdatePlanEntryDto.builder().caseIds(caseIds).build()));
    debugLogPostResponse(result.code(), "updatePlanEntry");

    if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
      throw new TestRailException(
          "Bad request for test plan: " + planId + " of test plan entry: " + planEntryId,
          TestRailErrorStatus.BAD_REQUEST);
    }
    if (result.code() == Status.FORBIDDEN.getStatusCode()) {
      throw new TestRailException(
              "No permission to add test result or no access to TestRail project.",
              TestRailErrorStatus.ACCESS_DENIED)
          .setRetryable(false);
    }
    throwForCommonErrorStatuses(
        result, "Could not update TestRail plan entry " + planEntryId + "in plan " + planId);

    return result.body();
  }

  /**
   * Fetch a test plan
   *
   * @param projectId The id of the project to fetch from TestRail
   * @param planName The name of the plan to search for
   * @return The plan object
   * @throws TestRailException if there is an error response from TestRail
   */
  public Optional<PlanDto> findExistingTestPlan(
      final long projectId, @NonNull final String planName) throws TestRailException {
    log.debug(
        "Requesting getPlansForProject from TestRail for projectId [ "
            + projectId
            + " ] planName [ "
            + planName
            + " ]");

    int offset = 0;
    boolean nextPage = true;
    List<PlanDto> plans = new ArrayList<>();
    while (nextPage) {
      final var currentOffset = offset;
      var result =
          this.makeCall(
              client -> client.getPlansForProject(projectId, currentOffset, TESTRAIL_PAGE_LIMIT));
      nextPage = false;
      offset += TESTRAIL_PAGE_LIMIT;
      debugLogPostResponse(result.code(), "getPlansForProject");

      if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
        throw new TestRailException(
            "Invalid or unknown project: " + projectId, TestRailErrorStatus.BAD_REQUEST);
      }
      if (result.code() == Status.FORBIDDEN.getStatusCode()) {
        throw new TestRailException(
                "No access to TestRail project: " + projectId, TestRailErrorStatus.ACCESS_DENIED)
            .setRetryable(false);
      }
      throwForCommonErrorStatuses(result, "Could not get TestRail test project: " + projectId);

      var body = result.body();
      if (body != null && body.plans() != null && !body.plans().isEmpty()) {
        plans.addAll(body.plans());
      }
      if (body != null
          && body._links() != null
          && body._links().next() != null
          && body.size() > 0) {
        nextPage = true;
      }
    }

    return plans.parallelStream()
        .filter(plan -> !plan.getIsCompleted())
        .filter(plan -> plan.getName().equals(planName))
        .min((planA, planB) -> Math.toIntExact(planB.getCreatedOn() - planA.getCreatedOn()));
  }

  /**
   * Get the test results for a run
   *
   * @param testRailRunId The run id to fetch the results for
   * @param statusIds A CSV string of status ids to search the results for
   * @return A list of tests for the given TestRail run with the matching status ids
   * @throws TestRailException if there is an error response from TestRail
   */
  public List<TestDto> getTestResultsForRun(
      final long testRailRunId, @Nullable final String statusIds) throws TestRailException {
    log.debug(
        "Requesting getTests from TestRail for testRailRunId [ "
            + testRailRunId
            + " ] containing statusIds [ "
            + statusIds
            + " ]");

    int offset = 0;
    boolean nextPage = true;
    List<TestDto> tests = new ArrayList<>();
    while (nextPage) {
      final var currentOffset = offset;
      var result =
          this.makeCall(
              client ->
                  client.getTests(testRailRunId, statusIds, currentOffset, TESTRAIL_PAGE_LIMIT));
      nextPage = false;
      offset += TESTRAIL_PAGE_LIMIT;
      debugLogPostResponse(result.code(), "getTests");

      if (result.code() == Status.BAD_REQUEST.getStatusCode()) {
        throw new TestRailException(
            "Invalid or unknown test run id: " + testRailRunId, TestRailErrorStatus.BAD_REQUEST);
      }
      if (result.code() == Status.FORBIDDEN.getStatusCode()) {
        throw new TestRailException(
            "No access to TestRail project for testRailRunId: " + testRailRunId,
            TestRailErrorStatus.ACCESS_DENIED);
      }
      throwForCommonErrorStatuses(
          result, "Could not get TestRail test list for test run id: " + testRailRunId);

      var body = result.body();
      if (body != null && body.tests() != null && !body.tests().isEmpty()) {
        tests.addAll(body.tests());
      }
      if (body != null
          && body._links() != null
          && body._links().next() != null
          && body.size() > 0) {
        nextPage = true;
      }
    }
    return tests;
  }

  private void debugLogPostResponse(final int httpStatusCode, @NonNull final String methodName) {
    log.debug("Retrieved HTTP " + httpStatusCode + " from TestRail " + methodName + " request.");
  }

  private <T> Response<T> makeCall(
      Function<TestRailApi, CompletableFuture<Response<T>>> testRailAction)
      throws TestRailException {
    try {
      return testRailAction.apply(apiClient).join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof SocketTimeoutException) {
        throw new TestRailException(TestRailErrorStatus.SOCKET_TIMEOUT);
      } else {
        log.info("Encountered an error communicating with TestRail", e);
        throw new TestRailException(TestRailErrorStatus.UNKNOWN_ERROR);
      }
    }
  }
}
