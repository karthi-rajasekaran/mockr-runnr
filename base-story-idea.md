# Enhanced Planning Prompt for GitHub Issues via MCP (Mockr Runner)

## Objective

Help me generate high-quality GitHub Issues for **Mockr Runner** directly inside IDE using GitHub MCP.

Each issue must be implementation-ready so I can immediately start coding with GitHub Copilot.

Do not create vague issues.

Every issue must contain technical low-level guidance.

---

## Product Context

Mockr Runner is a runtime mock API engine.

It reads configuration from an existing SQLite DB managed by Mockr Keepr.

Runner receives HTTP requests, evaluates configured rules, and returns mock responses.

---

## MVP Scope

Must support:

* REST API request handling
* Path + method matching
* SQLite config loading
* On-demand cache
* 10 min idle expiry
* Condition matching
* Default fallback response
* Return headers/body/status

---

## Out of Scope

Ignore for now:

* Unit tests
* Metrics
* Structured logging
* Full preload cache
* Cache reload admin API
* Advanced security
* UI

---

## Mandatory Format for Every GitHub Issue

Each issue must include the following sections.

---

## 1. Title

Clear developer task title.

Example:

```text id="q0mg0p"
Create SQLite datasource configuration
```

---

## 2. Goal

Explain purpose of story in simple terms.

---

## 3. Low Level Design

Must include technical guidance like:

### Framework / Tech

Example:

* Use Spring Boot
* Use Spring Data JPA
* Hibernate
* SQLite dialect
* Constructor injection

### Suggested Classes

Example:

```text id="s3jz0w"
DatabaseConfig
DatasourceProperties
EndpointRepository
EndpointEntity
```

### Suggested Methods

Example:

```text id="mpp5y6"
createDataSource()
entityManagerFactory()
validateDbPath()
findByMethodAndPath(method, path)
```

### Inputs / Outputs

Example:

```text id="c7hjtn"
Input:
MOCKR_DB_PATH env variable

Output:
Spring datasource bean connected to SQLite file
```

### Flow

Example:

1. Read env variable
2. Validate file path
3. Build datasource
4. Enable JPA repositories

---

## 4. Acceptance Criteria

Must be measurable.

Example:

* App starts successfully with valid DB path
* App fails clearly if DB missing
* JPA can query existing tables
* No schema auto creation
* Connection uses SQLite file

---

## 5. Priority

High / Medium / Low

---

## 6. Copilot Coding Prompt

Add one ready-to-use prompt.

Example:

```text id="g1t9ea"
Implement SQLite datasource config using Spring Boot + Spring Data JPA + Hibernate. Use env MOCKR_DB_PATH. Do not auto create schema.
```

---

## Story Generation Rules

When generating issues:

* Break work into small stories
* Each story should be finishable independently
* Prefer vertical slices
* Avoid huge stories
* Keep class names realistic
* Keep method names Java standard
* Follow clean architecture

---

## Example Stories to Generate

1. SQLite datasource config
2. JPA entities for project/endpoint/response tables
3. Repository layer
4. Catch-all runtime controller
5. On-demand cache service
6. Path matcher engine
7. Condition evaluator
8. Response builder
9. Default fallback logic
10. Exception handler

---

## Final Instruction

Generate GitHub Issues like a senior Java architect preparing tasks for a productive developer using Copilot.

Each issue should be so clear that coding can start immediately without further clarification.
