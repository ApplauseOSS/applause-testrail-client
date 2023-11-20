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

/** A DTO representing a test case */
@Data
@Builder
public class TestDto {

  /** The ID of the user the test is assigned to */
  @SerializedName("assignedto_id")
  private final Long assignedToId;

  /** The ID of the related test case */
  private final Long caseId;

  /** The estimate of the related test case, e.g. "30s" or "1m 45s" */
  private final String estimate;

  /** The estimate forecast of the related test case, e.g. "30s" or "1m 45s" */
  private final String estimateForecast;

  /** The unique ID of the test */
  private final Long id;

  /** The ID of the milestone that is linked to the test case */
  private final Long milestoneId;

  /** The ID of the priority that is linked to the test case */
  private final Long priorityId;

  /** A comma-separated list of references/requirements that are linked to the test case */
  private final String refs;

  /** The ID of the test run the test belongs to */
  private final Long runId;

  /** The ID of the current status of the test, also see get_statuses */
  private final Integer statusId;

  /** The title of the related test case */
  private final String title;

  /** The ID of the test case type that is linked to the test case */
  private final Long typeId;

  /**
   * Custom fields of test cases are included in the response and use their system name prefixed
   * with 'custom'
   */
  private final String customExpected;

  /**
   * Custom fields of test cases are included in the response and use their system name prefixed
   * with 'custom'
   */
  private final String customPreconds;

  /**
   * Custom fields of test cases are included in the response and use their system name prefixed
   * with 'custom'
   */
  private final List<CustomStepDto> customStepsSeparated;
}
