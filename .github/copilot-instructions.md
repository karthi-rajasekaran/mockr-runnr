# GitHub Copilot Instructions for Mockr Runner

## Project Context

Mockr Runner is a lightweight high-performance runtime mock API engine.

It reads mock configurations from an existing SQLite database managed by another application called **Mockr Keepr**.

Mockr Runner must expose runtime APIs that evaluate incoming HTTP requests and return configured mock responses based on path, method, conditions, and default rules.

This application is optimized for **read-heavy traffic**, low latency, and maintainable enterprise-grade architecture.

---

## Primary Goals

1. Fast request matching
2. Low memory footprint
3. Clean architecture
4. Easy maintainability
5. Production-ready code
6. Scalable design for future enhancements

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Web
* Spring JDBC / JPA (choose lightweight approach)
* SQLite
* Maven
* JUnit 5
* Mockito

---

## Mandatory Engineering Standards

### Coding Style

* Write clean, readable, production-grade code.
* Use meaningful names.
* Keep methods small and focused.
* Prefer immutability.
* Avoid duplicated code.
* Use records where appropriate.
* Use constructor injection only.
* No field injection.
* No unnecessary comments.
* Self-documenting code preferred.

### SOLID Principles

Always follow:

* Single Responsibility Principle
* Open/Closed Principle
* Interface Segregation
* Dependency Inversion

---

## Architecture Rules

Use layered architecture:

* controller → request entry
* service → business logic
* repository → database access
* matcher → condition/path evaluation
* model/dto → data contracts
* config → Spring configuration

Controllers must stay thin.

Business logic must not be inside controllers.

---

## Database Rules (Critical)

SQLite database is owned by **Mockr Keepr**.

Mockr Runner must only consume it.

### Never Do These

* Do not create schema
* Do not auto-generate tables
* Do not run ddl-auto=create
* Do not silently create in-memory DB
* Do not mutate schema unexpectedly

### Mandatory

* Connect only to configured DB path
* If DB file missing → fail startup clearly
* Use read-only connection when feasible
* Validate required tables at startup
* Externalize DB path using environment variable

Example:

MOCKR_DB_PATH=/apps/data/mockr.db

---

## Performance Rules

Mockr Runner is read-heavy.

Optimize reads aggressively.

### Required

* Cache endpoint metadata on startup
* Use HashMap lookups where possible
* Avoid repeated DB calls per request
* Avoid unnecessary object creation
* Reuse immutable objects
* Prefer O(1) access patterns
* Avoid nested loops for matching
* Minimize reflection usage

### Memory

* Avoid loading unused data
* Use lightweight DTOs
* Reuse parsed path patterns
* Avoid large temporary collections

---

## Runtime Matching Rules

Incoming request should match using:

1. HTTP method
2. Path
3. Conditions
4. Default response fallback

### Conditions Examples

* header.x-api-key EQ abc
* query.order NEQ 10
* path variable checks
* future extensible operators

### Prefer Patterns

* Strategy Pattern for operators
* Factory Pattern for matcher selection
* Specification Pattern for conditions
* Builder Pattern for response creation

---

## Spring Boot Rules

* Use @ConfigurationProperties for configs
* Use profiles (dev, prod)
* Use global exception handling
* Validate startup configuration
* Graceful startup failure on bad config

---

## Logging Rules

* Structured logs preferred
* Log startup summary
* Log cache load counts
* Log request path + method
* Log matching failures
* Log errors with stack traces
* Never log secrets or sensitive headers

---

## Testing Rules

Generate tests for all critical modules.

### Must Cover

* Path matcher
* Condition evaluator
* Default fallback logic
* Repository loading
* Startup validation

Use:

* JUnit 5
* Mockito
* Integration tests with file-based SQLite

---

## Code Generation Preferences

When generating code:

* Prefer simpler implementation first
* Avoid overengineering
* Keep extension points open
* Write reusable classes
* Return complete compilable code

---

## Anti-Patterns to Avoid

* God classes
* Huge services
* Static utility abuse
* Deep inheritance chains
* Premature microservices
* Reflection-heavy logic
* Excessive annotations

---

## Suggested Build Order

1. Bootstrap project
2. SQLite config
3. Repository layer
4. Cache loader
5. Path matcher
6. Condition engine
7. Runtime API controller
8. Response builder
9. Exception handling
10. Metrics / logs

---

## Final Instruction

Always think like a Senior Java Architect building a high-performance runtime engine, not a demo CRUD project.
