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

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;

/** A Generic OkHttp error interceptor for the TestRail API */
@Slf4j
public class GenericErrorInterceptor implements Interceptor {
  private static final Long BYTES_TO_PEEK = 20_000L;
  private static final Set<Integer> DEFAULT_STATUS_CODES_TO_EXCLUDE = ImmutableSet.of();

  private final Set<Integer> excludedStatusCodes;

  /** Set up a new Error Interceptor using the default status codes */
  public GenericErrorInterceptor() {
    this(DEFAULT_STATUS_CODES_TO_EXCLUDE);
  }

  /**
   * Note: the excludedStatusCodes overrides any default status codes that exist
   *
   * @param excludedStatusCodes - The interceptor will not log any status codes in the set
   */
  public GenericErrorInterceptor(final @NonNull Set<Integer> excludedStatusCodes) {
    log.trace(
        "Excluding Error statuses from reporting: " + StringUtils.join(excludedStatusCodes, ","));
    this.excludedStatusCodes = excludedStatusCodes;
  }

  @NonNull
  @Override
  public okhttp3.Response intercept(final @NonNull Interceptor.Chain chain) throws IOException {
    final Request request = chain.request();
    final okhttp3.Response response = chain.proceed(request);

    if (!response.isSuccessful()) {
      log.trace("Http response not successful.");
      final int responseCode = response.code();
      if (!excludedStatusCodes.contains(responseCode)) {
        log.info(
            "Failed call with response status: '{}' to host: '{}' with path: '{}' and response body '{}'",
            responseCode,
            request.url().url().getHost(),
            request.url().url().getPath(),
            response.peekBody(BYTES_TO_PEEK).string());
      }

      return response;
    }

    return response;
  }
}
