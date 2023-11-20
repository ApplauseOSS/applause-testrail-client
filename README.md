## Testrail client library
Testrail client library for Applause Automation.

#### Installation
This library is not built or deployed on its own, it must be built with another package.

#### Linter/Formatter:
The linter/formatter will run automatically during a build, but builds do not happen automatically.  You must manually create a build or run unit tests to trigger auto-formatter.

This project uses the same formatter rules as auto-api.  

Please run `mvn test` to run unit tests and trigger the auto-formatter before committing code.

#### Unit Tests
`mvn test`

#### Example usage:
```java
// External HTTPClient needs to be passed in
OkHttpClient httpClient = new OkHttpClient();

// configuration object(s)
TestRailConfig testRailConfig =
    TestRailConfig.builder()
        .email("email")
        .apiKey("apiKey")
        .url("testrailUrl")
        .build();

// TestRailClient can create 4 types of clients

// 1a) Using TestRail API Client
final var baseApi = new TestRailClientFactory(httpClient).getTestRailApi(testRailConfig);
ProjectDto projectDto1 = baseApi.getProject(8L).join().body();
System.out.println(projectDto1);

// 2) Using TestRailClient, which wraps the raw TestRail API Client
final var testRailClient =
    new TestRailClientFactory(httpClient).getTestRailClient(testRailConfig);
ProjectDto projectDto3 = testRailClient.getProject(8L);
System.out.println(projectDto3);

// 3) Using ParamValidator, which uses the TestRailClient for basic param validation
final var testRailParamvalidator = new TestRailParamValidator(testRailClient);
ProjectDto projectDto4 = testRailParamvalidator.validateTestRailProjectId(8L);
System.out.println(projectDto4);

// 4) Using Specialized lazy/builder Reporting Client
final var testRailResultLogger = new TestRailResultLogger(testRailClient);
Table<String, Long, TestRailStatusComment> resultsToLog = HashBasedTable.create();
resultsToLog.put(
    "Run Name to Use",
    0L, // Project ID
    new TestRailStatusComment(TestRailUtil.TESTRAIL_PASSED_STATUS_ID, "Result comment"));
final var invalidCaseIds =
    testRailResultLogger.execute(
        // Project ID, Suite ID, Plan Name, Plan ID
        resultsToLog, new TestRailValidateRequest(0L, 0L, "planName", null, false));
System.out.println(invalidCaseIds);
```
