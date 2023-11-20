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
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** A DTO representing a TestRail Test Run */
@Data
@Builder
public class TestRunDto {

  @SerializedName("assignedto_id")
  private final Long assignedToId;

  private final Long blockedCount;

  private final String completedOn;

  /** CSV format */
  private final String config;

  private final List<Long> configIds;

  @SerializedName("custom_status1_count")
  private final Long customStatus1Count;

  @SerializedName("custom_status2_count")
  private final Long customStatus2Count;

  @SerializedName("custom_status3_count")
  private final Long customStatus3Count;

  @SerializedName("custom_status4_count")
  private final Long customStatus4Count;

  @SerializedName("custom_status5_count")
  private final Long customStatus5Count;

  @SerializedName("custom_status6_count")
  private final Long customStatus6Count;

  @SerializedName("custom_status7_count")
  private final Long customStatus7Count;

  private final String description;

  private final String entryId;

  private final Long entryIndex;

  private final Long failedCount;

  private final Long id;

  private final Boolean includeAll;

  private final Boolean isCompleted;

  private final Long milestoneId;

  private final String name;

  private final Long passedCount;

  private final Long planId;

  private final Long projectId;

  private final Long createdOn;

  private final Long retestCount;

  private final Long suiteId;

  private final Long untestedCount;

  /** the test run page URL */
  private final String url;
}
