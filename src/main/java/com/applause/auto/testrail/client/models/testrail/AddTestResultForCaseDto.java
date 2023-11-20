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

/** DTO for adding a new TestResult for a test case */
@Data
@Builder
public class AddTestResultForCaseDto {
  /** The ID of the test case */
  private final Long caseId;

  /** The ID of the test status */
  private final Integer statusId;

  /** The comment / description for the test result */
  private final String comment;

  /** The version or build you tested against */
  private final String version;

  /** The time it took to execute the test, e.g. "30s" or "1m 45s". */
  private final String elapsed;

  /** A comma-separated list of defects to link to the test result */
  private final String defects;

  /** The ID of a user the test should be assigned to */
  @SerializedName("assignedto_id")
  private final Long assignedToId;
}
