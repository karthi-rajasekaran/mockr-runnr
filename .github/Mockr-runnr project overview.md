# Mockr Runner – Project Overview

## Introduction

Mockr Runner is a lightweight mock API execution engine designed to simulate real backend services using configurable endpoints, dynamic conditions, and predefined responses. It enables teams to test frontend applications, integrations, automation flows, and QA scenarios without depending on live downstream systems.

The application stores mock definitions in a relational database and exposes runtime APIs that evaluate incoming requests, match conditions, and return the configured response payload.

## Key Objectives

* Reduce dependency on unavailable or incomplete backend systems.
* Enable faster frontend and integration testing.
* Support multiple projects and isolated mock environments.
* Return dynamic responses based on headers, query params, or request attributes.
* Centralized management of endpoints and reusable mock data.

## Core Features

* Multi-project mock management.
* Endpoint configuration by path and HTTP method.
* Multiple responses per endpoint.
* Conditional response selection.
* Default fallback response support.
* Custom response headers.
* JSON / XML / YAML payload support.
* Database-driven runtime execution.

---

# Database Schema

## Table Summary

| Table Name      | Purpose                                   |
| --------------- | ----------------------------------------- |
| project         | Stores logical projects or domains        |
| endpoint        | Stores mock API endpoints under a project |
| response        | Stores possible responses for an endpoint |
| condition       | Stores matching rules for a response      |
| response_header | Stores custom headers for a response      |

## Table Relationships

* One **Project** can have many **Endpoints**.
* One **Endpoint** can have many **Responses**.
* One **Response** can have many **Conditions**.
* One **Response** can have many **Response Headers**.

---

## 1. project

| Column     | Type      | Description            |
| ---------- | --------- | ---------------------- |
| id         | TEXT      | UUID primary key       |
| name       | TEXT      | Unique project name    |
| created_at | TIMESTAMP | Creation timestamp     |
| updated_at | TIMESTAMP | Last updated timestamp |

## 2. endpoint

| Column      | Type      | Description            |
| ----------- | --------- | ---------------------- |
| id          | TEXT      | UUID primary key       |
| project_id  | TEXT      | Reference to project   |
| path        | TEXT      | API path               |
| method      | TEXT      | HTTP method            |
| description | TEXT      | Endpoint description   |
| created_at  | TIMESTAMP | Creation timestamp     |
| updated_at  | TIMESTAMP | Last updated timestamp |

## 3. response

| Column        | Type    | Description           |
| ------------- | ------- | --------------------- |
| id            | TEXT    | UUID primary key      |
| description   | TEXT    | Response description  |
| status_code   | INTEGER | HTTP status code      |
| content_type  | TEXT    | MIME type             |
| response_body | TEXT    | Payload content       |
| is_default    | INTEGER | Default response flag |
| endpoint_id   | TEXT    | Reference to endpoint |

## 4. condition

| Column      | Type | Description           |
| ----------- | ---- | --------------------- |
| id          | TEXT | UUID primary key      |
| lhs         | TEXT | Left-hand operand     |
| operation   | TEXT | Comparison operator   |
| rhs         | TEXT | Right-hand operand    |
| response_id | TEXT | Reference to response |

## 5. response_header

| Column       | Type | Description           |
| ------------ | ---- | --------------------- |
| id           | TEXT | UUID primary key      |
| response_id  | TEXT | Reference to response |
| header_key   | TEXT | Header name           |
| header_value | TEXT | Header value          |

---

# Runtime Flow

1. Receive incoming HTTP request.
2. Identify endpoint using path + method.
3. Load all responses for that endpoint.
4. Evaluate conditions in priority/order.
5. Return first matching response.
6. If no match found, return default response.
7. Apply configured custom headers.

---

# Sample Endpoint Configuration

```json
{
  "path": "/mycontext/api/{sds}/sdfsd",
  "method": "GET",
  "description": "API karthi",
  "project": "2198e8c4-57eb-47c9-9b3d-83eb6ea1a589",
  "responses": [
    {
      "statusCode": 200,
      "description": "success case"
    },
    {
      "statusCode": 400,
      "description": "failure case"
    }
  ]
}
```

## Interpretation

* GET request mapped to configured path.
* Multiple responses available.
* Conditions determine whether success or failure response is returned.
* Headers can be injected in the final response.

---

# Suggested Future Enhancements

* Priority-based condition evaluation.
* Delay / timeout simulation.
* Randomized responses.
* Request body condition matching.
* Response templating with variables.
* Authentication support.
* Import / Export configurations.
* UI dashboard for administration.
* Metrics and request audit logs.

---

# Technology Suggestions

* Java / Spring Boot
* SQLite / PostgreSQL
* Flyway for migrations
* REST APIs
* Docker packaging
* React / Angular admin UI

---

# Conclusion

Mockr Runner acts as a configurable mock execution engine that helps development teams accelerate testing and integration cycles. With database-driven definitions and runtime condition matching, it provides a scalable foundation for enterprise-grade mocking solutions.
