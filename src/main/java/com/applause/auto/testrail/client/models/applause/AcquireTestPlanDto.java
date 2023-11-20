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
package com.applause.auto.testrail.client.models.applause;

import com.applause.auto.testrail.client.models.config.TestRailDto;

/**
 * DTO that can be passed to the Applause Services to acquire a TestPlan
 *
 * @param projectId The ID of the TestRail Project
 * @param planId The ID of the plan
 * @param testPlanName The Name of the plan
 * @param config The TesRail config
 */
public record AcquireTestPlanDto(
    Long projectId, Long planId, String testPlanName, TestRailDto config) {}
