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

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Response;

/** OkHttp Interceptor for adding common Http Headers to the request */
@AllArgsConstructor
@Slf4j
public class HeadersInterceptor implements Interceptor {
  private final String userName;
  private final String apiKey;

  /**
   * Intercepts the okhttp request and adds common headers
   *
   * @param chain The Http Chain
   * @return The response
   * @throws IOException If I/O Fails
   */
  @Override
  public @NonNull Response intercept(final Chain chain) throws IOException {
    log.trace("Adding headers to Http client");
    final var request = chain.request();
    final var newRequest =
        request
            .newBuilder()
            .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .addHeader(HttpHeaders.AUTHORIZATION, Credentials.basic(userName, apiKey))
            .build();
    return chain.proceed(newRequest);
  }
}
