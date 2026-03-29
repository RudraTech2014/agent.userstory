backend/
src/main/java/com/<org>/specpro/
SpecProApplication.java
- Spring Boot entry point.

    config/
      AiClientsConfig.java
        - Builds ChatClient instances:
          - draftClient (for spec.md + bundle generation)
          - criticClient (for validation)
        - Sets safe prompt template delimiters (avoid JSON conflicts).
        - Configures model options (temperature, etc).

      JacksonConfig.java
        - Provides ObjectMapper config for stable serialization and SSE payload consistency.

      WebFluxSseConfig.java (optional)
        - SSE defaults (content-type, keep-alive) if needed.

    controller/
      SpecProRunController.java
        - REST endpoints:
          - POST /api/specpro/runs: create a run, start async generation, return runId
          - GET /api/specpro/runs/{runId}: return final result JSON

      SpecProStreamController.java
        - SSE endpoint:
          - GET /api/specpro/runs/{runId}/events: stream status + spec_md_delta + critic + final_bundle

    service/
      SpecAgentProService.java
        - Orchestrates Draft → Critic → Refine loop (max 2 refinements).
        - Calls drafting/critic services and publishes SSE events.
        - Stores run state and final outputs in RunStore.

      AiDraftingService.java
        - Agent A responsibilities:
          - streamSpecMd(): stream-only spec.md markdown
          - buildBundle(): generate bundle JSON from featureIdea + specMd
          - refineBundle(): regenerate bundle using critic feedback

      AiCriticService.java
        - Agent B responsibilities:
          - critic(bundle): returns PASS/FAIL with issues
        - Enforces strict output format (JSON).

    prompts/
      PromptCatalog.java
        - Stores all system/user prompts as constants or templates:
          - A1 draft spec.md (markdown only)
          - A2 build bundle (JSON only)
          - B critic (JSON only)
          - A3 refine (JSON only)

      PromptVariables.java
        - Central list of template variable names + helper to build prompt context:
          - featureIdea, techReference, versions, constraints, specMdText, bundleJson, criticJson

    runtime/
      RunStore.java
        - In-memory run registry (Caffeine recommended):
          - runId -> RunState + SSE sink
        - TTL eviction (15–30 min).
        - Fetch sink for streaming endpoint.

      RunState.java
        - Per-run mutable state:
          - phase/status
          - iteration counter
          - specMdBuffer
          - bundle, critic, timestamps

      RunEventPublisher.java
        - Creates and publishes standard SSE events:
          - status
          - spec_md_delta
          - critic
          - final_bundle
          - error

    tech/
      TechReferenceKey.java
        - Enum of supported tech references (JAVA_ANGULAR, JAVA_REACT, etc.)

      TechReference.java
        - Holds “allowed tech list” + pinned “version catalog” and optional constraints.

      TechReferenceCatalog.java
        - Maps TechReferenceKey -> TechReference
        - Used to inject guardrails into prompts and critic validation.

    model/
      dto/
        SpecRunRequest.java
          - Request payload for creating run:
            featureIdea, techReferenceKey, constraints?, existingStack?

        SpecRunCreated.java
          - Response for create run: runId

        SpecRunResult.java
          - Final response: runId, status(PASS/FAIL), iterations, bundle, critic

        SpecBundle.java
          - Represents SpecKit bundle:
            featureKey + files(Map<String,String>)

        CriticResult.java
          - Critic output: status + issues[]
          - issue fields: severity, file, problem, fix

        SseEvents.java (or separate classes)
          - Payload shapes for each SSE event type:
            StatusEvent, SpecMdDeltaEvent, CriticEvent, FinalBundleEvent, ErrorEvent

    validation/ (recommended)
      BundleSchemaValidator.java
        - Ensures bundle includes required files and non-empty content.

      SpecMdValidator.java
        - Ensures spec.md contains exactly 3 scenarios (Happy/Edge/Error) and Error Path exists.

      ContractValidator.java
        - Ensures contracts/api-spec.json is valid JSON and consistent with plan.md endpoints.

src/test/java/com/<org>/specpro/
fakes/
FakeAiGateway.java
- Deterministic fake for Draft/Critic calls so tests run without real LLM.

    service/
      SpecAgentProServiceTest.java
        - Unit tests for loop behavior:
          PASS first try, FAIL->PASS after refine, FAIL max iterations

    controller/
      SpecProSseIntegrationTest.java
        - WebFlux integration test for SSE event ordering:
          status -> spec_md_delta* -> critic? -> final_bundle -> complete

      SpecProRunControllerTest.java
        - Create run and fetch final result behavior tests.