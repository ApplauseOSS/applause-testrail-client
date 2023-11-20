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
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/** DTO for adding a new Plan Entry */
@Data
@Builder
public class AddPlanEntryDto {

  /** The ID of the test suite for the test run(s) (required) */
  private final long suiteId;

  /** The name of the test run(s) */
  private final String name;

  /** The description of the test run(s) (requires TestRail 5.2 or later) */
  private final String description;

  /** The ID of the user the test run(s) should be assigned to */
  @SerializedName("assignedto_id")
  private final Long assignedToId;

  /**
   * True for including all test cases of the test suite and false for a custom case selection
   * (default: true)
   */
  private final Boolean includeAll;

  /** An array of case IDs for the custom case selection */
  private final Set<Long> caseIds;

  /**
   * An array of configuration IDs used for the test runs of the test plan entry (requires TestRail
   * 3.1 or later)
   */
  private final List<Long> configIds;

  private final List<PlanEntryRunDto> runs;
}
