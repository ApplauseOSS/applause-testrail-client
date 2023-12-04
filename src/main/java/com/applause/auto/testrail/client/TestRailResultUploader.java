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
import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.config.TestRailConfig;
import com.applause.auto.testrail.client.models.config.TestRailConfigExtended;
import com.applause.auto.testrail.client.models.config.TestRailStatusMaps;
import com.applause.auto.testrail.client.models.internal.TestRailStatusComment;
import com.applause.auto.testrail.client.models.internal.TestRailValidateRequest;
import com.applause.auto.testrail.client.models.testrail.StatusDto;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import okhttp3.OkHttpClient;

/** Handles uploading result to TestRail */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestRailResultUploader {
  @NonNull private final TestRailConfigExtended testRailConfigExtended;
  @NonNull private final ProjectConfiguration projectConfiguration;
  @NonNull private final TestRailResultLogger testRailResultLogger;
  private final long testRailPlanId;
  @NonNull private final TestRailParamValidator paramValidator;

  /**
   * @param testRailConfig mostly credentials
   * @param projectConfiguration project specific configuration
   * @param proxyConfig MAKE SURE TO PASS SDK PROXY CONFIG. YOU CAN GET THIS WITH
   *     ApplauseConfigHelper.getHttpProxy(ApplauseEnvironmentConfigurationManager.get()) . I didn't
   *     just call it in here because this submodule can't depend on SDK code (it's used in auto-api
   *     too)
   * @return The TestRailResultUploader
   * @throws TestRailException if initialization fails
   */
  public static TestRailResultUploader initialize(
      @NonNull final TestRailConfig testRailConfig,
      @NonNull final ProjectConfiguration projectConfiguration,
      @Nullable final Proxy proxyConfig)
      throws TestRailException {
    var httpClient =
        new OkHttpClient.Builder()
            .proxy(proxyConfig != null ? proxyConfig : Proxy.NO_PROXY)
            .build();

    // testrail statuses are ints in their API, but strings in the SDK config file. We need to map
    // them over
    final var client = new TestRailClientFactory(httpClient).getTestRailClient(testRailConfig);
    var statusesFromTestRail = client.getCustomStatuses();
    var testRailConfigExtended =
        testRailConfig.toExtended(
            getTestRailStatusMaps(projectConfiguration, statusesFromTestRail));
    var paramValidator = new TestRailParamValidator(client);

    // then call validateTestrailConfiguration to validate everything
    paramValidator.validateTestrailConfiguration(
        projectConfiguration.testRailProjectId(),
        projectConfiguration.testRailSuiteId(),
        projectConfiguration.testRailPlanName(),
        projectConfiguration.testRailRunName(),
        null);

    var resultLogger = new TestRailResultLogger(client);
    // grab planId on init, so we don't do it repeatedly during result upload
    // yeah this is pretty awkward API but whatever. We call this with null planId to force lookup,
    // then grab ID for later so we don't need to lookup plan ID for every result we upload
    var testRailPlanId =
        resultLogger
            .verifyOrCreatePlan(
                projectConfiguration.testRailProjectId(),
                null,
                projectConfiguration.testRailPlanName())
            .getId();
    // make instance of this class
    return new TestRailResultUploader(
        testRailConfigExtended, projectConfiguration, resultLogger, testRailPlanId, paramValidator);
  }

  private static TestRailStatusMaps getTestRailStatusMaps(
      ProjectConfiguration projectConfiguration, List<StatusDto> statusesFromTestRail) {
    var testRailStatusMap =
        statusesFromTestRail.stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    status -> status.getName().toLowerCase().trim(),
                    Function.identity(),
                    (first, second) -> {
                      throw new RuntimeException(
                          "TestRail has multiple result statuses configured with system name ["
                              + first.getName()
                              + "] and ["
                              + second.getName()
                              + "] Duplicates detected have status ID's "
                              + first.getId()
                              + " and "
                              + second.getId());
                    }));
    // try to map the status strings in config bean to ints in testrail
    return new TestRailStatusMaps(
        Optional.ofNullable(
                testRailStatusMap.get(projectConfiguration.statusPassed().toLowerCase().trim()))
            .map(StatusDto::getId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Mapping for TestRail Passed Status "
                            + projectConfiguration.statusPassed().toLowerCase().trim()
                            + " not found")),
        Optional.ofNullable(
                testRailStatusMap.get(projectConfiguration.statusFailed().toLowerCase().trim()))
            .map(StatusDto::getId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Mapping for TestRail Failed Status "
                            + projectConfiguration.statusFailed().toLowerCase().trim()
                            + " not found")),
        Optional.ofNullable(
                testRailStatusMap.get(projectConfiguration.statusSkipped().toLowerCase().trim()))
            .map(StatusDto::getId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Mapping for TestRail Skipped Status "
                            + projectConfiguration.statusSkipped().toLowerCase().trim()
                            + " not found")),
        Optional.ofNullable(
                testRailStatusMap.get(projectConfiguration.statusError().toLowerCase().trim()))
            .map(StatusDto::getId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Mapping for TestRail Error Status "
                            + projectConfiguration.statusError().toLowerCase().trim()
                            + " not found")),
        Optional.ofNullable(
                testRailStatusMap.get(projectConfiguration.statusCanceled().toLowerCase().trim()))
            .map(StatusDto::getId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Mapping for TestRail Cancelled Status "
                            + projectConfiguration.statusCanceled().toLowerCase().trim()
                            + " not found")));
  }

  /**
   * Uploads a set of test results to TestRail
   *
   * @param resultsToUpload A set of results to upload
   * @throws TestRailException when upload of batch failed
   * @return set of invalid test case ID's
   */
  public ImmutableSet<Long> uploadResults(@NonNull final Set<UploadResultDto> resultsToUpload)
      throws TestRailException {

    // validate case ID's first
    paramValidator.validateTestCaseIds(
        projectConfiguration.testRailProjectId(),
        projectConfiguration.testRailSuiteId(),
        resultsToUpload.stream()
            .filter(Objects::nonNull)
            .map(UploadResultDto::testCaseId)
            .collect(ImmutableSet.toImmutableSet()));

    // upload results
    final Table<String, Long, TestRailStatusComment> resultsToLogByRunNameAndCaseId =
        HashBasedTable.create();
    resultsToUpload.forEach(
        result ->
            resultsToLogByRunNameAndCaseId.put(
                projectConfiguration.testRailRunName(),
                TestRailUtil.extractTestCaseId(result.testCaseId()),
                new TestRailStatusComment(
                    testRailConfigExtended.getStatusMaps().getStatusMap().get(result.status()),
                    result.resultComment())));

    var validateRequest =
        new TestRailValidateRequest(
            projectConfiguration.testRailProjectId(),
            projectConfiguration.testRailSuiteId(),
            projectConfiguration.testRailPlanName(),
            testRailPlanId,
            projectConfiguration.addAllTestsToPlan());
    return ImmutableSet.copyOf(
        testRailResultLogger.execute(resultsToLogByRunNameAndCaseId, validateRequest));
  }

  /**
   * DTO Used for uploading result to testrail
   *
   * @param testCaseId The test case id
   * @param status The result status
   * @param resultComment The comment
   */
  public record UploadResultDto(
      @EqualsAndHashCode.Include @NonNull String testCaseId,
      @NonNull TestResultStatus status,
      @NonNull String resultComment) {}

  /**
   * The full TestRail project configuration
   *
   * @param testRailProjectId The project id
   * @param testRailSuiteId The suite id
   * @param addAllTestsToPlan If true, adds all test cases to the generated test plan
   * @param testRailPlanName The name of the plan to report to
   * @param testRailRunName The name of the run to report to
   * @param statusPassed The mapped status for passed results
   * @param statusFailed The mapped status for failed results
   * @param statusSkipped The mapped status for skipped results
   * @param statusError The mapped status for error results
   * @param statusCanceled The mapped status for canceled results
   */
  public record ProjectConfiguration(
      long testRailProjectId,
      long testRailSuiteId,
      boolean addAllTestsToPlan,
      @NonNull String testRailPlanName,
      @NonNull String testRailRunName,
      @NonNull String statusPassed,
      @NonNull String statusFailed,
      @NonNull String statusSkipped,
      @NonNull String statusError,
      @NonNull String statusCanceled) {}
}
