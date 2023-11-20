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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.config.TestRailStatusMaps;
import com.applause.auto.testrail.client.models.testrail.PaginatedBulkCaseDto;
import com.applause.auto.testrail.client.models.testrail.ProjectDto;
import com.applause.auto.testrail.client.models.testrail.StatusDto;
import com.applause.auto.testrail.client.models.testrail.TestSuiteDto;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import retrofit2.Response;

public class TestRailParamValidatorTest {

  @BeforeAll
  public static void beforeAll() {
    OkHttpClient httpClient = mock(OkHttpClient.class);
    // short-circuit client rebuilding to return original mock lol
    var clientBuilder = mock(OkHttpClient.Builder.class);
    when(clientBuilder.addInterceptor(any())).thenReturn(clientBuilder);
    when(clientBuilder.build()).thenReturn(httpClient);
    when(httpClient.newBuilder()).thenReturn(clientBuilder);
  }

  @Test
  public void testValidationLogicNullTestrailParams() throws TestRailException {

    final TestRailApi apiMock = getTestRailApiMocked(true, true);
    var paramValidator = makeParamValidator(apiMock);

    TestRailStatusMaps statuses = new TestRailStatusMaps(1, 2, 3, 4, 5);
    // everything okay
    paramValidator.validateTestrailConfiguration(1L, 10L, "planName", "runName", statuses);
    // null project id
    assertThrowsWithMessage(
        "Invalid testRailProjectId",
        () ->
            paramValidator.validateTestrailConfiguration(
                null, 10L, "planName", "runName", statuses));
    // negative project id
    assertThrowsWithMessage(
        "Invalid testRailProjectId",
        () ->
            paramValidator.validateTestrailConfiguration(
                -1L, 10L, "planName", "runName", statuses));
    // null suite id
    assertThrowsWithMessage(
        "Invalid testRailSuiteId",
        () ->
            paramValidator.validateTestrailConfiguration(
                1L, null, "planName", "runName", statuses));
    // negative suite id
    assertThrowsWithMessage(
        "Invalid testRailSuiteId",
        () ->
            paramValidator.validateTestrailConfiguration(
                1L, -10L, "planName", "runName", statuses));
    // null plan name
    assertThrowsWithMessage(
        "Invalid testRailPlanName",
        () -> paramValidator.validateTestrailConfiguration(1L, 10L, null, "runName", statuses));
    // null run name
    assertThrowsWithMessage(
        "Invalid testRailRunName",
        () -> paramValidator.validateTestrailConfiguration(1L, 10L, "planName", null, statuses));
  }

  @Test
  void checkTestrailErrorResponses() {
    TestRailStatusMaps statuses = new TestRailStatusMaps(1, 2, 3, 4, 5);
    assertThrowsWithMessage(
        "An unknown TestRail error has occurred. Details: Could not get TestRail project: 1",
        () ->
            makeParamValidator(getTestRailApiMocked(false, true))
                .validateTestrailConfiguration(1L, 10L, "planName", "runName", statuses));

    assertThrowsWithMessage(
        "An unknown TestRail error has occurred. Details: Could not get TestRail test suite: 10",
        () ->
            makeParamValidator(getTestRailApiMocked(true, false))
                .validateTestrailConfiguration(1L, 10L, "planName", "runName", statuses));
  }

  private TestRailParamValidator makeParamValidator(@NonNull final TestRailApi apiMock) {
    return new TestRailParamValidator(new TestRailClient(apiMock));
  }

  @NonNull
  private TestRailApi getTestRailApiMocked(
      final boolean projectResponseSuccess, final boolean suiteResponseSuccess) {
    var apiMock = mock(TestRailApi.class);
    Response<ProjectDto> projectResponseMock = Mockito.mock();
    Response<TestSuiteDto> suiteResponseMock = Mockito.mock();
    Response<StatusDto> statusResponseMock = Mockito.mock();
    Response<PaginatedBulkCaseDto> casesResponseMock = Mockito.mock();
    when(projectResponseMock.code()).thenReturn(200);
    when(suiteResponseMock.code()).thenReturn(200);
    when(statusResponseMock.code()).thenReturn(200);
    when(casesResponseMock.code()).thenReturn(200);

    doReturn(projectResponseSuccess).when(projectResponseMock).isSuccessful();
    doReturn(suiteResponseSuccess).when(suiteResponseMock).isSuccessful();
    doReturn(true).when(statusResponseMock).isSuccessful();
    doReturn(true).when(casesResponseMock).isSuccessful();

    doReturn(new TestSuiteDto(1, "hi", 0, false, false, false, "name", 1, "url"))
        .when(suiteResponseMock)
        .body();
    doReturn(mock(ProjectDto.class)).when(projectResponseMock).body();
    doReturn(Collections.emptyList()).when(statusResponseMock).body();
    doReturn(new PaginatedBulkCaseDto(0, 0, 0, null, Collections.emptyList()))
        .when(casesResponseMock)
        .body();

    when(apiMock.getProject(anyLong()))
        .thenReturn(CompletableFuture.completedFuture(projectResponseMock));
    when(apiMock.getSuite(anyLong()))
        .thenReturn(CompletableFuture.completedFuture(suiteResponseMock));
    doReturn(CompletableFuture.completedFuture(statusResponseMock)).when(apiMock).getStatuses();
    when(apiMock.getCasesForSuite(anyLong(), anyLong(), anyInt(), anyInt()))
        .thenReturn(CompletableFuture.completedFuture(casesResponseMock));
    return apiMock;
  }

  private static final class LambdaDidntThrowException extends RuntimeException {}

  private <T extends Exception> void assertThrowsWithMessage(
      @NonNull final String msgSubStr, @NonNull FailableRunnable<T> func) {
    try {
      func.run();
      // throw if running lambda didn't throw an exception lol
      throw new LambdaDidntThrowException();
    } catch (LambdaDidntThrowException e) {
      throw new RuntimeException("Lambda didn't throw an exception as expected");
    } catch (Exception ex) {
      // rethrow if not exception type we want
      if (!(ex instanceof TestRailException)) {
        throw new RuntimeException(
            "Exception class didn't match expected "
                + TestRailException.class.getName()
                + " , was: "
                + ex.getClass().getName(),
            ex);
      }
      // throw if exception message doesn't match
      if (!Optional.ofNullable(ex.getMessage())
          .map(exMsg -> exMsg.contains(msgSubStr))
          .orElse(false)) {
        throw new RuntimeException(
            "Exception message '" + ex.getMessage() + "' did not contain '" + msgSubStr + "'");
      }
    }
  }
}
