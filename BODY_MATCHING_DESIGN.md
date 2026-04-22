# Body Matching Design - Simplified Multi-Parser Approach

## Overview

Add support for matching incoming request body fields based on conditions, with content-type detection for multiple formats (JSON, Form, XML/SOAP).

---

## 1. Current State vs Desired State

### Current

- Only supports: `header.*`, `query.*`, `path.*`
- EvaluationContext has: headers, queryParameters, pathVariables
- Body is NOT evaluated at all

### Desired

- Add support for: `body.*` prefixed conditions
- Multi-format parsing (real-world APIs):
  - `application/json` → Parse as JSON object, flatten nested fields
  - `application/x-www-form-urlencoded` → Parse form fields (OAuth2 tokens)
  - `application/xml` / `application/soap+xml` → Parse XML with attributes (Banks, SOAP APIs)
  - Other types → Store as raw text
- Unified field resolution: All fields in single map with full path keys

---

## 2. Simplified Architecture: Unified Field Map

### 2.1 Single EvaluationContext with Unified Field Map

Instead of separate maps for headers/query/path/body, use **ONE unified map** with full-path keys:

```java
public record EvaluationContext(
    Map<String, Object> fields  // NEW: Single unified map
) {}

// Example contents:
fields = {
    "header.content-type" → "application/json",
    "header.x-api-key" → "secret-key",
    "query.page" → "1",
    "query.limit" → "10",
    "path.userId" → "123",
    "body.username" → "karthi",
    "body.password" → "Welcome@01",
    "body.user.email" → "user@example.com",
    "body.items[0].name" → "Product A",
    "body.soap:Body.User.Name" → "John"  // XML with namespaces
}
```

**Benefits:**

- Single source of truth for all fields
- Field resolution is trivial: `fields.get(fieldPath)`
- Supports headers, query, path, body in same structure
- Easy to debug (print one map)

---

### 2.2 Multi-Format Body Parser (Simple Strategy Pattern)

```java
public interface BodyParser {
    /**
     * Parse body into flat map of fields.
     * @param body Raw body string
     * @return Map of flattened fields (no prefix, will be added by caller)
     */
    Map<String, Object> parse(String body) throws BodyParseException;
}
```

**Implementations:**

```java
// JSON: Recursively flatten nested objects and arrays
public class JsonBodyParser implements BodyParser {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_DEPTH = 10;

    @Override
    public Map<String, Object> parse(String body) {
        Map<String, Object> json = mapper.readValue(body, Map.class);
        return flatten(json, "", 0);
    }

    private Map<String, Object> flatten(Object obj, String prefix, int depth) {
        if (depth > MAX_DEPTH) return new HashMap<>();

        Map<String, Object> result = new HashMap<>();
        if (obj instanceof Map) {
            ((Map<?, ?>) obj).forEach((k, v) ->
                result.putAll(flatten(v,
                    prefix.isEmpty() ? k.toString() : prefix + "." + k,
                    depth + 1))
            );
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                result.putAll(flatten(list.get(i), prefix + "[" + i + "]", depth + 1));
            }
        } else {
            result.put(prefix, obj);  // Primitive
        }
        return result;
    }
}

// Form-URLEncoded: Parse form fields (OAuth2 tokens, etc)
public class FormBodyParser implements BodyParser {
    @Override
    public Map<String, Object> parse(String body) {
        Map<String, Object> result = new HashMap<>();
        if (body == null || body.isBlank()) return result;

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ?
                URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }
}

// XML: Parse with namespace support, store attributes with @prefix
public class XmlBodyParser implements BodyParser {
    private static final DocumentBuilder docBuilder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();

    @Override
    public Map<String, Object> parse(String body) {
        Document doc = docBuilder.parse(new ByteArrayInputStream(body.getBytes()));
        return flattenXml(doc.getDocumentElement(), "");
    }

    private Map<String, Object> flattenXml(Element elem, String prefix) {
        Map<String, Object> result = new HashMap<>();
        String nodeName = elem.getTagName();
        String currentPath = prefix.isEmpty() ? nodeName : prefix + "." + nodeName;

        // Add attributes with @ prefix: soap:Body@version="1.0"
        NamedNodeMap attrs = elem.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            result.put(currentPath + "@" + attr.getName(), attr.getValue());
        }

        // Recursively flatten child elements
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                result.putAll(flattenXml((Element) child, currentPath));
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getNodeValue().trim();
                if (!text.isEmpty()) {
                    result.put(currentPath, text);
                }
            }
        }

        return result;
    }
}

// Fallback: Store raw text
public class RawBodyParser implements BodyParser {
    @Override
    public Map<String, Object> parse(String body) {
        Map<String, Object> result = new HashMap<>();
        result.put("raw", body);
        return result;
    }
}
```

---

### 2.3 Simplified Parser Selection

```java
private static Map<String, BodyParser> PARSERS = Map.of(
    "application/json", new JsonBodyParser(),
    "application/x-www-form-urlencoded", new FormBodyParser(),
    "application/xml", new XmlBodyParser(),
    "application/soap+xml", new XmlBodyParser()
);

private BodyParser selectParser(String contentType) {
    if (contentType == null) return new RawBodyParser();

    String type = contentType.split(";")[0].toLowerCase().trim();
    return PARSERS.getOrDefault(type, new RawBodyParser());
}
```

---

### 2.4 Condition Resolution (Trivial)

```java
private Object resolveFieldValue(String fieldPath, EvaluationContext context) {
    return context.fields().get(fieldPath);  // ONE LINE!
}
```

---

## 3. MockRequest Enhancement

**Current:**

```java
public class MockRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> queryParameters;
    // NO BODY
}
```

**Updated:**

```java
public class MockRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> queryParameters;
    private String body;  // NEW: Raw request body
    private String contentType;  // NEW: Content-Type header
}
```

---

## 3. Simplified ResponseResolver Flow

```java
public ResponseEntity<?> resolve(MockRequest mockRequest, java.util.UUID endpointId) {
    try {
        // 1. Load endpoint from cache
        Optional<Endpoint> endpointOpt = cacheService.getOrLoadEndpoint(endpointId);
        if (endpointOpt.isEmpty()) return buildNotFoundResponse();

        Endpoint endpoint = endpointOpt.get();

        // 2. Build unified field map
        Map<String, Object> fields = buildFieldsMap(mockRequest);
        EvaluationContext context = new EvaluationContext(fields);

        // 3. Evaluate responses
        List<ResponseCandidate> candidates = new ArrayList<>();
        for (Response response : endpoint.getResponses()) {
            if (conditionEvaluator.evaluate(response.getConditions(), context)) {
                // Score by specificity
                ResponseCandidate candidate = buildCandidate(response,
                    response.getConditions().size());
                candidates.add(candidate);
            }
        }

        // 4. Select best and return
        return candidates.isEmpty() ?
            buildNotFoundResponse() :
            buildResponse(selectionEngine.findBestMatch(candidates));

    } catch (Exception e) {
        logger.error("Error resolving response", e);
        throw new ResponseResolutionException("Failed to resolve response", e);
    }
}

private Map<String, Object> buildFieldsMap(MockRequest mockRequest) {
    Map<String, Object> fields = new HashMap<>();

    // 1. Add headers with prefix
    if (mockRequest.getHeaders() != null) {
        mockRequest.getHeaders().forEach((k, v) ->
            fields.put("header." + k.toLowerCase(), v)
        );
    }

    // 2. Add query parameters with prefix
    if (mockRequest.getQueryParameters() != null) {
        mockRequest.getQueryParameters().forEach((k, v) ->
            fields.put("query." + k.toLowerCase(), v)
        );
    }

    // 3. Add path variables with prefix
    // (extracted from request or already in MockRequest)

    // 4. Parse and add body fields with prefix
    if (mockRequest.getBody() != null && !mockRequest.getBody().isBlank()) {
        try {
            String contentType = mockRequest.getHeaders().get("content-type");
            BodyParser parser = selectParser(contentType);
            Map<String, Object> bodyFields = parser.parse(mockRequest.getBody());
            bodyFields.forEach((k, v) -> fields.put("body." + k, v));
            logger.debug("Parsed {} body fields from {}", bodyFields.size(), contentType);
        } catch (Exception e) {
            logger.warn("Failed to parse body as structured format, storing as raw", e);
            fields.put("body.raw", mockRequest.getBody());
        }
    }

    return fields;
}
```

---

## 4. Example Scenarios

### Scenario 1: JSON POST Authentication

**Incoming Request:**

```
POST /v1/authenticate
Content-Type: application/json

{
  "username": "karthi",
  "password": "Welcome@01",
  "rememberMe": true
}
```

**Unified Fields Map:**

```
fields = {
    "header.content-type" → "application/json",
    "body.username" → "karthi",
    "body.password" → "Welcome@01",
    "body.rememberme" → true
}
```

**Stored Conditions & Matching:**

```
body.username Equals karthi ✓
body.password Equals Welcome@01 ✓
body.rememberme Equals true ✓
→ Response matched!
```

---

### Scenario 2: OAuth2 Token Request (Form-URLEncoded)

**Incoming Request:**

```
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&username=admin&password=Pass123&scope=read
```

**Unified Fields Map:**

```
fields = {
    "header.content-type" → "application/x-www-form-urlencoded",
    "body.grant_type" → "password",
    "body.username" → "admin",
    "body.password" → "Pass123",
    "body.scope" → "read"
}
```

**Stored Conditions & Matching:**

```
body.grant_type Equals password ✓
body.username Equals admin ✓
→ Response matched!
```

---

### Scenario 3: SOAP/XML Bank API

**Incoming Request:**

```
POST /soap/bank
Content-Type: application/soap+xml

<?xml version="1.0"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <TransferRequest version="1.0">
      <From>ACC123</From>
      <To>ACC456</To>
      <Amount>1000</Amount>
    </TransferRequest>
  </soap:Body>
</soap:Envelope>
```

**Unified Fields Map:**

```
fields = {
    "header.content-type" → "application/soap+xml",
    "body.soap:Envelope" → undefined,
    "body.soap:Envelope.soap:Body" → undefined,
    "body.soap:Envelope.soap:Body.TransferRequest@version" → "1.0",
    "body.soap:Envelope.soap:Body.TransferRequest.From" → "ACC123",
    "body.soap:Envelope.soap:Body.TransferRequest.To" → "ACC456",
    "body.soap:Envelope.soap:Body.TransferRequest.Amount" → "1000"
}
```

**Stored Conditions & Matching:**

```
body.soap:Envelope.soap:Body.TransferRequest@version Equals 1.0 ✓
body.soap:Envelope.soap:Body.TransferRequest.From Equals ACC123 ✓
→ Response matched!
```

---

### Scenario 4: JSON with Nested Arrays

**Incoming Request:**

```
POST /api/orders
Content-Type: application/json

{
  "customerId": 100,
  "items": [
    {"id": 1, "quantity": 2, "price": 50},
    {"id": 2, "quantity": 1, "price": 100}
  ]
}
```

**Unified Fields Map:**

```
fields = {
    "body.customerId" → 100,
    "body.items[0].id" → 1,
    "body.items[0].quantity" → 2,
    "body.items[0].price" → 50,
    "body.items[1].id" → 2,
    "body.items[1].quantity" → 1,
    "body.items[1].price" → 100
}
```

**Stored Conditions & Matching:**

```
body.customerId Equals 100 ✓
body.items[0].quantity Equals 2 ✓
→ Response matched!
```

---

## 5. Implementation Checklist (Simplified)

**Core Components:**

- [ ] Update `EvaluationContext` record with single `fields` map
- [ ] Create `BodyParser` interface (simple, no contentType param)
- [ ] Implement `JsonBodyParser` with recursive flattening
- [ ] Implement `FormBodyParser` for URL-encoded bodies
- [ ] Implement `XmlBodyParser` for SOAP/XML with attribute support
- [ ] Implement `RawBodyParser` fallback
- [ ] Update `MockRequest` with body field
- [ ] Update `ResponseResolver.buildFieldsMap()` method
- [ ] Update `ConditionEvaluator.resolveFieldValue()` to use single fields map

**Testing:**

- [ ] Unit tests: JsonBodyParser with nested/array structures
- [ ] Unit tests: FormBodyParser (URL decoding)
- [ ] Unit tests: XmlBodyParser with namespaces and attributes
- [ ] Integration tests: End-to-end with all three formats
- [ ] Edge cases: Malformed JSON/XML, empty bodies, nulls

**Integration:**

- [ ] Update controller to capture request body
- [ ] Update documentation with examples

---

## 6. Edge Cases & Handling

| Scenario             | Handling                                                        |
| -------------------- | --------------------------------------------------------------- |
| No body in request   | fields map has no body.\* keys                                  |
| Malformed JSON       | Log warning, store as body.raw                                  |
| Malformed XML        | Log warning, store as body.raw                                  |
| Missing Content-Type | Default to RawBodyParser                                        |
| Empty body           | Skip body parsing                                               |
| Nested JSON          | Flatten with dots: `body.user.address.city`                     |
| JSON arrays          | Flatten with index: `body.items[0].name`, `body.items[1].price` |
| XML attributes       | Store with @: `body.Element@attr="value"`                       |
| XML namespaces       | Preserve in key: `body.soap:Body.User.Name`                     |
| Very deep nesting    | Stop at MAX_DEPTH=10 (prevent DoS)                              |
| Null values          | Include as null in map                                          |
| Boolean/Number       | Preserve types in map                                           |

---

## 8. JSON Flattening Strategy with Full Array Support

### 8.1 Object Flattening (Dot Notation)

**Example Input:**

```json
{
  "user": {
    "name": "John",
    "email": "john@example.com"
  },
  "role": "admin"
}
```

**Flattened Output (Dot Notation):**

```
user.name = "John"
user.email = "john@example.com"
role = "admin"
```

**Condition Usage:**

```
body.user.name Equals John ✓
body.role Equals admin ✓
```

### 8.2 Array Support with Bracket Indexing

**Example 1: Array of Objects**

```json
{
  "items": [
    { "id": 1, "name": "Product A", "price": 100 },
    { "id": 2, "name": "Product B", "price": 200 },
    { "id": 10, "name": "Product K", "price": 1000 }
  ]
}
```

**Flattened Output:**

```
items[0].id = 1
items[0].name = "Product A"
items[0].price = 100
items[1].id = 2
items[1].name = "Product B"
items[1].price = 200
items[2].id = 10
items[2].name = "Product K"
items[2].price = 1000
```

**Condition Usage:**

```
body.items[0].name Equals Product A ✓
body.items[2].id Equals 10 ✓
body.items[2].price Equals 1000 ✓
```

**Example 2: Array of Primitives**

```json
{
  "tags": ["electronics", "gadgets", "premium"],
  "scores": [95, 87, 92, 88]
}
```

**Flattened Output:**

```
tags[0] = "electronics"
tags[1] = "gadgets"
tags[2] = "premium"
scores[0] = 95
scores[1] = 87
scores[2] = 92
scores[3] = 88
```

**Condition Usage:**

```
body.tags[0] Equals electronics ✓
body.scores[2] Equals 92 ✓
```

**Example 3: Nested Arrays (Multi-level)**

```json
{
  "matrix": [
    [1, 2, 3],
    [4, 5, 6],
    [7, 8, 9]
  ]
}
```

**Flattened Output:**

```
matrix[0][0] = 1
matrix[0][1] = 2
matrix[0][2] = 3
matrix[1][0] = 4
matrix[1][1] = 5
matrix[1][2] = 6
matrix[2][0] = 7
matrix[2][1] = 8
matrix[2][2] = 9
```

**Condition Usage:**

```
body.matrix[1][1] Equals 5 ✓
body.matrix[2][2] Equals 9 ✓
```

**Example 4: Complex Mixed Structure**

```json
{
  "users": [
    {
      "id": 1,
      "name": "John",
      "addresses": [
        { "street": "123 Main St", "city": "NYC" },
        { "street": "456 Oak Ave", "city": "LA" }
      ]
    },
    {
      "id": 2,
      "name": "Jane",
      "addresses": [{ "street": "789 Elm St", "city": "Chicago" }]
    }
  ]
}
```

**Flattened Output:**

```
users[0].id = 1
users[0].name = "John"
users[0].addresses[0].street = "123 Main St"
users[0].addresses[0].city = "NYC"
users[0].addresses[1].street = "456 Oak Ave"
users[0].addresses[1].city = "LA"
users[1].id = 2
users[1].name = "Jane"
users[1].addresses[0].street = "789 Elm St"
users[1].addresses[0].city = "Chicago"
```

**Condition Usage:**

```
body.users[0].name Equals John ✓
body.users[0].addresses[1].city Equals LA ✓
body.users[1].addresses[0].city Equals Chicago ✓
```

### 8.3 Flattening Algorithm

**Pseudocode:**

```java
Map<String, Object> flatten(Object obj, String prefix="", int depth=0):
    if (depth > MAX_FLATTEN_DEPTH) {
        LOG.warn("Max depth exceeded at {}", prefix)
        return empty map
    }

    result = new HashMap<>

    if obj is Map:
        for each (key, value) in obj.entries():
            newKey = prefix.isEmpty() ? key : prefix + "." + key
            result.putAll(flatten(value, newKey, depth + 1))

    else if obj is List:
        list = (List) obj
        for index from 0 to list.size()-1:
            newKey = prefix + "[" + index + "]"
            result.putAll(flatten(list.get(index), newKey, depth + 1))

    else:
        // Primitive value (String, Number, Boolean, null)
        result.put(prefix, obj)

    return result
```

**Key Points:**

- For Map: use dot notation separator (`.`)
- For List: use bracket notation with index (`[index]`)
- Recursively flatten all nested structures
- Stop at MAX_FLATTEN_DEPTH to prevent DoS
- Primitives are terminal values (String, Number, Boolean, null)

---

## 9. Performance Optimization: Lazy Body Flattening

### 9.1 Why Lazy Flattening Matters

**Problem with Always-Flatten:**

- Every request parses & flattens body, even if no body conditions exist
- Large JSON bodies (1-100MB) get completely parsed and flattened
- Wastes CPU on JSON parsing + recursion
- Wastes memory on temporary objects

**Solution: Lazy Flattening**

- Check if endpoint has ANY body conditions FIRST (O(n\*m) scan, but very fast)
- Only parse/flatten body if needed
- Skip parsing for endpoints that only use headers/query/path conditions

### 9.2 Performance Impact

**Typical Web API Scenario:**

- 70% of endpoints: header + query conditions ONLY (no body matching)
- 30% of endpoints: Include body conditions

**Results with Lazy Flattening:**
| Scenario | Old Approach | New Approach (Lazy) | Savings |
|----------|------------|---------------------|---------|
| No body conditions | Parse 1MB JSON | Skip parsing | **100% less CPU** |
| 50 body conditions | Parse 1MB JSON | Parse 1MB JSON | 0% (no optimization) |
| Simple auth (no body) | Parse 100B JSON | Skip parsing | **Minimal but consistent** |

**Real-World Impact:**

- Endpoints with NO body matching: ~50% faster (skip JSON parsing)
- Endpoints WITH body matching: Same speed (full parsing needed)
- Overall throughput: +20-40% (estimate, based on 70% no-body endpoints)

### 9.3 Implementation: Body Condition Detection

```java
/**
 * Check if ANY response in endpoint has body conditions.
 * Runs once per request, very fast.
 *
 * Time: O(r * c) where r=responses, c=conditions per response
 * Typical: 3 responses * 2 conditions = 6 comparisons
 * vs JSON parse time: 1-50ms depending on body size
 *
 * Decision: If ANY body condition found → parse body
 * Otherwise → skip parsing
 */
private boolean hasBodyConditions(List<Response> responses) {
    return responses.stream()
            .flatMap(response -> response.getConditions().stream())
            .anyMatch(condition -> condition.getFieldPath()
                    .toLowerCase()
                    .startsWith("body."));
}
```

### 9.4 Cache: Condition Check at Startup

**Further Optimization:** Cache result at startup

```java
// In CacheService during endpoint loading
private Map<UUID, Boolean> endpointBodyConditionCache = new ConcurrentHashMap<>();

private void cacheEndpoint(Endpoint endpoint) {
    UUID endpointId = endpoint.getId();

    // Pre-compute: Does this endpoint need body parsing?
    boolean needsBodyParsing = hasBodyConditions(endpoint.getResponses());
    endpointBodyConditionCache.put(endpointId, needsBodyParsing);

    // During request resolution:
    boolean shouldParse = endpointBodyConditionCache.get(endpointId);
}
```

**Result:** O(1) lookup instead of O(r\*c) check per request!

### 9.5 When Lazy Flattening Helps MOST

✅ **Best Case (Big Wins):**

- Large request bodies (>10KB JSON)
- Endpoints with header/query/path matching only
- High-volume REST APIs

✅ **Good Case:**

- Medium bodies (1-10KB)
- Mixed condition types
- Reduces memory churn

❌ **No Benefit:**

- Endpoints already have body conditions
- Small bodies (<1KB)
- Infrequent requests

---

## 9a. Complete Request Flow Diagram with Lazy Flattening

````
┌─────────────────────────────────────────────────────────────────┐
│ Incoming HTTP Request                                           │
│ POST /v1/authenticate                                           │
│ Content-Type: application/json                                  │
│ Body: {"username": "karthi", "password": "Welcome@01"}          │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│ PathMatcher (Issue 6)                                           │
│ Matches endpoint by path + method                               │
│ ✓ Returns: endpointId = UUID                                    │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│ ResponseResolver.resolve()                                      │
│ - Load endpoint from cache (Issue 5)                            │
│ - Check if endpoint has body conditions (LAZY CHECK)            │
│ - Convert request to EvaluationContext                          │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │ Has body        │
                   │ conditions?     │
                   └────────┬────────┘
                 ┌──────────┴──────────┐
                 │                     │
              YES ▼                      ▼ NO
┌──────────────────────────┐  ┌──────────────────────────┐
│ toEvaluationContext()    │  │ toEvaluationContext()    │
│ WITH body parsing        │  │ SKIP body parsing        │
│ 1. Extract headers       │  │ 1. Extract headers       │
│ 2. Extract query params  │  │ 2. Extract query params  │
│ 3. Extract path vars     │  │ 3. Extract path vars     │
│ 4. Parse & flatten body: │  │ 4. bodyFields = EMPTY    │
│    • Get Content-Type    │  │ 5. Return: Context       │
│    • Select parser       │  │    (No body processing)  │
│    • Recursive flatten   │  │                          │
│    → bodyFields = {...}  │  └──────────────────────────┘
│ 5. Return: Context       │            │
│    (Full processing)     │            │
└──────────────────────────┘            │
             │                          │
             └──────────────┬───────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│ ConditionEvaluator.evaluate()                                   │
│ For each Response in Endpoint:                                  │
│   For each Condition:                                           │
│     1. Parse field path: \"body.username\"                        │
│     2. Extract prefix: \"body\"                                   │
│     3. Extract field: \"username\"                                │
│     4. Look up in bodyFields: \"username\" → \"karthi\" (or NULL) │
│     5. Evaluate operator (Equals)                               │
│                                                                 │
│ ✓ All conditions match → Add to candidates                      │
└─────────────────────────────────────────────────────────────────┘\n                            │\n                            ▼\n┌─────────────────────────────────────────────────────────────────┐\n│ ResponseSelectionEngine.findBestMatch()                         │\n│ - Score candidates by condition count (specificity)             │\n│ - Apply tie-breaking: createdAt DESC, responseId DESC           │\n│ ✓ Return: Best matching response                                │\n└─────────────────────────────────────────────────────────────────┘\n                            │\n                            ▼\n┌─────────────────────────────────────────────────────────────────┐\n│ ResponseBuilder.build()                                         │\n│ - Set status code, body, content type                           │\n│ - Add custom headers                                            │\n│ ✓ Return: ResponseEntity                                        │\n└─────────────────────────────────────────────────────────────────┘\n                            │\n                            ▼\n┌─────────────────────────────────────────────────────────────────┐\n│ HTTP Response (200 OK)                                          │\n│ Headers: [Set-Cookie: trace-id=...]                             │\n│ Body: {\"token\": \"eyHjkqesekm...\"}                              │\n└─────────────────────────────────────────────────────────────────┘\n```"

---

## 9. Implementation Details - Flattening Engine

### 9.1 JsonBodyParser with Flattening

```java
public class JsonBodyParser implements BodyParser {
    private static final int MAX_FLATTEN_DEPTH = 10;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> parse(String body, String contentType) {
        Map<String, Object> parsed = mapper.readValue(body, Map.class);
        return flatten(parsed, "", 0);
    }

    private Map<String, Object> flatten(Object obj, String prefix, int depth) {
        if (depth > MAX_FLATTEN_DEPTH) {
            logger.warn("Max flattening depth {} exceeded at prefix: {}", MAX_FLATTEN_DEPTH, prefix);
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();

        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String newKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                result.putAll(flatten(entry.getValue(), newKey, depth + 1));
            }
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                String newKey = prefix + "[" + i + "]";
                result.putAll(flatten(list.get(i), newKey, depth + 1));
            }
        } else {
            // Primitive: String, Number, Boolean, null
            result.put(prefix, obj);
        }

        return result;
    }
}
````

### 9.2 Field Path Validation

```java
public class FieldPathValidator {
    // Validates paths like: user.name, items[0].price, matrix[1][2]

    private static final Pattern VALID_PATH = Pattern.compile(
        "^[a-zA-Z_][a-zA-Z0-9_.\\[\\]]*$"
    );

    public static boolean isValidFieldPath(String path) {
        if (path == null || path.isBlank()) return false;

        // Check overall format
        if (!VALID_PATH.matcher(path).matches()) return false;

        // Check brackets are balanced
        return bracketsBalanced(path);
    }

    private static boolean bracketsBalanced(String path) {
        int open = 0;
        for (char c : path.toCharArray()) {
            if (c == '[') open++;
            else if (c == ']') {
                open--;
                if (open < 0) return false;
            }
        }
        return open == 0;
    }
}
```

### 9.3 Test Cases for Arrays

**Valid Condition Paths:**

- `body.items[0].id` → Should match first item's id
- `body.tags[2]` → Should match third tag (primitive)
- `body.matrix[0][1]` → Should match nested array element at row 0, col 1
- `body.users[0].addresses[1].city` → Deeply nested structure
- `body.data[10].value` → Index 10 exists after flattening

**Invalid/Non-matching Scenarios:**

- `body.users[99].name` → Index out of range (not in flattened map) → Condition fails
- `body.items[-1].id` → Negative index (never created during flattening) → Condition fails
- `body.items[abc].id` → Non-numeric index → Never created → Condition fails
- `body.nonexistent[0]` → Array doesn't exist → Condition fails

**Test Assertions:**

```java
// Given JSON: {"items": [{"id": 100}]}
// Flattened: {"items[0].id": 100}

assert bodyFields.containsKey("items[0].id");
assert bodyFields.get("items[0].id").equals(100);
assert !bodyFields.containsKey("items[1].id"); // Out of range
assert !bodyFields.containsKey("items[-1].id"); // Negative index
```

---

## 10. Updated Implementation Checklist

- [x] Design array support with bracket indexing
- [x] Define flattening algorithm for mixed structures
- [ ] Update `EvaluationContext` record with `bodyFields`
- [ ] Create `BodyParser` interface
- [ ] Implement `JsonBodyParser` with nested field flattening + arrays
- [ ] Implement `FormBodyParser`
- [ ] Create `BodyParserFactory`
- [ ] Create `FieldPathValidator` for path validation
- [ ] Update `MockRequest` with body & contentType fields
- [ ] Update `ResponseResolver.toEvaluationContext()`
- [ ] Update `ConditionEvaluator.resolveFieldValue()` to handle `body.*` and arrays
- [ ] Add unit tests for flattening algorithm
- [ ] Add unit tests for each parser
- [ ] Add integration tests with complex nested/array structures
- [ ] Update controller to capture request body
- [ ] Update documentation

---

## 11. Future Enhancements

- [ ] Support XPath for XML bodies
- [ ] Support JSONPath for complex JSON queries: `$.items[?(@.price > 100)]`
- [ ] Support regex matching on body fields
- [ ] Support custom body parsers (plugins)
- [ ] Support wildcard matching: `body.items[*].status` (match all items)
- [ ] Support range queries: `body.items[0:5]` (items 0-5)
- [ ] Support array length matching: `body.items.length Equals 3`
