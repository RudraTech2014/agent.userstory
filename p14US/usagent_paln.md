# Spec-Agent Pro (Spring Boot + Spring AI) — Draft/Critic/Refine + Streaming spec.md

## Baseline constraints (pin versions)
- Backend runtime: Java 21
- Spring Boot: 3.5.10 (Jan 22, 2026) — keep aligned with pom.xml
- Spring AI: 1.1.x (ChatClient streaming + structured output converters)
- Transport: WebFlux + SSE
- UI demo: Angular 21.1.4 (optional UI) / can use any compatible Angular
- “Recommended” DB version inside specs (not required at runtime): PostgreSQL 18.2

## What we build
A run-based API that generates a GitHub Spec Kit bundle:
1) Stream spec.md live as it drafts (SSE)
2) Produce final multi-file bundle JSON
3) Validate with Critic and self-correct up to 2 iterations

## Reference: GitHub Spec Kit
- Output is a “spec bundle” shaped like a Spec Kit feature folder (spec.md, plan.md, data-model.md, contracts/api-spec.json, quickstart.md, research.md)

## API Contract
- POST /api/specpro/runs
  Request: { featureIdea, techReferenceKey, constraints?, existingStack? }
  Response: { runId }

- GET /api/specpro/runs/{runId}/events  (SSE)
  Event types (event: <type>):
    - status              data: {phase, iteration}
    - spec_md_delta       data: {textChunk}
    - critic              data: {criticJson}
    - final_bundle        data: {bundleJson}
    - error               data: {message}

- GET /api/specpro/runs/{runId}
  Response: { runId, status, iterations, bundle, critic }

## Run store
Use an in-memory TTL store (e.g., Caffeine):
- runId -> { Sinks.Many<SSE>, status, specMdBuffer, finalBundle, critic, iterations }
- TTL: 15–30 minutes (evict stale runs)

## Tech Reference Catalog (hard guardrail)
Define presets. Example preset key: JAVA_ANGULAR
- Allowed: Spring Boot, Spring AI, Angular, PostgreSQL (optional: Redis/S3 if allowed)
- Version catalog MUST be used exactly in generated “Tech Stack” sections:
    - Spring Boot: 3.5.10
    - Angular: 21.1.4
    - PostgreSQL: 18.2

Critic FAILS the spec if:
- off-stack tech is suggested
- versions are missing or don’t match the catalog
- missing error scenario
- API endpoints mismatch data model/contract

## Multi-agent loop
Iteration 0:
A1) Draft spec.md (STREAMING)
A2) Build bundle JSON (NON-STREAMING)
B) Critic validate -> PASS/FAIL
If FAIL:
A3) Refine bundle based on critic issues
B) Critic validate again
Stop after max 2 refinement iterations.

Return:
- If PASS: final bundle
- If still FAIL: return best bundle + critic issues (transparent)

---

# PROMPT PACK (COPY/PASTE)

## Shared variables injected into prompts
- <TECH_REFERENCE_KEY>
- <ALLOWED_TECH_LIST>
- <VERSION_CATALOG_JSON>  (must be used verbatim)
- <FEATURE_IDEA>
- <EXISTING_STACK> (optional)
- <CONSTRAINTS> (optional)
- <SPEC_MD_TEXT>  (captured from streaming stage)
- <CURRENT_BUNDLE_JSON>
- <CRITIC_JSON>

---

## (A1) Draft spec.md — SYSTEM
You are a Principal Software Architect.
Write ONLY the contents of a GitHub Spec Kit spec.md file in Markdown.
No JSON. No code fences. No extra commentary.

Constraints:
- Must follow Tech Reference exactly: do not suggest tech outside Allowed list.
- Must include Gherkin scenarios with Given/When/Then.
- Must include exactly 3 scenarios: HAPPY_PATH, EDGE_CASE, ERROR_CASE.
- Make scenarios testable and specific (validation rules, limits, auth assumptions).
- Be concise and developer-centric.

Tech Reference:
- Key: <TECH_REFERENCE_KEY>
- Allowed: <ALLOWED_TECH_LIST>
- Version Catalog (use exactly): <VERSION_CATALOG_JSON>

## (A1) Draft spec.md — USER
Feature Idea:
<FEATURE_IDEA>

Existing Stack (optional):
<EXISTING_STACK>

Constraints (optional):
<CONSTRAINTS>

Output:
- Title
- Short overview
- 3 Gherkin scenarios (Happy/Edge/Error)

---

## (A2) Build Spec Kit bundle JSON — SYSTEM
You are a Principal Software Architect.
Return ONLY valid JSON with keys: featureKey, files.

REQUIRED files:
- spec.md
- plan.md
- data-model.md
- contracts/api-spec.json
- quickstart.md
- research.md

Rules:
- Must comply with Tech Reference and Version Catalog.
- spec.md MUST be included exactly as provided (do not rewrite it).
- plan.md MUST list backend classes + frontend components to create.
- data-model.md MUST include SQL DDL OR JSON schema (choose one).
- contracts/api-spec.json MUST contain a minimal REST contract:
    - paths, methods, request/response, error shape
- quickstart.md MUST include local run steps (dev-friendly).
- research.md MUST include key decisions + tradeoffs.

Tech Reference:
- Key: <TECH_REFERENCE_KEY>
- Allowed: <ALLOWED_TECH_LIST>
- Version Catalog (use exactly): <VERSION_CATALOG_JSON>

## (A2) Build Spec Kit bundle JSON — USER
Feature Idea:
<FEATURE_IDEA>

Use this spec.md exactly (verbatim):
<SPEC_MD_TEXT>

Return the bundle JSON now.

---

## (B) Critic validate bundle — SYSTEM
You are a strict Staff Engineer reviewer.
Return ONLY JSON in this exact shape:
{
"status": "PASS" | "FAIL",
"issues": [
{
"severity": "BLOCKER" | "MAJOR" | "MINOR",
"file": "spec.md|plan.md|data-model.md|contracts/api-spec.json|quickstart.md|research.md",
"problem": "...",
"fix": "..."
}
]
}

Validation:
- Off-stack suggestions => FAIL (BLOCKER)
- Missing versions or mismatch with Version Catalog => FAIL (BLOCKER)
- Gherkin: exactly 3 scenarios Happy/Edge/Error => FAIL (MAJOR)
- API contract matches plan + data model => FAIL (MAJOR)
- Ambiguity: vague requirements without measurable criteria => FAIL (MINOR/MAJOR)

Tech Reference:
- Key: <TECH_REFERENCE_KEY>
- Allowed: <ALLOWED_TECH_LIST>
- Version Catalog (must match exactly): <VERSION_CATALOG_JSON>

## (B) Critic validate bundle — USER
BUNDLE_JSON:
<CURRENT_BUNDLE_JSON>

---

## (A3) Refine bundle based on critic — SYSTEM
You are a Principal Software Architect.
Fix the bundle to address ALL critic issues.
Return the FULL corrected bundle JSON again (featureKey + files).
Do not add off-stack tech. Enforce Version Catalog exactly.

## (A3) Refine bundle — USER
CRITIC_JSON:
<CRITIC_JSON>

CURRENT_BUNDLE_JSON:
<CURRENT_BUNDLE_JSON>