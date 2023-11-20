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

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;

/** An exception */
@Getter
public class TestRailException extends Exception {
  @NonNull private final TestRailErrorStatus status;
  private boolean retryable = true;

  /**
   * Creates a new TestRail exception from a TestRailErrorStatus
   *
   * @param status The TestRailErrorStatus
   */
  public TestRailException(@NonNull final TestRailErrorStatus status) {
    this(null, status, null);
  }

  /**
   * Creates a new TestRail exception from a TestRailErrorStatus
   *
   * @param status The TestRailErrorStatus
   * @param cause The cause of the exception
   */
  public TestRailException(
      @NonNull final TestRailErrorStatus status, @Nullable final Exception cause) {
    this(null, status, cause);
  }

  /**
   * Creates a new TestRail exception from a TestRailErrorStatus
   *
   * @param details Additional information about the error
   * @param status The TestRailErrorStatus
   */
  public TestRailException(@Nullable String details, @NonNull final TestRailErrorStatus status) {
    this(details, status, null);
  }

  /**
   * Creates a new TestRail exception from a TestRailErrorStatus
   *
   * @param details Additional information about the error
   * @param status The TestRailErrorStatus
   * @param cause The cause of the exception
   */
  public TestRailException(
      @Nullable final String details,
      @NonNull final TestRailErrorStatus status,
      @Nullable final Exception cause) {
    super(status.getMessage() + (details != null ? " Details: " + details : ""), cause);
    this.status = status;
  }

  /**
   * Marks whether the exception is considered retryable
   *
   * @param retryable True if the action can be retried
   * @return The TestRailException
   */
  @NonNull
  public TestRailException setRetryable(boolean retryable) {
    this.retryable = retryable;
    return this;
  }
}
