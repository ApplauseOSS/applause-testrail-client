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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.applause.auto.testrail.client.errors.TestRailErrorStatus;
import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.testrail.*;
import jakarta.ws.rs.core.Response.Status;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TestRailClientTest {
  private static final TestRailApi testRailApi = mock(TestRailApi.class);
  private static final TestRailApi socketTimeoutApi = mock(TestRailApi.class);
  private static final CompletableFuture mockFuture = mock(CompletableFuture.class);
  private static final CompletableFuture socketTimeoutFuture = mock(CompletableFuture.class);
  private static final Response mockResponse = mock(Response.class);

  private static final Set<Long> setOfZero = Set.of(0L);

  private static TestRailClient client;

  @BeforeAll
  static void beforeAll() {
    OkHttpClient httpClient = mock(OkHttpClient.class);
    var clientBuilder = mock(OkHttpClient.Builder.class);
    when(clientBuilder.addInterceptor(any())).thenReturn(clientBuilder);
    when(clientBuilder.build()).thenReturn(httpClient);
    when(httpClient.newBuilder()).thenReturn(clientBuilder);
  }

  @BeforeEach
  public void setup() {
    reset(testRailApi, socketTimeoutApi, mockResponse, mockFuture, socketTimeoutFuture);
    client = spy(new TestRailClient(testRailApi));

    when(mockFuture.join()).thenReturn(mockResponse);
    when(socketTimeoutFuture.join())
        .thenThrow(new CompletionException(new SocketTimeoutException()));
    when(testRailApi.addPlan(anyLong(), any())).thenReturn(mockFuture);
    when(testRailApi.addPlanEntry(anyLong(), any())).thenReturn(mockFuture);
    when(testRailApi.addResult(anyLong(), any())).thenReturn(mockFuture);
    when(testRailApi.getPlan(anyLong())).thenReturn(mockFuture);
    when(testRailApi.getProject(anyLong())).thenReturn(mockFuture);
    when(testRailApi.getStatuses()).thenReturn(mockFuture);
    when(testRailApi.getSuite(anyLong())).thenReturn(mockFuture);
    when(testRailApi.getTests(anyLong(), any(), anyInt(), anyInt())).thenReturn(mockFuture);
    when(testRailApi.updatePlanEntry(anyLong(), anyString(), any())).thenReturn(mockFuture);
    when(socketTimeoutApi.addPlan(anyLong(), any())).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.addPlanEntry(anyLong(), any())).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.addResult(anyLong(), any())).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.getPlan(anyLong())).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.getProject(anyLong())).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.getStatuses()).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.getSuite(anyLong())).thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.getTests(anyLong(), any(), anyInt(), anyInt()))
        .thenReturn(socketTimeoutFuture);
    when(socketTimeoutApi.updatePlanEntry(anyLong(), anyString(), any()))
        .thenReturn(socketTimeoutFuture);
  }

  public static void setResponseCode(Status status) {
    when(mockResponse.code()).thenReturn(status.getStatusCode());
    when(mockResponse.isSuccessful()).thenReturn(status == Status.OK);
  }

  public static void setResponseBody(Object body) {
    when(mockResponse.body()).thenReturn(body);
  }

  public static void setApiClient(final TestRailApi testRailApi) {
    client = spy(new TestRailClient(testRailApi));
  }

  @SneakyThrows
  @Test
  public void testCreatePlanEntrySuccess() {
    when(mockResponse.code()).thenReturn(Status.OK.getStatusCode());
    when(mockResponse.isSuccessful()).thenReturn(true);
    setResponseCode(Status.OK);
    final var result = PlanEntryDto.builder().build();
    setResponseBody(result);
    assertEquals(client.createNewPlanEntry("run name", 0L, 0L, true, setOfZero), result);
  }

  @Test
  public void testCreatePlanEntryFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.createNewPlanEntry("run name", 0L, 0L, true, setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.createNewPlanEntry("run name", 0L, 0L, true, setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.createNewPlanEntry("run name", 0L, 0L, true, setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.createNewPlanEntry("run name", 0L, 0L, true, setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.createNewPlanEntry("run name", 0L, 0L, true, setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testUpdateExistingPlanEntrySuccess() {
    setResponseCode(Status.OK);
    final var result = PlanEntryDto.builder().build();
    setResponseBody(result);
    assertEquals(client.updateExistingPlanEntry(0L, "plan entry name", setOfZero), result);
  }

  @SneakyThrows
  @Test
  public void testUpdateExistingPlanEntryFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.updateExistingPlanEntry(0L, "plan entry name", setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.updateExistingPlanEntry(0L, "plan entry name", setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.updateExistingPlanEntry(0L, "plan entry name", setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.updateExistingPlanEntry(0L, "plan entry name", setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.updateExistingPlanEntry(0L, "plan entry name", setOfZero);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testCreateTestPlanSuccess() {
    setResponseCode(Status.OK);
    final var result = PlanDto.builder().build();
    setResponseBody(result);
    assertEquals(client.createTestPlan("Test Plan", 0L), result);
  }

  @SneakyThrows
  @Test
  public void testCreateTestPlanFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.createTestPlan("Test Plan", 0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.createTestPlan("Test Plan", 0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.createTestPlan("Test Plan", 0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.createTestPlan("Test Plan", 0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.createTestPlan("Test Plan", 0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testGetTestPlanSuccess() {
    setResponseCode(Status.OK);
    final var result = PlanDto.builder().build();
    setResponseBody(result);
    assertEquals(client.getTestPlan(0L), result);
  }

  @SneakyThrows
  @Test
  public void testGetTestPlanFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.getTestPlan(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.getTestPlan(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.getTestPlan(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.getTestPlan(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.getTestPlan(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testGetProjectSuccess() {
    setResponseCode(Status.OK);
    final var result = ProjectDto.builder().build();
    setResponseBody(result);
    assertEquals(client.getProject(0L), result);
  }

  @SneakyThrows
  @Test
  public void testGetProjectFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.getProject(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.getProject(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.getProject(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.getProject(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.getProject(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testGetTestSuiteSuccess() {
    setResponseCode(Status.OK);
    final var result = TestSuiteDto.builder().build();
    setResponseBody(result);
    assertEquals(client.getTestSuite(0L), result);
  }

  @SneakyThrows
  @Test
  public void testGetTestSuiteFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.getTestSuite(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.getTestSuite(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.getTestSuite(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.getTestSuite(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.getTestSuite(0L);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testGetTestResultsForRunSuccess() {
    setResponseCode(Status.OK);
    final var result =
        new PaginatedBulkTestDto(
            0, 0, 0, null, Collections.singletonList(TestDto.builder().build()));
    setResponseBody(result);
    assertEquals(client.getTestResultsForRun(0L, null), result.tests());
  }

  @SneakyThrows
  @Test
  public void testGetTestResultsForRunFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.getTestResultsForRun(0L, null);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.getTestResultsForRun(0L, null);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.getTestResultsForRun(0L, null);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
    setResponseCode(Status.BAD_REQUEST);
    try {
      client.getTestResultsForRun(0L, null);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.BAD_REQUEST);
    }
    setResponseCode(Status.FORBIDDEN);
    try {
      client.getTestResultsForRun(0L, null);
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.ACCESS_DENIED);
    }
  }

  @SneakyThrows
  @Test
  public void testRetrieveStatusesFromTestRailsSuccess() {
    setResponseCode(Status.OK);
    final var result =
        Arrays.asList(
            StatusDto.builder().id(0).label("label 0").name("name 0").build(),
            StatusDto.builder().id(1).label("label 1").name("name 1").build(),
            StatusDto.builder().id(2).label("label 2").name("name 2").build(),
            StatusDto.builder().id(3).label("label 3").name("name 3").build());
    setResponseBody(result);
    assertEquals(client.getCustomStatuses(), result);
  }

  @SneakyThrows
  @Test
  public void testRetrieveStatusesFromTestRailFailures() {
    setResponseCode(Status.CONFLICT);
    try {
      client.getCustomStatuses();
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.MAINTENANCE);
    }
    setResponseCode(Status.UNAUTHORIZED);
    try {
      client.getCustomStatuses();
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.AUTHENTICATION_FAILED);
    }
    setResponseCode(Status.TOO_MANY_REQUESTS);
    try {
      client.getCustomStatuses();
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.HIT_RATE_LIMIT);
    }
  }

  @Test
  public void testSocketTimeoutException() {
    setApiClient(socketTimeoutApi);
    try {
      client.getCustomStatuses();
    } catch (TestRailException e) {
      assertEquals(e.getStatus(), TestRailErrorStatus.SOCKET_TIMEOUT);
    }
  }
}
