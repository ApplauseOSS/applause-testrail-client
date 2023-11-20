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

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/** DTO for adding a new plan */
@Data
@Builder
public class AddPlanDto {

  /** The name of the test plan (required) */
  @NonNull private final String name;

  /** The description of the test plan */
  private final String description;

  /** The ID of the milestone to link to the test plan */
  private final Long milestoneId;

  /** An array of objects describing the test runs of the plan */
  private final List<PlanEntryRunDto> entries;
}
