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
package com.applause.auto.testrail.client.models.config;

import com.applause.auto.testrail.client.TestRailUtil;
import com.applause.auto.testrail.client.enums.TestResultStatus;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** The Integer values to map to the TestRail StatusDto keys */
@Getter
@Slf4j
public class TestRailStatusMaps {
  private final Map<TestResultStatus, Integer> statusMap;
  private final int mappedStatusPassed;
  private final int mappedStatusFailed;
  private final int mappedStatusSkipped;
  private final int mappedStatusError;
  private final int mappedStatusCanceled;

  /**
   * Constructs a new Status map from a TestRail config DTO
   *
   * @param testRailDto The TestRail config
   */
  public TestRailStatusMaps(@NonNull TestRailDto testRailDto) {
    // make sure stored map is immutable since this is data class
    statusMap = ImmutableMap.copyOf(TestRailUtil.buildCustomStatusMapping(testRailDto));
    this.mappedStatusPassed = testRailDto.getMappedStatusPassed();
    this.mappedStatusSkipped = testRailDto.getMappedStatusSkipped();
    this.mappedStatusFailed = testRailDto.getMappedStatusFailed();
    this.mappedStatusCanceled = testRailDto.getMappedStatusCanceled();
    this.mappedStatusError = testRailDto.getMappedStatusError();
  }

  /**
   * Construct a new status mapping
   *
   * @param mappedStatusPassed The status to map passed result to
   * @param mappedStatusFailed The status to map failed result to
   * @param mappedStatusSkipped The status to map shipped result to
   * @param mappedStatusError The status to map error result to
   * @param mappedStatusCanceled The status to map canceled result to
   */
  public TestRailStatusMaps(
      int mappedStatusPassed,
      int mappedStatusFailed,
      int mappedStatusSkipped,
      int mappedStatusError,
      int mappedStatusCanceled) {
    this.mappedStatusPassed = mappedStatusPassed;
    this.mappedStatusFailed = mappedStatusFailed;
    this.mappedStatusSkipped = mappedStatusSkipped;
    this.mappedStatusError = mappedStatusError;
    this.mappedStatusCanceled = mappedStatusCanceled;
    statusMap =
        ImmutableMap.<TestResultStatus, Integer>builder()
            .put(TestResultStatus.PASSED, mappedStatusPassed)
            .put(TestResultStatus.FAILED, mappedStatusFailed)
            .put(TestResultStatus.SKIPPED, mappedStatusSkipped)
            .put(TestResultStatus.ERROR, mappedStatusError)
            .put(TestResultStatus.CANCELED, mappedStatusCanceled)
            .build();
    var duplicates = findDuplicates(statusMap.values());
    log.warn(
        "duplicate status mappings found for TestRail statuses "
            + Joiner.on(", ").join(duplicates)
            + " If this was not intended, make sure each TestRail status integer maps to a unique test result status");
  }

  private static <T> ImmutableSet<T> findDuplicates(Collection<T> collection) {
    var uniques = new HashSet<>();
    return collection.stream().filter(e -> !uniques.add(e)).collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Gets the set of testrail status ids included in this map
   *
   * @return The set of testrail status ids
   */
  public Set<Integer> getTestRailStatusIds() {
    return Stream.of(
            this.mappedStatusPassed,
            this.mappedStatusFailed,
            this.mappedStatusSkipped,
            this.mappedStatusCanceled,
            this.mappedStatusError)
        .collect(Collectors.toUnmodifiableSet());
  }
}
