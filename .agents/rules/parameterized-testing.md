# Parameterized Testing Standards

## First-Class Practice

Use parameterized tests as a primary design for a behavior contract that must hold across multiple inputs, configurations, database engines, or boundary values. Prefer one test class for one behavior contract, with each case supplied as a test parameter; do not create a separate test class per database engine when the assertions are the same.

Use JUnit Jupiter `@ParameterizedTest` with:

- `@ValueSource` or `@CsvSource` for simple scalar cases.
- `@MethodSource` for named, multi-field, connection, or domain-specific cases.
- A test-only record when a case has several fields whose meaning should remain explicit.

Give cases readable display names and assert the same externally observable behavior for every parameter. Do not replace parameterized tests with loops inside a single `@Test`; separate test invocations make failures attributable to a specific case.

## Database Contract Tests

- Pass connection configuration (engine label, URL, credentials) as test parameters; keep one test body and one set of behavioral assertions.
- Use SQL and schema types supported by every configured engine. Keep database-specific connection URLs in test arguments, not SQL branches in the test body.
- Do not use driver-specific APIs or `Class.forName` in shared contract tests. Use JDBC 4 service-provider discovery.
- Keep production JDBC drivers at their production runtime scope. Configure the Maven test plugin to include the runtime drivers needed by database parameters, using shared version properties to prevent drift.
- Add a database parameter only when the production application supports that engine and its runtime driver is available in the test classpath.

## Coverage and Determinism

- Include success, boundary, and failure cases when they represent distinct contract behavior.
- Keep test data deterministic and independent of machine-specific paths, locale, time zone, or network services.
- On failure, include the parameter/case name in the assertion or JUnit display name.
- Verify every parameter is executed; do not skip a backend silently because its driver is unavailable.

## Definition of Done

- One parameterized test class covers each shared cross-engine behavior contract.
- All configured database cases pass with the same SQL and assertions.
- The forked test JVM can discover the runtime-scoped drivers through JDBC 4 service loading.
- The test remains on the project's Java 17 API baseline.