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

/** A DTO representing a TestResult */
@Data
@Builder
public class TestResultDto {

  /** The ID of the assignee (user) of the test result */
  @SerializedName("assignedto_id")
  private final Long assignedToId;

  /** The comment or error message of the test result */
  private final String comment;

  /** The ID of the user who created the test result */
  private final Long createdBy;

  /** The date/time when the test result was created (as UNIX timestamp) */
  private final String createdOn;

  /** A comma-separated list of defects linked to the test result */
  private final String defects;

  /** The amount of time it took to execute the test (e.g. "1m" or "2m 30s") */
  private final String elapsed;

  /** The unique ID of the test result */
  private final Long id;

  /** The status of the test result, e.g. passed or failed, also see get_statuses */
  private final Integer statusId;

  /** The ID of the test this test result belongs to */
  private final Long testId;

  /** The (build) version the test was executed against */
  private final String version;
}
