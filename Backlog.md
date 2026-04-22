# Mockr Runner - Backlog

> Future optimizations and enhancements to be addressed after MVP.

---

## Logging & Observability

### Better Logging for Issue 9 (Response Resolution)

**Future Enhancement**: Add detailed logging strategy

```
Current: Basic logging (responseId, score, condition count)

Future:
- DEBUG level: Detailed condition evaluation (each condition match/fail)
- INFO level: Selected response with score
- WARN level: No matching responses (request details, sanitized)
- ERROR level: Evaluation failures/exceptions
- Metrics: Resolution time, cache hits/misses
- Trace ID: Include request correlation ID across logs
```

**Priority**: Medium  
**Related Issue**: Issue 9  
**Estimated Effort**: 2-3 hours

---

## Performance & Scalability

### Maximum Response & Condition Count Optimization

**Future Enhancement**: Handle large endpoint response lists efficiently

```
Current: Sequential evaluation (acceptable for MVP)

Future:
- Max responses per endpoint: 1000+ (configurable threshold)
- Max conditions per response: 50+ (configurable threshold)
- Parallel evaluation: Use virtual threads (Java 19+) or reactive streams
- Early termination: Stop evaluation once best match found
- Index-based lookup: Pre-index responses by condition source (headers, query, path)
- Caching: Cache condition evaluation results (optional)
```

**Priority**: High  
**Related Issue**: Issue 9  
**Trigger**: When evaluation time exceeds SLA (50ms)  
**Estimated Effort**: 4-6 hours

---

## Cache & Thread Safety

### Thread-Safe Cache Updates & Mutations

**Future Enhancement**: Handle concurrent cache updates

```
Current: Caffeine handles thread-safety for reads
         No mutation/updates from Issue 9 in MVP

Future:
- Thread-safe cache invalidation strategy
- Lock-free cache updates (compare-and-swap)
- Atomic response/condition swaps
- Cache coherency: Ensure responses and conditions stay in sync
- Cache versioning: Version-based cache invalidation (optional)
- Distributed cache invalidation (if multi-instance deployment)
```

**Priority**: High  
**Related Issue**: Issue 5 (Cache Service)  
**Estimated Effort**: 3-4 hours

---

### Immediate Cache Invalidation on Endpoint Mutations

**Future Enhancement**: Invalidate cache immediately when endpoints are modified

```
Current: Caffeine cache uses expireAfterAccess(10 minutes) TTL
         When endpoint HTTP method is changed (e.g., GET → POST), it's a new
         endpoint combination (path + method = unique key), but changes only
         reflect after 10+ minutes of cache inactivity

Problem:
- Endpoint configuration changes are delayed in runtime reflection
- HTTP method changes create new endpoint combos but old cached data persists
- Difficult to debug: mock behavior doesn't match recent configuration changes
- UX impact: Users reconfigure endpoints but see stale responses for 10 minutes

Future:
- Event-driven cache invalidation from Mockr-Keepr notifications
- Immediate invalidation on: endpoint path change, HTTP method change, response updates
- Cache invalidation API: Expose HTTP endpoint for manual invalidation
- Intelligent granular invalidation: Only invalidate affected endpoints (not full cache)
- Fallback: Keep TTL as safety net but prefer push-based invalidation
- Strategy: Webhook/event listener pattern between Mockr-Keepr and Mockr-Runnr
```

**Priority**: High  
**Severity**: Affects user experience and debugging  
**Related Issue**: Issue 5 (Cache Service)  
**Dependencies**: Requires event notification mechanism from Mockr-Keepr  
**Estimated Effort**: 2-3 hours

---

## Condition & Response Validation

### Entry-Point Validation (Mockr-Keepr & Mockr-Weavr)

**Future Enhancement**: Prevent confusion from unconfigured responses

```
Current: Issue 9 handles responses without conditions as default
         Can cause ambiguity in mock configuration

Future (in Mockr-Keepr):
- Enforce explicit default flag on responses (not just condition count = 0)
- Validate: Max 1 default response per endpoint
- Validate: All responses have at least 1 condition or explicit default flag
- Validation at configuration save time (not runtime)

Future (in Mockr-Weavr):
- UI validation: Prevent creation of ambiguous response sets
- UI warnings: Alert user if responses could be confused
- Default response management: Visual indicator for default responses
```

**Priority**: Medium  
**Related Issue**: Mockr-Keepr & Mockr-Weavr (not Mockr-Runnr)  
**Estimated Effort**: 3-4 hours (per service)

---

## Response Type & Content Handling

### Template Variable Replacement (Issue 8 Future)

**Future Enhancement**: Support dynamic response body generation

```
Current: Static response bodies (from database)

Future:
- Template syntax: ${request.header.x-correlation-id}, ${request.path.id}
- Expression language: Simple expressions for transformations
- Dynamic headers: Set response headers from request data
- Header mapping: Copy/transform request headers to response
- Body transformation: JSON path selection, XML XPath, etc.
```

**Priority**: Low  
**Related Issue**: Issue 8 (Response Builder)  
**Estimated Effort**: 4-5 hours

---

## Fallback & Default Response Handling

### Default Response Strategy (Post-Issue 9)

**Future Issue**: Create default fallback response handler

```
Current: Issue 9 returns 404 if no responses match
         No fallback configured at endpoint level

Future:
- Endpoint-level default response (explicit flag)
- Project-level default response (catchall)
- Strategy pattern: RETURN_404, RETURN_CONFIGURED, RETURN_CUSTOM
- Priority order: Endpoint default > Project default > 404
- Fallback logging: Track when defaults are used
```

**Priority**: Medium  
**Dependencies**: Issue 9 must be completed first  
**Estimated Effort**: 2-3 hours

---

## Monitoring & Metrics

### Response Resolution Metrics & Observability

**Future Enhancement**: Add observability for Issue 9

```
Current: Basic logging only

Future:
- Metrics: Response selection time (min/max/avg)
- Metrics: Cache hit rate for responses/conditions per endpoint
- Metrics: Score distribution (histogram of selected scores)
- Metrics: Failed resolutions (404 count, reason tracking)
- Alerts: Evaluation time exceeds SLA
- Distributed tracing: Request ID propagation across layers
- Prometheus exporter: Metrics endpoint for monitoring tools
```

**Priority**: Low  
**Related Issue**: Issue 9  
**Estimated Effort**: 3-4 hours

---

## Request Path Optimization (Issue 6 Future)

### Path Matching Performance Tuning

**Future Enhancement**: Optimize path matcher for large endpoint sets

```
Current: Path matcher supports exact and parameterized patterns ({var})
         Exact matches take precedence over parameterized matches
         O(n) segment comparison per request

Future:
- Trie-based prefix matching for O(log n) lookup
- Compiled pattern caching with LRU eviction
- Path pattern normalization (trailing slashes, double slashes)
- Performance: <1ms for path matching on 1000+ endpoints
- Benchmarking: Add micro-benchmarks to prevent regressions
```

**Priority**: Medium  
**Related Issue**: Issue 6 (Path Matcher)  
**Estimated Effort**: 2-3 hours

---

### Advanced Path Matching Patterns (Post-MVP Enhancement)

**Future Enhancement**: Support regex and wildcard path patterns

```
Current:
- Exact matching: /api/user == /api/user
- Parameterized matching: /api/user/{id} matches /api/user/1234
- Precedence: Exact > Parameterized

Future Patterns:
- Regex support: /api/user/{id:[0-9]+} (only numeric IDs)
- Wildcard prefix: /api/** (matches all /api/* paths)
- Wildcard suffix: /api/*/details (matches /api/users/details, /api/posts/details)
- Character class: /api/user/{id:[a-zA-Z0-9_-]+}

Precedence Order (highest to lowest):
1. Exact matches: /api/user
2. Character-constrained params: /api/user/{id:[0-9]+}
3. Parameterized matches: /api/user/{id}
4. Wildcard patterns: /api/**

Implementation Strategy:
- Extend PathMatcher enum MatchType with new types
- Create pattern compiler for regex patterns
- Add pattern precedence comparator
- Preserve backward compatibility with existing simple patterns
```

**Priority**: Low  
**Dependencies**: Current path matching (Issue 6) must be MVP complete  
**Estimated Effort**: 3-4 hours

## Condition Evaluation Optimization (Issue 7 Future)

### Condition Operator Extensions & Performance

**Future Enhancement**: Add new operators and optimize evaluation

```
Current: Operators - EQ, NEQ, GT, LT, IN, CONTAINS

Future Operators:
- REGEX: Pattern matching on string values
- BETWEEN: Numeric range checking
- STARTSWITH/ENDSWITH: String prefix/suffix matching
- LENGTH: String length validation
- EXISTS: Check if key exists
- CUSTOM: Extensible custom operators

Optimizations:
- Short-circuit evaluation: Stop on first failure
- Operator memoization: Cache operator instances
- Type inference: Determine operator type at cache load
```

**Priority**: Medium  
**Related Issue**: Issue 7 (Condition Evaluator)  
**Estimated Effort**: 2-3 hours

---

## API & Response Versioning

### Response Version Management

**Future Enhancement**: Support versioned responses

```
Current: No versioning support

Future:
- Response version field (v1, v2, etc.)
- Version-based condition routing (different conditions per version)
- Migration strategy: Graceful v1 → v2 switchover
- Backward compatibility: Support multiple versions simultaneously
```

**Priority**: Low  
**Estimated Effort**: 3-4 hours

---

## Analytics & Usage Tracking

### Response Selection Analytics

**Future Enhancement**: Track mock usage patterns

```
Current: No usage analytics

Future:
- Track which responses are selected most often
- Identify unused responses (never matched)
- Heat map: Which conditions match frequently
- Recommendations: Optimize response configuration based on usage
- Export analytics: CSV/JSON export for reporting
```

**Priority**: Low  
**Estimated Effort**: 2-3 hours

---

## Summary

| Backlog Item                        | Priority | Scope             | Related Issue | Est. Effort |
| ----------------------------------- | -------- | ----------------- | ------------- | ----------- |
| Better Logging                      | Medium   | Mockr-Runnr       | Issue 9       | 2-3h        |
| Max Response/Condition Optimization | High     | Mockr-Runnr       | Issue 9       | 4-6h        |
| Thread-Safe Cache Updates           | High     | Mockr-Runnr       | Issue 5       | 3-4h        |
| Entry-Point Validation              | Medium   | Mockr-Keepr/Weavr | Architecture  | 3-4h ea     |
| Template Variable Replacement       | Low      | Mockr-Runnr       | Issue 8       | 4-5h        |
| Default Response Handler            | Medium   | Mockr-Runnr       | Issue 9       | 2-3h        |
| Monitoring & Metrics                | Low      | Mockr-Runnr       | Issue 9       | 3-4h        |
| Path Matching Optimization          | Medium   | Mockr-Runnr       | Issue 6       | 2-3h        |
| Condition Operator Extensions       | Medium   | Mockr-Runnr       | Issue 7       | 2-3h        |
| Response Versioning                 | Low      | Mockr-Runnr       | Architecture  | 3-4h        |
| Analytics & Usage Tracking          | Low      | Mockr-Runnr       | Future        | 2-3h        |

**Total Backlog Effort**: ~32-47 hours (estimated)

---

**Next Steps**:

1. Complete MVP (Issues 1-10 in github-issues-plan.md)
2. Execute backlog items based on priority
3. Re-evaluate performance & scalability after load testing
4. Gather user feedback from Mockr-Keepr/Weavr teams
