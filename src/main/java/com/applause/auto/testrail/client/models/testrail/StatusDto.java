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
package com.applause.auto.testrail.client.models.testrail;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * A DTO describing a TestRail result status
 *
 * @see <a href="https://docs.gurock.com/testrail-api2/reference-statuses">Reference docs</a>
 */
@Data
@Builder
public class StatusDto {
  // color attributes
  @SerializedName("color_bright")
  private final Long colorBright;

  @SerializedName("color_dark")
  private final Long colorDark;

  @SerializedName("color_medium")
  private final Long colorMedium;

  // other internal flags
  @SerializedName("is_final")
  private final Boolean isFinal;

  @SerializedName("is_system")
  private final Boolean isSystem;

  @SerializedName("is_untested")
  private final Boolean isUntested;

  @NonNull private final Integer id;
  // display value to be used by the UI
  @NonNull private final String label;
  // internal name
  @NonNull private final String name;
}
