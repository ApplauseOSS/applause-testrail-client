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
package com.applause.auto.testrail.client.interceptors;

import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/** OKHttp Interception for updating request URLs to match the TestRail api format */
@Slf4j
public class UrlInterceptor implements Interceptor {

  /**
   * Overrides the request url to use query based routing
   *
   * @param chain The Http Chain
   * @return The response
   * @throws IOException If request fails
   */
  @Override
  public @NotNull Response intercept(final Chain chain) throws IOException {
    String basePath = "/index.php";
    String baseQueryPath = "/api/v2";
    log.trace(
        "Updating base url to expected path containing " + basePath + " and " + baseQueryPath);
    final var request = chain.request();
    final var newUrl = request.url().newBuilder();
    // in URL everything after host is "file name", this includes query params and path
    final var path = request.url().url().getPath();
    final var query = request.url().url().getQuery();
    newUrl
        .encodedPath(basePath)
        .query(baseQueryPath + path + Optional.ofNullable(query).map(q -> "&" + q).orElse(""));
    final var newRequest = request.newBuilder().url(newUrl.build()).build();
    return chain.proceed(newRequest);
  }
}
