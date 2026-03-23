package com.agent.agent.userstory.controller;

import com.agent.agent.userstory.model.dto.SpecRunRequest;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import com.agent.agent.userstory.service.SpecAgentProService;
import com.agent.agent.userstory.tech.TechReferenceKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller to create runs.
 */
@RestController
@RequestMapping("/api/specpro/runs")
public class SpecProRunController {

    private static final Logger log = LoggerFactory.getLogger(SpecProRunController.class);

    private final RunStore runStore;
    private final SpecAgentProService agentService;

    public SpecProRunController(RunStore runStore, SpecAgentProService agentService) {
        this.runStore = runStore;
        this.agentService = agentService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new SpecPro run",
            description = "Starts asynchronous draft/critic/refine execution and returns the runId."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Run created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"runId\":\"9f36cfff-6fb5-4ab7-9fbb-4f091ea4f5b0\"}")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation error or invalid techReferenceKey",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\":\"VALIDATION_ERROR\",\"message\":\"featureIdea featureIdea is required\"}")
            )
    )
    public ResponseEntity<Map<String, String>> createRun(@Valid @RequestBody SpecRunRequest req) {
        TechReferenceKey key = null;
        if (req.getTechReferenceKey() != null) {
            try {
                key = TechReferenceKey.valueOf(req.getTechReferenceKey());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_TECH_REFERENCE_KEY",
                        "message", "techReferenceKey must be a valid enum value"
                ));
            }
        }

        RunState state = (key == null) ? runStore.createRun() : runStore.createRun(key);
        log.info("Created run {} for featureIdea='{}' with techReferenceKey={}", state.getRunId(), req.getFeatureIdea(), key);

        // start async orchestration (draft -> critic -> refine)
        agentService.startRun(state, req.getFeatureIdea(), key).subscribe();

        return ResponseEntity.ok().body(Map.of("runId", state.getRunId()));
    }
}
