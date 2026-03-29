frontend/
src/app/
app.routes.ts
- Defines route: /spec-pro -> SpecProComponent

    core/
      api/
        specpro-api.service.ts
          - HTTP client:
            - createRun()
            - getResult()
          - SSE subscribe helper (or delegates to sse-client)

      sse/
        sse-client.ts
          - EventSource wrapper:
            - connect(runId)
            - typed event handlers
            - cleanup/reconnect logic (optional)

      models/
        specpro.models.ts
          - TypeScript interfaces:
            SpecRunRequest, SpecRunCreated, SpecRunResult, SpecBundle, CriticResult
            SSE payload types: StatusEvent, SpecMdDeltaEvent, CriticEvent, FinalBundleEvent, ErrorEvent

    features/spec-pro/
      spec-pro.component.ts
        - Main container:
          - form input (feature idea + tech reference + optional constraints)
          - calls createRun()
          - connects to SSE stream
          - appends spec_md_delta into live viewer
          - stores final bundle and critic issues

      spec-pro.component.html
        - Layout:
          - left panel: input + dropdown + generate button + progress
          - right panel: live spec.md viewer
          - below/right: tabs for final bundle files

      spec-pro.component.scss
        - Minimal layout and tabs styles.

      components/
        tech-reference-select/
          tech-reference-select.component.ts
            - Dropdown list for tech reference presets.

        spec-live-viewer/
          spec-live-viewer.component.ts
            - Displays streaming spec.md content (markdown render or pre).

        bundle-tabs/
          bundle-tabs.component.ts
            - Tabs for bundle files:
              spec.md, plan.md, data-model.md, contracts/api-spec.json, quickstart.md, research.md

        critic-issues/
          critic-issues.component.ts
            - Displays critic issues grouped by file/severity.

src/app/features/spec-pro/spec-pro.component.spec.ts
- Component test: state transitions on SSE events

src/app/core/sse/sse-client.spec.ts
- SSE parsing test: appends spec_md_delta correctly, handles final_bundle