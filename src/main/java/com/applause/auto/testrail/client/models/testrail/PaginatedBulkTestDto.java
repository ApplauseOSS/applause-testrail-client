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

/**
 * A Paginated Test Case response
 *
 * @param offset The page offset
 * @param limit The page size limit
 * @param size The total number of test cases
 * @param _links A pagination link
 * @param tests The tests in this page
 */
public record PaginatedBulkTestDto(
    int offset, int limit, int size, PaginatedLinkDto _links, List<TestDto> tests) {}
