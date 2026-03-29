## Spec-Agent Pro tasks



### 1) API skeleton (20–30 min)
- POST /api/specpro/runs -> create runId + fire async generation
- GET /api/specpro/runs/{runId}/events -> SSE stream from sink
- GET /api/specpro/runs/{runId} -> return final result object

### 2) SSE event model (10 min)
- Emit events: status, spec_md_delta, critic, final_bundle, error
- UI can render spec_md_delta incrementally

### 3) Implement Agent A1 streaming spec.md (30–45 min)
- Use Spring AI ChatClient streaming API
- For each chunk:
    - append to specMdBuffer
    - emit spec_md_delta SSE
- Emit status events around each phase

### 4) Implement Agent A2 bundle builder (25–35 min)
- Use structured output conversion to map JSON -> SpecBundle DTO
- Ensure spec.md inserted verbatim from buffer
- Emit status: building_bundle

### 5) Implement Critic + loop (30–45 min)
- Call critic prompt -> CriticResult DTO
- If FAIL:
    - emit critic SSE
    - call refine prompt
    - re-run critic
- Max 2 refinement iterations
- Emit final_bundle SSE + mark run done

### 6) UI streaming (45–60 min)
- One page:
    - textarea featureIdea
    - dropdown techReferenceKey
    - Generate button -> POST /runs
    - Subscribe to /events (EventSource)
    - Live markdown rendering for spec.md
    - When final_bundle arrives, show tabbed file viewer

### 7) Testing (45–60 min)
Unit tests (no real LLM):
- Stub AI gateway with deterministic outputs:
    - PASS-first-try path
    - FAIL then PASS after refine
    - FAIL twice (max iterations)
- Validate:
    - spec.md has 3 scenarios
    - bundle contains required files
    - critic issues flow into refine prompt call

WebFlux integration tests:
- Use WebTestClient to:
    - create run
    - subscribe SSE and assert event ordering includes:
      status -> spec_md_delta* -> status -> critic? -> final_bundle -> completion
- Use StepVerifier for reactive assertions