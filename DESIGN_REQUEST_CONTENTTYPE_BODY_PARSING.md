# Design: Request Content-Type Based Body Parsing

## Overview

Extend the Endpoint model with a `requestContentType` enum field that explicitly defines how incoming request bodies should be parsed and flattened for condition evaluation. This gives API designers explicit control over body interpretation, independent of the Content-Type header.

---

## Architecture

### 1. ContentType Enum

**Location:** `com.mockr.runnr.enums.ContentType`

```java
public enum ContentType {
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    APPLICATION_YAML("application/yaml"),
    APPLICATION_JSON_LD("application/ld+json");

    private final String mediaType;

    ContentType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static ContentType fromMediaType(String mediaType) {
        // Finds enum by media type string
    }
}
```

### 2. Updated Endpoint Entity

**Location:** `com.mockr.runnr.domain.Endpoint`

Add field:

```java
@Column(name = "request_content_type", length = 50)
@Enumerated(EnumType.STRING)
@Builder.Default
private ContentType requestContentType = ContentType.APPLICATION_JSON;
```

**Rationale:**

- Explicitly defines how request bodies should be parsed for this endpoint
- Allows endpoint designer to enforce/override content interpretation
- Default to JSON for backward compatibility

---

## Body Parsing Flow

### Current Flow (Unchanged for requests without Endpoint context)

```
HTTP Request
  ↓
Content-Type Header
  ↓
BodyParserRegistry.selectParser()
  ↓
BodyParser.parse() → Map<String, Object>
  ↓
EvaluationContext (body.* prefixed fields)
```

### New Flow (Endpoint-Aware Parsing)

```
HTTP Request + Endpoint Configuration
  ↓
Use Endpoint.requestContentType (PRIMARY)
Fall back to Content-Type header (SECONDARY)
  ↓
Determine target content type
  ↓
BodyParserRegistry.selectParser(contentType)
  ↓
BodyParser.parse() → Map<String, Object>
  ↓
Flatten with appropriate strategy
  ↓
EvaluationContext (body.* prefixed fields)
```

---

## Implementation Components

### 1. Enhanced BodyParserRegistry

**Location:** `com.mockr.runnr.matcher.body.BodyParserRegistry`

```java
public class BodyParserRegistry {
    // Existing string-based lookup
    public static BodyParser selectParser(String contentType) { }

    // NEW: ContentType enum-based lookup
    public static BodyParser selectParser(ContentType contentType) {
        return selectParser(contentType.getMediaType());
    }

    // NEW: Endpoint-aware selection (primary + fallback)
    public static BodyParser selectParserForEndpoint(
            ContentType endpointRequestContentType,
            String requestContentTypeHeader) {
        // Primary: Use endpoint's configured content type
        if (endpointRequestContentType != null) {
            return selectParser(endpointRequestContentType);
        }
        // Fallback: Use request header
        return selectParser(requestContentTypeHeader);
    }
}
```

### 2. Updated ResponseResolver

**Location:** `com.mockr.runnr.service.resolver.ResponseResolver`

Modify `buildEvaluationContext()` method:

```java
private EvaluationContext buildEvaluationContext(
        MockRequest mockRequest,
        Endpoint endpoint) {  // NEW: Pass endpoint

    Map<String, Object> fields = new HashMap<>();

    // ... existing code for query, headers, etc ...

    // UPDATED: Body parsing using Endpoint's requestContentType
    if (mockRequest.getBody() != null && !mockRequest.getBody().isBlank()) {
        try {
            // Determine which content type to use
            ContentType targetContentType = endpoint.getRequestContentType();
            String contentTypeHeader = mockRequest.getHeaders()
                .getOrDefault("content-type", "");

            // Select parser (endpoint config takes priority)
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                targetContentType,
                contentTypeHeader
            );

            Map<String, Object> bodyFields = parser.parse(mockRequest.getBody());
            bodyFields.forEach((k, v) -> fields.put("body." + k, v));

            logger.debug(
                "Parsed body for endpoint={}, contentType={}, fields={}",
                endpoint.getId(),
                targetContentType,
                bodyFields.size()
            );
        } catch (BodyParser.BodyParseException e) {
            logger.warn("Failed to parse body: {}", e.getMessage());
            fields.put("body.raw", mockRequest.getBody());
        }
    }

    return EvaluationContext.builder().fields(fields).build();
}
```

---

## Parsing Strategy by Content Type

### JSON (application/json)

- **Parser:** JsonBodyParser (existing)
- **Flattening:** Dot notation + bracket arrays
- **Example:**
  ```json
  {"user": {"name": "John", "age": 30}}
  ↓
  {body.user.name: "John", body.user.age: 30}
  ```

### XML (application/xml, text/xml)

- **Parser:** XmlBodyParser (existing)
- **Flattening:** XPath-like notation
- **Example:**
  ```xml
  <user><name>John</name><age>30</age></user>
  ↓
  {body.user.name: "John", body.user.age: 30}
  ```

### Form URL Encoded (application/x-www-form-urlencoded)

- **Parser:** FormBodyParser (existing)
- **Flattening:** Direct key-value pairs
- **Example:**
  ```
  name=John&age=30
  ↓
  {body.name: "John", body.age: 30}
  ```

### Plain Text (text/plain)

- **Parser:** RawBodyParser
- **Storage:** As-is in `body.raw`
- **Example:**
  ```
  Some plain text
  ↓
  {body.raw: "Some plain text"}
  ```

### YAML (application/yaml) - NEW IF NEEDED

- **Parser:** YamlBodyParser (to be created)
- **Flattening:** Similar to JSON
- **Example:**
  ```yaml
  user:
    name: John
    age: 30
  ↓
  {body.user.name: "John", body.user.age: 30}
  ```

---

## Condition Evaluation Integration

### Example Use Case

**Endpoint Configuration:**

```
Path: /api/payment
Method: POST
requestContentType: APPLICATION_XML
```

**Incoming Request:**

```
Content-Type: application/json  (mismatch!)
Body: <payment><amount>100</amount></payment>
```

**Behavior:**

1. Endpoint specifies requestContentType = APPLICATION_XML
2. Body is parsed as XML (ignoring the Content-Type header)
3. Flattened fields: `body.payment.amount = "100"`
4. Conditions evaluated on correct flatmap

**Condition Example:**

```java
Condition: "body.payment.amount EQ 100"
Result: MATCH ✓
```

---

## Call Hierarchy

```
MockApiServiceImpl.handle()
  ↓
CacheService.getOrLoadEndpoints()  // Get endpoint config
  ↓
ResponseResolver.resolve(mockRequest, endpoint)
  ↓
ResponseResolver.buildEvaluationContext(mockRequest, endpoint)  // UPDATED
  ↓
BodyParserRegistry.selectParserForEndpoint(
    endpoint.getRequestContentType(),
    mockRequest.getContentTypeHeader()
)
  ↓
BodyParser.parse()
  ↓
Flatten to Map<String, Object>
  ↓
Add to EvaluationContext with "body." prefix
```

---

## Database Schema Update

### Endpoint Table

Add new column:

```sql
ALTER TABLE endpoint ADD COLUMN request_content_type VARCHAR(50) DEFAULT 'APPLICATION_JSON';
```

---

## Key Design Decisions

| Decision                              | Rationale                                              |
| ------------------------------------- | ------------------------------------------------------ |
| Enum over String                      | Type-safe, prevents invalid values, IDE autocompletion |
| Endpoint takes priority               | API designer intent > client header, prevents spoofing |
| Default to JSON                       | Backward compatible, sensible default                  |
| Factory pattern in BodyParserRegistry | Consistent with existing design, extensible            |
| Pass Endpoint to ResponseResolver     | Minimal changes, context available where needed        |

---

## Extension Points

### Future Enhancements

1. **Per-Response Content Type:** Add requestContentType to Response entity
2. **Custom Flattening Strategies:** Pluggable flatten depth/notation
3. **Schema Validation:** Validate body matches expected structure
4. **Content Negotiation:** Support multiple body formats in conditions
5. **YAML/GraphQL Parsers:** Add as needed

---

## Testing Strategy

### Unit Tests

- `ContentTypeTest.java` - Enum to/from media type conversion
- `BodyParserRegistryTest.java` - NEW parser selection with enum
- `ResponseResolverTest.java` - UPDATED to verify Endpoint requestContentType usage

### Integration Tests

- Test body parsing with Endpoint override
- Test fallback to Content-Type header
- Test all content types end-to-end

---

## Summary

This design:
✅ Gives explicit control over body parsing to endpoint designers  
✅ Maintains backward compatibility (defaults to JSON)  
✅ Reuses existing body parser infrastructure  
✅ Follows SOLID principles (SRP, OCP, DIP)  
✅ Extensible for future content types  
✅ Minimal changes to ResponseResolver

---

## Approval Checklist

- [ ] Architecture makes sense?
- [ ] Design follows project guidelines?
- [ ] Implementation plan clear?
- [ ] Any concerns or suggestions?

**Ready to implement once approved! 🚀**
