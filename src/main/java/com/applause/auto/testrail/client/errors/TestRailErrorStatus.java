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
package com.applause.auto.testrail.client.errors;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;

/** Common TestRail http failure mappings with related reasons. */
@Getter
public enum TestRailErrorStatus {
  /** No credentials are configured */
  NO_CREDENTIALS("TestRail credentials have not been configured."),
  /** The status mappings do not line up with the actual TestRail statuses */
  STATUS_MAPPING("TestRail status mapping is misconfigured."),
  /** An invalid case id was provided */
  CASE_ID("TestRail invalid case ID."),
  /** TestRail rejected the request */
  BAD_REQUEST("TestRail rejected your request for invalid input."),
  /** The TestRail rate limit was reached */
  HIT_RATE_LIMIT("TestRail rejected your request because the rate limit has been reached."),
  /** Authentication with TestRail failed */
  AUTHENTICATION_FAILED("TestRail authentication failed."),
  /** TestRail rejected access to the resource */
  ACCESS_DENIED("TestRail not allowing access to resource."),
  /** The provided resource was not found */
  RESOURCE_NOT_FOUND("TestRail resource does not exist."),
  /** An invalid http route was constructed */
  INVALID_ROUTE("The given TestRail endpoint does not exist."),
  /** TestRail is performing maintenance */
  MAINTENANCE("TestRail is performing maintenance.  Try again later."),
  /** Another error occurred */
  UNKNOWN_ERROR("An unknown TestRail error has occurred.");

  /** A set of statuses that are considered non-blocking */
  public static final Set<TestRailErrorStatus> NON_BLOCKING_STATUSES =
      Sets.immutableEnumSet(HIT_RATE_LIMIT, MAINTENANCE, UNKNOWN_ERROR);

  private final String message;

  TestRailErrorStatus(final String message) {
    this.message = message;
  }
}
