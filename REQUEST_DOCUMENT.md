# SpecPro Request Document

This document provides sample API requests for creating and streaming a SpecPro run.

## 1) Create a Run

### Endpoint
`POST /api/specpro/runs`

### Headers
- `Content-Type: application/json`

### Sample Request Body
```json
{
  "featureIdea": "Add team-based project templates",
  "techReferenceKey": "JAVA_ANGULAR"
}
```

### cURL Example
```bash
curl -X POST "http://localhost:8080/api/specpro/runs" \
  -H "Content-Type: application/json" \
  -d '{
    "featureIdea": "Add team-based project templates",
    "techReferenceKey": "JAVA_ANGULAR"
  }'
```

### Sample Success Response (200)
```json
{
  "runId": "9f36cfff-6fb5-4ab7-9fbb-4f091ea4f5b0"
}
```

### Sample Validation Error (400)
```json
{
  "error": "VALIDATION_ERROR",
  "message": "featureIdea featureIdea is required"
}
```

### Sample Invalid Tech Key Error (400)
```json
{
  "error": "INVALID_TECH_REFERENCE_KEY",
  "message": "techReferenceKey must be a valid enum value"
}
```

---

## 2) Stream Run Events

### Endpoint
`GET /api/specpro/runs/{runId}/events`

### cURL Example
```bash
curl -N "http://localhost:8080/api/specpro/runs/9f36cfff-6fb5-4ab7-9fbb-4f091ea4f5b0/events"
```

### Example SSE Event Sequence
```text
event: status
data: {"phase":"DRAFTING","iteration":0}

event: spec_md_delta
data: # Title: Add team-based project templates

...

event: critic
data: {"status":"PASS","issues":[]}

event: final_bundle
data: {"featureKey":"add-team-based-project-templates","files":{...}}

event: done
data: {"runId":"9f36cfff-6fb5-4ab7-9fbb-4f091ea4f5b0","phase":"DONE","iteration":1}
```
