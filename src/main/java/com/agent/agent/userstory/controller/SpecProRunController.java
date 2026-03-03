package com.agent.agent.userstory.controller;

import com.agent.agent.userstory.model.dto.SpecRunRequest;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import com.agent.agent.userstory.service.AiDraftingService;
import com.agent.agent.userstory.tech.TechReferenceKey;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller to create runs.
 */
@RestController
@RequestMapping("/api/specpro/runs")
public class SpecProRunController {

    private final RunStore runStore;
    private final AiDraftingService draftingService;

    public SpecProRunController(RunStore runStore, AiDraftingService draftingService) {
        this.runStore = runStore;
        this.draftingService = draftingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createRun(@RequestBody SpecRunRequest req) {
        TechReferenceKey key = null;
        if (req.getTechReferenceKey() != null) {
            try {
                key = TechReferenceKey.valueOf(req.getTechReferenceKey());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        RunState state = (key == null) ? runStore.createRun() : runStore.createRun(key);

        // start async streaming drafting
        draftingService.streamSpecMd(state, req.getFeatureIdea(), key)
                .subscribe();

        return ResponseEntity.ok().body(Map.of("runId", state.getRunId()));
    }
}
