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
package com.applause.auto.testrail.client.models.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/** The TestRail config class */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TestRailConfig extends TestRailCredentials {
  @NonNull private final String url;

  /**
   * Converts this config to an extended config
   *
   * @param statusMaps The status mapping
   * @return The extended config
   */
  public TestRailConfigExtended toExtended(@NonNull final TestRailStatusMaps statusMaps) {
    return TestRailConfigExtended.builder()
        .email(this.getEmail())
        .apiKey(this.getApiKey())
        .url(this.getUrl())
        .statusMaps(statusMaps)
        .build();
  }
}
