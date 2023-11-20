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

import com.applause.auto.testrail.client.models.testrail.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import retrofit2.Response;
import retrofit2.http.*;

/** Retrofit client for interacting with the TestRail API */
public interface TestRailApi {

  /**
   * Updates an existing test plan (partial updates are supported, i.e. you can submit and update
   * specific fields only). If you want to update only certain fields, leave others null
   *
   * @param planId The ID of the test plan
   * @param entryId The ID of the test plan entry (note: not the test run ID)
   * @param updatePlanEntryDto info about the plan entry to update
   * @return If successful, this method returns the updated test plan entry including test runs
   *     using the same response format as the entries field of get_plan, but for a single entry
   *     instead of a list of entries.
   */
  @POST("/update_plan_entry/{plan_id}/{entry_id}")
  CompletableFuture<Response<PlanEntryDto>> updatePlanEntry(
      @Path("plan_id") long planId,
      @Path("entry_id") final String entryId,
      @Body final UpdatePlanEntryDto updatePlanEntryDto);

  /**
   * Adds one or more new test runs to a test plan.
   *
   * @param planId The ID of the plan the test runs should be added to
   * @param addPlanEntryDto info about the plan entry to add
   * @return If successful, this method returns the updated test plan entry including test runs
   *     using the same response format as the entries field of get_plan, but for a single entry
   *     instead of a list of entries.
   */
  @POST("/add_plan_entry/{plan_id}")
  CompletableFuture<Response<PlanEntryDto>> addPlanEntry(
      @Path("plan_id") long planId, final @Body AddPlanEntryDto addPlanEntryDto);

  /**
   * Returns an existing test plan.
   *
   * @param planId The ID of the test plan
   * @return Test plan. The entries field includes an array of test plan entries. A test plan entry
   *     is a group of test runs that belong to the same test suite (just like in the user
   *     interface). Each group can have a variable amount of test runs and also supports
   *     configurations. Please also see add_plan and add_plan_entry.
   */
  @GET("/get_plan/{plan_id}")
  CompletableFuture<Response<PlanDto>> getPlan(@Path("plan_id") long planId);

  /**
   * Returns all existing test plans in a project.
   *
   * @param projectId The ID of the project
   * @param offset number of items to offset the pagination by ie : an offset of 250 will skip the
   *     first 250 items
   * @param limit max number of items to return
   * @return A list of test plans for the project. This can be used to find an existing plan id that
   *     was created manually.
   */
  @GET("/get_plans/{project_id}")
  CompletableFuture<Response<PaginatedBulkPlanDto>> getPlansForProject(
      @Path("project_id") long projectId, @Query("offset") int offset, @Query("limit") int limit);

  /**
   * Returns an existing test plan.
   *
   * @param projectId The ID of the project the test plan should be added to
   * @param addPlanDto data fields to set in new plan
   * @return Test plan. The entries field includes an array of test plan entries. A test plan entry
   *     is a group of test runs that belong to the same test suite (just like in the user
   *     interface). Each group can have a variable amount of test runs and also supports
   *     configurations. Please also see add_plan and add_plan_entry.
   */
  @POST("/add_plan/{project_id}")
  CompletableFuture<Response<PlanDto>> addPlan(
      @Path("project_id") long projectId, final @Body AddPlanDto addPlanDto);

  /**
   * Adds a new test result, comment or assigns a test. It's recommended to use add_results instead
   * if you plan to add results for multiple tests.
   *
   * <p>NOTE: This doesn't currently support updating custom fields since there's no good way to
   * represent them in the DTO. If you need to add them, see TestRail docs for this call
   *
   * @param testId The ID of the test the result should be added to
   * @param updateTestResultDto info about the test result to update
   * @return If successful, this method returns the updated test plan entry including test runs
   *     using the same response format as the entries field of get_plan, but for a single entry
   *     instead of a list of entries.
   */
  @POST("/add_result/{test_id}")
  CompletableFuture<Response<TestResultDto>> addResult(
      @Path("test_id") long testId, final @Body UpdateTestResultDto updateTestResultDto);

  /**
   * Adds a list new test results.
   *
   * @param testRailRunId The ID of the TestRail run the results should be added to
   * @param testResults info about the test results to add
   * @return If successful, this method returns the newly created tests for the given cases
   */
  @POST("/add_results_for_cases/{run_id}")
  CompletableFuture<Response<List<TestResultDto>>> addResultsForCases(
      @Path("run_id") long testRailRunId, final @Body AddTestResultsForCaseDto testResults);

  /**
   * Returns a list of tests for a test run. The response includes an array of tests. Each test in
   * this list follows the same format as get_test. NOTE: This doesn't currently support updating
   * custom fields since there's no good way to represent them in the DTO. * If you need to add
   * them, see TestRail docs for this call
   *
   * @param testRailRunId The ID of the test run
   * @param statusIds optional comma-separated list of Statuses to filter by. Pass null if you don't
   *     want to filter
   * @param offset number of items to offset the pagination by ie : an offset of 250 will skip the
   *     first 250 items
   * @param limit max number of items to return
   * @return If successful, The response includes an array of tests. Each test in this list follows
   *     the same format as get_test.
   */
  @GET("/get_tests/{run_id}")
  CompletableFuture<Response<PaginatedBulkTestDto>> getTests(
      @Path("run_id") long testRailRunId,
      final @Query("status_id") String statusIds,
      @Query("offset") int offset,
      @Query("limit") int limit);

  /**
   * Returns a list of test cases for a project or specific test suite (if the project has multiple
   * suites enabled).
   *
   * @param projectId The ID of the project
   * @param suiteId The id of the test suite inside the project
   * @param offset number of items to offset the pagination by ie : an offset of 250 will skip the
   *     first 250 items
   * @param limit max number of items to return
   * @return If successful, The response includes an array of tests. Each test in this list follows
   *     the same format as get_test.
   */
  @GET("/get_cases/{project_id}")
  CompletableFuture<Response<PaginatedBulkCaseDto>> getCasesForSuite(
      @Path("project_id") long projectId,
      final @Query("suite_id") long suiteId,
      @Query("offset") int offset,
      @Query("limit") int limit);

  /**
   * Returns a list of TestRail test result statuses for the calling customer. The response should
   * contain the 5 default statuses, plus any other custom statuses the customer may have set up.
   *
   * @return list of statuses and their attributes. see <a
   *     href="http://docs.gurock.com/testrail-api2/reference-statuses">TestRail API</a>
   */
  @GET("/get_statuses")
  CompletableFuture<Response<List<StatusDto>>> getStatuses();

  /**
   * Returns the TestRail project matching the given id
   *
   * @param projectId The ID of the test project to fetch
   * @return The project
   */
  @GET("/get_project/{project_id}")
  CompletableFuture<Response<ProjectDto>> getProject(@Path("project_id") long projectId);

  /**
   * Returns the TestRail suite with the given ID
   *
   * @param suiteId The ID of the test suite to fetch
   * @return The test suite
   */
  @GET("/get_suite/{suite_id}")
  CompletableFuture<Response<TestSuiteDto>> getSuite(@Path("suite_id") long suiteId);
}
