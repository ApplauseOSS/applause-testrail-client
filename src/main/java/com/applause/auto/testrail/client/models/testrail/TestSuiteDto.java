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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** A DTO Representing a TestRail Test Suite */
@Data
@Builder
@AllArgsConstructor
public class TestSuiteDto {
  private int id;

  private String description;

  @SerializedName("completed_on")
  private int completedOn;

  @SerializedName("is_baseline")
  private boolean isBaseline;

  @SerializedName("is_completed")
  private boolean isCompleted;

  @SerializedName("is_master")
  private boolean isMaster;

  private String name;

  @SerializedName("project_id")
  private int projectId;

  private String url;
}
