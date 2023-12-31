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
package com.applause.auto.testrail.client.models.internal;

import com.applause.auto.testrail.client.models.testrail.TestRunDto;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NonNull;

/** A map of TestRail Runs and invalid test case ids */
@Data
public class TestRailRunsAndInvalidCases {
  @NonNull private Map<String, TestRunDto> runDtosByName = new HashMap<>();
  @NonNull private Set<Long> invalidCaseIds = new HashSet<>();
}
