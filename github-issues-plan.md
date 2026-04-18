# Mockr Runner - GitHub Issues Implementation Plan

> Planned issues for execution. Verify before creating in GitHub.

---

## Issue 1: Add SQLite Dialect and Configure Application Properties

### Title

Copy SQLite dialect from Mockr Keepr and configure application.yaml for datasource

### Goal

Copy SQLite Hibernate dialect from mockr-keepr project and configure datasource properties in application.yaml. Eliminate need for custom DatabaseConfig beans. Use Spring Boot auto-configuration with externalized YAML configuration.

### Low Level Design

#### Framework / Tech

- Spring Boot 3.x auto-configuration
- Hibernate with SQLite dialect (copied from mockr-keepr)
- YAML configuration in application.yaml
- Spring Data JPA auto-configuration
- HikariCP connection pooling (default)

#### Suggested Classes

```
SQLiteDialect (copy from mockr-keepr)
```

#### Suggested Files/Config

```
application.yaml:
  spring:
    datasource:
      url: jdbc:sqlite:${MOCKR_DB_PATH}
      driverClassName: org.sqlite.JDBC
    jpa:
      database-platform: com.mockr.keepr.SQLiteDialect
      hibernate.ddl-auto: validate
```

#### Inputs / Outputs

```
Input:
- SQLiteDialect.java from mockr-keepr project
- MOCKR_DB_PATH environment variable

Output:
- SQLiteDialect copied to project
- application.yaml configured
- Spring auto-configuration handles datasource + JPA setup
```

#### Flow

1. Copy SQLiteDialect.java from mockr-keepr to com.mockr.runnr.config
2. Add spring.datasource.url with MOCKR_DB_PATH variable
3. Configure spring.jpa.database-platform to use SQLiteDialect
4. Set ddl-auto to 'validate' (no schema creation)
5. Verify Spring auto-configuration loads DataSource and EntityManagerFactory
6. Test application startup with valid DB file

### Acceptance Criteria

- SQLiteDialect is copied and compiles without errors
- application.yaml has datasource configuration
- Application starts successfully with valid DB path
- DataSource bean is auto-configured
- EntityManagerFactory bean is auto-configured
- No schema auto-generation (ddl-auto=validate)
- Connection pooling works (HikariCP)
- No custom @Configuration classes needed

### Priority

High

### Copilot Coding Prompt

```
Copy SQLiteDialect.java from mockr-keepr project to com.mockr.runnr.config.
Configure application.yaml with datasource url using MOCKR_DB_PATH environment variable.
Set spring.jpa.database-platform to the SQLiteDialect class.
Set spring.jpa.hibernate.ddl-auto to 'validate'.
Remove any custom @Configuration beans - rely on Spring Boot auto-configuration.
```

---

## Issue 2: Copy JPA Entities from Mockr Keepr and Evaluate for Immutability

### Title

Copy entity classes from Mockr Keepr and refactor to use records for immutability

### Goal

Copy existing JPA entity classes (ProjectEntity, EndpointEntity, ResponseEntity, ConditionEntity) from mockr-keepr project. Evaluate if entities can be refactored to use Java records for immutability, or keep existing design if records cause compatibility issues.

### Low Level Design

#### Framework / Tech

- Spring Data JPA
- Hibernate annotations
- Java records (if compatible) OR existing entity classes
- Read-only access pattern
- No @ManyToOne cascades

#### Suggested Classes

```
ProjectEntity (record or copy as-is)
EndpointEntity (record or copy as-is)
ResponseEntity (record or copy as-is)
ConditionEntity (record or copy as-is)
ConditionOperator enum (copy as-is)
```

#### Inputs / Outputs

```
Input:
- Entity classes from mockr-keepr project
- Mockr Keepr schema mappings

Output:
- Entities copied to com.mockr.runnr.domain
- Entities refactored to records (if feasible)
- ConditionOperator enum available
```

#### Flow

1. Identify entity classes in mockr-keepr (ProjectEntity, EndpointEntity, ResponseEntity, ConditionEntity)
2. Copy to com.mockr.runnr.domain package
3. Evaluate if records are compatible (Hibernate + JPA support)
4. If records work: refactor to records with @Table, @Column, @Id annotations
5. If records don't work: keep existing entity class design
6. Ensure no schema auto-generation is triggered
7. Copy ConditionOperator enum
8. Verify all entities compile and map correctly to tables

### Acceptance Criteria

- All entity classes copied from mockr-keepr
- Entities map correctly to existing SQLite tables
- ConditionOperator enum is available
- Entities compile without errors
- No schema auto-generation triggered
- Entities are either immutable records or final classes
- Entities can be queried via repositories
- If records used: verify Hibernate compatibility
- Primary keys and foreign keys mapped correctly

### Priority

High

### Copilot Coding Prompt

```
Copy entity classes from mockr-keepr: ProjectEntity, EndpointEntity, ResponseEntity, ConditionEntity.
Copy ConditionOperator enum.
Evaluate if Java records can be used for immutability with Hibernate annotations.
If records work, refactor entities to use records with @Table, @Column, @Id annotations.
If records incompatible, keep existing entity class design.
Ensure all entities map to existing SQLite tables without triggering schema generation.
Verify entities can be queried via Spring Data JPA repositories.
```

---

## Issue 3: Copy Repositories from Mockr Keepr and Evaluate for Runnr Needs

### Title

Copy repository interfaces from Mockr Keepr and evaluate if custom methods are needed

### Goal

Copy existing Spring Data JPA repository interfaces from mockr-keepr project. Evaluate if existing query methods are sufficient for Mockr Runnr's runtime matching needs, or if additional optimized queries are needed. Keep repositories focused on read-only access patterns.

### Low Level Design

#### Framework / Tech

- Spring Data JPA Repository interface
- Custom query methods with @Query (if needed)
- Read-only access pattern
- Query optimization for read-heavy workloads

#### Suggested Classes

```
ProjectRepository (copy from mockr-keepr, evaluate)
EndpointRepository (copy from mockr-keepr, add findByProjectIdAndMethodAndPath if missing)
ResponseRepository (copy from mockr-keepr, evaluate)
ConditionRepository (copy from mockr-keepr, evaluate)
```

#### Key Query Methods to Verify

```
EndpointRepository:
  - findByProjectIdAndMethodAndPath(Long projectId, String method, String path)
  - findByProjectId(Long projectId)

ConditionRepository:
  - findByResponseId(Long responseId)
```

#### Inputs / Outputs

```
Input:
- Repository interfaces from mockr-keepr
- Entity classes (ProjectEntity, EndpointEntity, ResponseEntity, ConditionEntity)

Output:
- Repositories copied to com.mockr.runnr.repository
- Custom methods added or verified for runtime matching
- All methods @Transactional(readOnly=true)
```

#### Flow

1. Copy repository interfaces from mockr-keepr
2. Evaluate existing query methods against Runnr requirements
3. Check if findByProjectIdAndMethodAndPath exists (critical for path matching)
4. Add custom @Query methods if existing ones don't support Runnr's runtime matching
5. Ensure all methods use @Transactional(readOnly=true)
6. Remove any mutation methods (save, delete)
7. Verify queries are index-friendly for performance
8. Test repository methods against SQLite database

### Acceptance Criteria

- All repositories copied from mockr-keepr
- Repositories extend JpaRepository correctly
- findByProjectIdAndMethodAndPath query exists and works efficiently
- findByResponseId query exists and works efficiently
- All query methods have @Transactional(readOnly=true)
- No mutation methods present (save, delete, update)
- Repositories compile without errors
- Repositories can be injected into services
- Queries execute efficiently against SQLite
- Query results support runtime matching logic

### Priority

High

### Copilot Coding Prompt

```
Copy repository interfaces from mockr-keepr: ProjectRepository, EndpointRepository, ResponseRepository, ConditionRepository.
Evaluate existing query methods for Runnr's runtime matching needs.
Ensure findByProjectIdAndMethodAndPath(projectId, method, path) exists in EndpointRepository.
Ensure findByResponseId(responseId) exists in ConditionRepository.
Add custom @Query methods if existing queries don't support efficient runtime matching.
Remove any mutation methods (save, delete, update).
Add @Transactional(readOnly=true) to all repository methods.
Optimize queries for read-heavy access patterns.
```

---

## Issue 4: Create Catch-All REST API Controller

### Title

Create REST API controller for mock request handling

### Goal

Implement a catch-all Spring Boot REST controller that accepts incoming HTTP requests on any path and method. Route requests to service layer for processing. Keep controller thin and focused on HTTP concerns only.

### Low Level Design

#### Framework / Tech

- Spring Web @RestController
- @RequestMapping with wildcards or @PostMapping/@GetMapping
- RequestEntity / HttpServletRequest for capturing all details
- ResponseEntity for response building
- Global exception handler (created later)

#### Suggested Classes

```
MockApiController
MockRequest
MockApiResponse
ControllerException
```

#### Suggested Methods

```
handleRequest(
  HttpServletRequest request,
  @RequestBody(required=false) String body
) : ResponseEntity<?>

getAllHeaders(HttpServletRequest request) : Map<String, String>
getAllQueryParams(HttpServletRequest request) : Map<String, String>
```

#### Inputs / Outputs

```
Input:
- HTTP request (any method, any path)
- Request headers
- Request body (if present)
- Query parameters

Output:
- ResponseEntity with status, headers, and body
```

#### Flow

1. Create @RestController class MockApiController
2. Add catch-all mapping: @RequestMapping("/\*\*")
3. Capture request method (GET, POST, PUT, DELETE, etc.)
4. Capture request path
5. Capture request headers (all)
6. Capture request body (if present)
7. Capture query parameters (all)
8. Pass to MockApiService for processing
9. Return ResponseEntity with response status, headers, body
10. Let global exception handler manage errors

### Acceptance Criteria

- Controller accepts all HTTP methods (GET, POST, PUT, DELETE, etc.)
- Controller accepts all URI paths (catch-all routing)
- Request headers are captured and passed to service
- Request body is captured and passed to service
- Query parameters are captured and passed to service
- Request method is captured and logged
- Response headers from service are returned in ResponseEntity
- Response body from service is returned in ResponseEntity
- Response status code from service is returned
- Controller does not contain business logic
- Request/response are logged (path, method, status)

### Priority

High

### Copilot Coding Prompt

```
Create REST controller MockApiController with catch-all routing.
Use @RequestMapping("/**") to accept all paths and methods.
Accept HttpServletRequest to capture all request details.
Capture headers, query parameters, body, method, path.
Pass captured request to MockApiService.handleRequest().
Return ResponseEntity with status, headers, body from service.
Log request path, method, and response status.
Keep controller thin - no business logic.
```

---

## Issue 5: Create On-Demand Cache Service with Caffeine for 10-Minute Idle Expiry

### Title

Create on-demand cache service using Caffeine cache with 10-minute idle expiry

### Goal

Implement a cache service using Caffeine (high-performance caching library) that loads endpoint configurations on-demand and expires after 10 minutes of inactivity. Leverage Caffeine's built-in expiry, statistics, and thread-safety for production-grade caching.

### Low Level Design

#### Framework / Tech

- Caffeine cache library
- Spring @Service with @Cacheable (using Caffeine backend)
- Spring CacheManager with Caffeine configuration
- Expiry policy: expireAfterAccess(10 minutes)
- Cache statistics via Caffeine's recordStats()

#### Suggested Classes

```
CacheConfiguration (Spring @Configuration for Caffeine setup)
CacheService (@Service using @Cacheable)
CacheStatistics (expose Caffeine stats via endpoint)
```

#### Suggested Methods

```
CacheConfiguration:
  - cacheManager() : CacheManager
  - caffeineConfig() : Caffeine<Object, Object>

CacheService:
  - @Cacheable("endpoints") getOrLoadEndpoints(Long projectId) : List<EndpointEntity>
  - @Cacheable("endpoint") getOrLoadEndpoint(Long id) : EndpointEntity
  - @CacheEvict(allEntries=true) invalidateCache() : void
  - getCacheStats() : Map<String, Object>
```

#### Inputs / Outputs

```
Input:
- Project ID or Endpoint ID
- Caffeine cache TTL: 10 minutes

Output:
- Cached endpoint configurations
- Cache hit/miss statistics
```

#### Flow

1. Add Caffeine dependency to build.gradle
2. Create CacheConfiguration with Spring CacheManager
3. Configure Caffeine with expireAfterAccess(10, TimeUnit.MINUTES)
4. Enable recordStats() for cache statistics
5. Create CacheService with @Cacheable methods
6. Implement getOrLoadEndpoints() with database fallback
7. Implement invalidateCache() for manual flush
8. Optional: expose cache stats via admin endpoint
9. Log cache hits/misses/evictions

### Acceptance Criteria

- Caffeine cache library is integrated
- Cache TTL is 10 minutes (expireAfterAccess)
- Cache loads endpoints on first access
- Cache returns cached data on subsequent accesses within 10 minutes
- Cache automatically evicts expired entries (no manual cleanup needed)
- invalidateCache() clears all cached data
- Cache statistics track hits, misses, evictions
- Cache is thread-safe (built-in to Caffeine)
- Cache performance is optimal (compiled code, no reflection)
- Configuration is externalized (duration, size limits)

### Priority

Medium

### Copilot Coding Prompt

```
Create Caffeine cache configuration in CacheConfiguration class.
Configure cache manager with CaffeineCacheManager.
Set expireAfterAccess(10, TimeUnit.MINUTES) for idle expiry.
Enable recordStats() for cache statistics.
Create CacheService with @Cacheable methods.
Implement @Cacheable("endpoints") for getOrLoadEndpoints(projectId).
Implement @Cacheable("endpoint") for getOrLoadEndpoint(id).
Implement @CacheEvict(allEntries=true) for invalidateCache().
Fallback to repository when cache miss.
Log cache hit/miss statistics.
```

---

## Issue 6: Create Path Matcher Engine for Request Routing

### Title

Create path matching engine for endpoint path resolution

### Goal

Implement intelligent path matching to resolve incoming request paths against configured endpoint paths. Support exact matches, wildcard patterns, and path variables. Optimize for O(1) or O(log n) lookup.

### Low Level Design

#### Framework / Tech

- Strategy Pattern for different matcher types
- Factory Pattern for matcher selection
- Compiled regex patterns (cached)
- HashMap for exact match lookups (O(1))
- Trie or sorted map for prefix matching (O(log n))

#### Suggested Classes

```
PathMatcher
PathMatcherFactory
ExactPathMatcher
WildcardPathMatcher
RegexPathMatcher
PathMatchResult
MatcherException
```

#### Suggested Methods

```
PathMatcher:
  - matches(String requestPath, String configuredPath) : boolean
  - extract(String requestPath, String configuredPath) : Map<String, String>

PathMatcherFactory:
  - createMatcher(String pattern) : PathMatcher

PathMatchResult:
  - boolean isMatch()
  - Map<String, String> getPathVariables()
```

#### Inputs / Outputs

```
Input:
- Incoming request path (e.g., /api/users/123)
- Configured endpoint path pattern (e.g., /api/users/{id})

Output:
- Match result (matched or not)
- Extracted path variables (e.g., {id=123})
```

#### Flow

1. Analyze configured path pattern for type (exact, wildcard, regex)
2. Create appropriate PathMatcher implementation
3. Cache compiled matchers to avoid recompilation
4. On request: test request path against endpoint patterns in order
5. Return first match with extracted variables
6. Log match attempts and results

### Acceptance Criteria

- Exact path matching works (/api/users == /api/users)
- Wildcard matching works (/api/\* matches /api/users)
- Path variable extraction works (/api/users/{id})
- Multiple path variables work (/api/{org}/{repo}/{id})
- Compiled patterns are cached for performance
- Matchers are thread-safe
- Non-matching returns null or empty result
- Path variable extraction is accurate
- Performance is O(1) or O(log n)

### Priority

High

### Copilot Coding Prompt

```
Create path matching engine with Strategy and Factory patterns.
Support exact path matching, wildcard patterns, and path variables.
Create PathMatcher interface with implementations: ExactPathMatcher, WildcardPathMatcher, RegexPathMatcher.
Create PathMatcherFactory to select appropriate matcher.
Cache compiled regex patterns.
Implement path variable extraction (e.g., /users/{id} extracts {id}).
Return PathMatchResult with match status and extracted variables.
Optimize for O(1) or O(log n) lookup.
```

---

## Issue 7: Create Condition Evaluator for Rule Matching

### Title

Create condition evaluator engine for request condition evaluation

### Goal

Implement flexible condition evaluation supporting multiple operators (EQ, NEQ, GT, LT, IN, CONTAINS). Evaluate conditions against request headers, query parameters, and path variables. Support future extension for new operators.

### Low Level Design

#### Framework / Tech

- Strategy Pattern for operators
- Specification Pattern for conditions
- Enum for supported operators
- Predicate<T> for condition logic
- Immutable condition objects

#### Suggested Classes

```
ConditionEvaluator
Condition
ConditionOperator enum
EqualityOperator
ContainsOperator
GreaterThanOperator
InOperator
OperatorFactory
EvaluationContext
```

#### Suggested Methods

```
ConditionEvaluator:
  - evaluate(List<Condition> conditions, EvaluationContext ctx) : boolean
  - evaluateSingle(Condition condition, EvaluationContext ctx) : boolean

Condition:
  - String getKey()
  - String getValue()
  - ConditionOperator getOperator()

OperatorFactory:
  - getOperator(ConditionOperator op) : ConditionOperatorImpl
```

#### Inputs / Outputs

```
Input:
- List of conditions (from database)
- EvaluationContext (headers, query params, path variables)

Output:
- boolean (all conditions match or not)
```

#### Flow

1. Receive list of conditions and evaluation context
2. For each condition:
   a. Extract source (header.X-API-Key, query.limit, path.id)
   b. Get condition value from context
   c. Apply operator (EQ, NEQ, GT, LT, IN, CONTAINS)
   d. Return match result
3. Return true only if ALL conditions match (AND logic)
4. Log condition evaluation (key, operator, value, result)

### Acceptance Criteria

- Evaluator supports EQ, NEQ, GT, LT, IN, CONTAINS operators
- Evaluator extracts header values correctly (header.X-API-Key)
- Evaluator extracts query param values correctly (query.limit)
- Evaluator extracts path variables correctly (path.id)
- Evaluator applies operators correctly (e.g., EQ compares strings exactly)
- Evaluator returns true only if ALL conditions match (AND logic)
- New operators can be added without modifying existing code (open/closed)
- Condition evaluation is logged with context
- Case sensitivity is configurable per operator
- null/missing values are handled gracefully

### Priority

Medium

### Copilot Coding Prompt

```
Create condition evaluator with Strategy Pattern for operators.
Support operators: EQ, NEQ, GT, LT, IN, CONTAINS.
Implement ConditionEvaluator.evaluate(conditions, context).
Extract condition values from headers (header.X-Header), query params (query.param), path variables (path.var).
Create ConditionOperator enum and operator implementations.
Return true only if ALL conditions match.
Make operators extensible via OperatorFactory.
Log condition evaluation results.
Handle null/missing values gracefully.
```

---

## Issue 8: Create Extensible Response Builder with Strategy Pattern for Multiple Response Types

### Title

Create extensible response builder supporting JSON, XML, HTML, files, SOAP, GraphQL responses

### Goal

Build HTTP mock responses using Strategy Pattern to support multiple response types: JSON, XML, HTML, binary files, SOAP, GraphQL. Extensible design allows adding new response types without modifying existing code. Construct ResponseEntity ready for Spring to serialize with appropriate Content-Type and encoding.

### Low Level Design

#### Framework / Tech

- Strategy Pattern for response type handlers
- Builder Pattern for fluent construction
- ResponseType enum (JSON, XML, HTML, FILE, SOAP, GRAPHQL)
- ResponseStrategy interface for type-specific handling
- Immutable response objects
- Content-Type detection and encoding

#### Suggested Classes

```
ResponseBuilder
MockResponse
ResponseStrategy (interface)
JsonResponseStrategy implements ResponseStrategy
XmlResponseStrategy implements ResponseStrategy
FileResponseStrategy implements ResponseStrategy
SoapResponseStrategy implements ResponseStrategy
GraphQLResponseStrategy implements ResponseStrategy
ResponseTypeEnum (JSON, XML, HTML, FILE, SOAP, GRAPHQL, BINARY)
ResponseStrategyFactory
ResponseBuilderException
```

#### Suggested Methods

```
ResponseBuilder:
  - withStatus(int statusCode) : ResponseBuilder
  - withHeader(String key, String value) : ResponseBuilder
  - withBody(String body) : ResponseBuilder
  - withResponseType(ResponseType type) : ResponseBuilder
  - withFilePath(String filePath) : ResponseBuilder (for FILE type)
  - build() : ResponseEntity<?>
  - buildRaw(byte[] data) : ResponseEntity<?> (for binary)

ResponseStrategy:
  - ResponseEntity<?> handle(ResponseBuilder builder) : ResponseEntity<?>
  - String getContentType() : String
  - String getCharacterEncoding() : String

ResponseStrategyFactory:
  - getStrategy(ResponseType type) : ResponseStrategy
```

#### Inputs / Outputs

```
Input:
- ResponseEntity data from database (status, headers, body, type)
- Response type (JSON, XML, FILE, SOAP, GraphQL, etc.)
- Optional file path (for FILE type)
- Optional encoding (UTF-8, etc.)

Output:
- ResponseEntity<?> with appropriate headers, body, and Content-Type
- Binary responses (for files)
- Properly encoded responses (UTF-8, etc.)
```

#### Flow

1. Receive response entity from database with type indicator
2. Create ResponseBuilder
3. Set status code and headers
4. Set response type (JSON, XML, FILE, SOAP, GraphQL)
5. Set body or file path based on type
6. ResponseStrategyFactory selects appropriate handler
7. Strategy applies type-specific transformations (encoding, headers)
8. Build ResponseEntity with correct Content-Type
9. Return to controller

### Acceptance Criteria

- ResponseBuilder supports multiple response types (JSON, XML, HTML, FILE, SOAP, GraphQL)
- Strategy Pattern allows adding new response types without modifying existing code
- Each strategy handles Content-Type detection and encoding
- File responses read and stream binary data correctly
- Status codes are validated (100-599)
- Headers are correctly set by strategy (Content-Type, encoding)
- Response body is preserved as-is
- Template variable replacement supported (future enhancement)
- ResponseEntity is serializable by Spring
- Immutable response objects
- ResponseBuilder uses fluent API
- Errors are logged with context
- New response types can be added by creating new Strategy implementation

### Priority

Medium (High for extensibility)

### Copilot Coding Prompt

```
Create extensible response builder using Strategy Pattern.
Implement ResponseBuilder with methods: withStatus(), withHeader(), withBody(), withResponseType(), withFilePath(), build().
Create ResponseStrategy interface with handle() method.
Implement strategies: JsonResponseStrategy, XmlResponseStrategy, FileResponseStrategy, SoapResponseStrategy, GraphQLResponseStrategy.
Each strategy handles Content-Type detection, encoding, and type-specific serialization.
Create ResponseStrategyFactory to select appropriate strategy by ResponseType enum.
Support response types: JSON, XML, HTML, FILE, SOAP, GRAPHQL, BINARY.
File strategy should stream binary data efficiently.
Return ResponseEntity<?> from build() with appropriate headers and body.
Use immutable MockResponse objects.
Validate inputs and throw ResponseBuilderException on errors.
Enable future response types by extending Strategy interface.
```

---

## Issue 9: Create Default Fallback Response Handler

### Title

Create default fallback response handler for non-matching requests

### Goal

Implement default response handler to return configured fallback response when no endpoint matches the incoming request. Prevent 404 errors and provide configurable default behavior.

### Low Level Design

#### Framework / Tech

- Strategy Pattern for fallback strategies
- Configuration for default response
- Spring @Configuration for default bean setup

#### Suggested Classes

```
FallbackResponseHandler
FallbackResponse
FallbackStrategy enum (RETURN_CONFIGURED, RETURN_404, RETURN_CUSTOM)
DefaultResponseConfig
```

#### Suggested Methods

```
FallbackResponseHandler:
  - handle(MockRequest request, Long projectId) : ResponseEntity<?>
  - getDefaultResponse() : FallbackResponse

FallbackResponse:
  - int getStatusCode()
  - String getBody()
  - Map<String, String> getHeaders()
```

#### Inputs / Outputs

```
Input:
- Request that did not match any endpoint
- Project ID

Output:
- Default ResponseEntity (e.g., 404 or configured response)
```

#### Flow

1. Request does not match any endpoint pattern
2. FallbackResponseHandler.handle() is called
3. Load default response for project from database (if configured)
4. If configured response exists, return it
5. If not configured, return 404 with error message
6. Log fallback event (request path, method, reason)

### Acceptance Criteria

- Fallback handler returns configured default response if available
- Fallback handler returns 404 if no default configured
- Default response includes status, headers, body
- Fallback events are logged
- FallbackResponseHandler is injectable service
- Default response is cacheable (loaded once per project)

### Priority

Medium

### Copilot Coding Prompt

```
Create FallbackResponseHandler for non-matching requests.
Load default response from database for the project.
If default response exists, return it via ResponseEntity.
If no default response, return 404 with descriptive error message.
Log fallback events (path, method, status).
Use ResponseBuilder to construct fallback response.
Fallback responses should also be cached.
```

---

## Issue 10: Create Global Exception Handler for Error Management

### Title

Create global exception handler for consistent error responses

### Goal

Implement Spring @ControllerAdvice for consistent error handling across all endpoints. Return structured error responses with status, message, and timestamp. Log all errors appropriately.

### Low Level Design

#### Framework / Tech

- Spring @ControllerAdvice
- @ExceptionHandler for specific exceptions
- Spring HttpMessageNotReadable (for JSON errors)
- Structured error response DTOs

#### Suggested Classes

```
GlobalExceptionHandler
ErrorResponse
ErrorDetails
ApplicationException (base)
DatabaseException extends ApplicationException
ValidationException extends ApplicationException
```

#### Suggested Methods

```
GlobalExceptionHandler:
  - handleApplicationException(ApplicationException ex) : ResponseEntity<ErrorResponse>
  - handleDatabaseException(DatabaseException ex) : ResponseEntity<ErrorResponse>
  - handleValidationException(ValidationException ex) : ResponseEntity<ErrorResponse>
  - handleGenericException(Exception ex) : ResponseEntity<ErrorResponse>

ErrorResponse:
  - int getStatusCode()
  - String getMessage()
  - String getError()
  - long getTimestamp()
  - String getPath()
```

#### Inputs / Outputs

```
Input:
- Exception from any controller or service

Output:
- ResponseEntity<ErrorResponse> with structured error
```

#### Flow

1. Exception thrown in controller or service
2. @ControllerAdvice catches exception
3. Map exception to appropriate status code
4. Create ErrorResponse with status, message, timestamp
5. Log error with stack trace (for 5xx errors)
6. Log error without stack trace (for 4xx errors)
7. Return ResponseEntity with error response

### Acceptance Criteria

- All exceptions caught and handled
- ApplicationException -> 400 Bad Request
- DatabaseException -> 503 Service Unavailable
- ValidationException -> 400 Bad Request
- Generic Exception -> 500 Internal Server Error
- Error responses are JSON with status, message, timestamp
- Errors are logged with appropriate level (WARN/ERROR)
- Stack traces logged for 5xx errors only
- Error response includes request path
- Timestamp is ISO 8601 format

### Priority

Medium

### Copilot Coding Prompt

```
Create GlobalExceptionHandler with @ControllerAdvice.
Handle ApplicationException, DatabaseException, ValidationException.
Return ErrorResponse with statusCode, message, error, timestamp, path.
Map ApplicationException -> 400 Bad Request.
Map DatabaseException -> 503 Service Unavailable.
Map ValidationException -> 400 Bad Request.
Map generic Exception -> 500 Internal Server Error.
Log errors appropriately (stack trace for 5xx, message only for 4xx).
Error responses must be JSON.
```

---

## Implementation Sequence Recommendation

1. **Issue 1**: Create SQLite datasource (foundation)
2. **Issue 2**: Create JPA entities (data model)
3. **Issue 3**: Create repositories (data access)
4. **Issue 4**: Create REST controller (entry point)
5. **Issue 6**: Create path matcher (core logic)
6. **Issue 7**: Create condition evaluator (core logic)
7. **Issue 8**: Create response builder (response handling)
8. **Issue 5**: Create cache service (optimization)
9. **Issue 9**: Create fallback handler (edge cases)
10. **Issue 10**: Create exception handler (error management)

---

## Summary

- **Total Issues**: 10
- **High Priority**: 4 (Issues 1, 2, 3, 4, 6)
- **Medium Priority**: 6 (Issues 5, 7, 8, 9, 10)
- **Estimated Scope**: Vertical slice to working MVP
- **Architecture**: Clean, layered, extensible
- **Ready for**: GitHub Issues + Copilot coding
