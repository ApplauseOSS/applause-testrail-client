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

import com.applause.auto.testrail.client.interceptors.GenericErrorInterceptor;
import com.applause.auto.testrail.client.interceptors.HeadersInterceptor;
import com.applause.auto.testrail.client.interceptors.UrlInterceptor;
import com.applause.auto.testrail.client.models.config.TestRailConfig;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/** A TestRail Client Factory for initializing TestRail API Clients from a base OkHttp Client */
@AllArgsConstructor
public class TestRailClientFactory {
  private static final Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();

  private final OkHttpClient baseHttpClient;

  /**
   * Gets the base TestRail API for the provided config
   *
   * @param config The TestRail Config
   * @return The TestRail API Instance
   */
  public TestRailApi getTestRailApi(final TestRailConfig config) {
    final var httpClient =
        baseHttpClient
            .newBuilder()
            .addInterceptor(new HeadersInterceptor(config.getEmail(), config.getApiKey()))
            .addInterceptor(new UrlInterceptor())
            .addInterceptor(new GenericErrorInterceptor())
            .build();
    return new Retrofit.Builder()
        .baseUrl(config.getUrl())
        .client(httpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TestRailApi.class);
  }

  /**
   * Gets the base TestRail Client for the provided config
   *
   * @param config The TestRail Config
   * @return The TestRail Client Instance
   */
  public TestRailClient getTestRailClient(final TestRailConfig config) {
    final var apiClient = this.getTestRailApi(config);
    return new TestRailClient(apiClient);
  }

  /**
   * Gets the base TestRail Client for the provided config
   *
   * @param config The TestRail Config
   * @return The TestRail Client Instance
   */
  public TestRailParamValidator getTestRailParamValidator(final TestRailConfig config) {
    return new TestRailParamValidator(getTestRailClient(config));
  }

  /**
   * Gets the base TestRail Client for the provided config
   *
   * @param config The TestRail Config
   * @return The TestRail Client Instance
   */
  public TestRailResultLogger getTestRailResultLogger(final TestRailConfig config) {
    return new TestRailResultLogger(getTestRailClient(config));
  }
}
