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
package com.applause.auto.testrail.client.enums;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;

/**
 * An internal status to be mapped to the TestRail status. TestRail has a number of internal or
 * custom statuses that can change under the hood. These statuses provide a simple list of statuses
 * that we can map later
 */
@Getter
public enum TestResultStatus {
  /** The test has not been run yet */
  NOT_RUN("NOT_RUN"),
  /** The test is currently in progress */
  IN_PROGRESS("IN_PROGRESS"),
  /** The test has passed */
  PASSED("PASSED"),
  /** The test has failed */
  FAILED("FAILED"),
  /** The test has been skipped */
  SKIPPED("SKIPPED"),
  /** The test has been canceled */
  CANCELED("CANCELED"),
  /** The test has reached an error condition */
  ERROR("ERROR");

  private final String value;

  TestResultStatus(final String value) {
    this.value = value;
  }

  /** A set of statuses considered "complete" */
  public static final ImmutableSet<TestResultStatus> ALL_COMPLETE_STATUSES =
      Sets.immutableEnumSet(PASSED, FAILED, SKIPPED, CANCELED, ERROR);
}
