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
 * A DTO representing a paginated list of test cases
 *
 * @param offset The page offset
 * @param limit The max number of results
 * @param size The size of results
 * @param _links The pagination links
 * @param cases The list of testcases
 */
public record PaginatedBulkCaseDto(
    int offset, int limit, int size, PaginatedLinkDto _links, List<TestCaseDto> cases) {}
