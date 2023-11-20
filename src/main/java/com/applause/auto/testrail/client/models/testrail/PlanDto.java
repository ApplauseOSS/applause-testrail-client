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

/** A DTO describing a test plan */
@Data
@Builder
public class PlanDto {

  /** The ID of the user the entire test plan is assigned to */
  @SerializedName("assignedto_id")
  private final Long assignedToId;

  /** The amount of tests in the test plan marked as blocked */
  private final Long blockedCount;

  /** The date/time when the test plan was closed (as UNIX timestamp) */
  private final String completedOn;

  /** The ID of the user who created the test plan */
  private final Long createdBy;

  /** The date/time when the test plan was created (as UNIX timestamp) */
  private final Long createdOn;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status1_count")
  private final Long customStatus1Count;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status2_count")
  private final Long customStatus2Count;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status3_count")
  private final Long customStatus3Count;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status4_count")
  private final Long customStatus4Count;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status5_count")
  private final Long customStatus5Count;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status6_count")
  private final Long customStatus6Count;

  /** The amount of tests in the test plan with the respective custom status */
  @SerializedName("custom_status7_count")
  private final Long customStatus7Count;

  /** The description of the test plan */
  private final String description;

  /** An array of 'entries', i.e. group of test runs */
  private final List<PlanEntryDto> entries;

  /** The amount of tests in the test plan marked as failed */
  private final Long failedCount;

  /** The unique ID of the test plan */
  private final Long id;

  /** True if the test plan was closed and false otherwise */
  private final Boolean isCompleted;

  /** The ID of the milestone this test plan belongs to */
  private final Long milestoneId;

  /** The name of the test plan */
  private final String name;

  /** The amount of tests in the test plan marked as passed */
  private final Long passedCount;

  /** The ID of the project this test plan belongs to */
  private final Long projectId;

  /** The amount of tests in the test plan marked as retest */
  private final Long retestCount;

  /** The amount of tests in the test plan marked as untested */
  private final Long untestedCount;

  /** The address/URL of the test plan in the user interface */
  private final String url;
}
