package com.agent.agent.userstory.service;

import com.agent.agent.userstory.model.dto.CriticResult;
import com.agent.agent.userstory.model.dto.SpecBundle;
import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import com.agent.agent.userstory.tech.TechReferenceKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class SpecAgentProService {

    private final RunStore runStore;
    private final RunEventPublisher publisher;
    private final AiDraftingService draftingService;
    private final AiCriticService criticService;
    private final ObjectMapper mapper = new ObjectMapper();

    public SpecAgentProService(RunStore runStore, RunEventPublisher publisher, AiDraftingService draftingService, AiCriticService criticService) {
        this.runStore = runStore;
        this.publisher = publisher;
        this.draftingService = draftingService;
        this.criticService = criticService;
    }

    public Mono<Void> startRun(RunState state, String featureIdea, TechReferenceKey key) {
        return draftingService.streamSpecMd(state, featureIdea, key)
                .then(Mono.defer(() -> buildCriticLoop(state, featureIdea, key)))
                .doOnSuccess(v -> {
                    state.setPhase(RunState.Phase.DONE);
                    publisher.emitStatus(state, "DONE", state.getIteration());
                })
                .doOnError(err -> {
                    publisher.emitError(state, "Run failed: " + err.getMessage());
                });
    }

    private Mono<Void> buildCriticLoop(RunState state, String featureIdea, TechReferenceKey key) {
        return Mono.fromCallable(() -> {
            String bundleJson = state.getFinalBundleJson();
            SpecBundle bundle = null;
            if (bundleJson != null) {
                try {
                    bundle = mapper.readValue(bundleJson, SpecBundle.class);
                } catch (Exception e) {
                    // ignore parse error, will handle below
                }
            }
            return bundle;
        })
        .flatMap(bundle -> runCriticIterations(state, featureIdea, key, bundle));
    }

    private Mono<Void> runCriticIterations(RunState state, String featureIdea, TechReferenceKey key, SpecBundle initialBundle) {
        return Mono.defer(() -> {
            SpecBundle currentBundle = initialBundle;
            int maxIterations = 2;
            int iteration = 0;

            while (true) {
                iteration++;
                state.incrementIteration();
                publisher.emitStatus(state, "CRITIC", state.getIteration());

                // run critic
                CriticResult result = criticService.critique(currentBundle, key);
                try {
                    String criticJson = mapper.writeValueAsString(result);
                    publisher.emitCritic(state, criticJson);
                    state.setCriticJson(criticJson);
                } catch (Exception e) {
                    // ignore
                }

                if ("PASS".equalsIgnoreCase(result.getStatus())) {
                    // done
                    break;
                }

                if (iteration > maxIterations) {
                    // reached max refinements
                    break;
                }

                // REFINE: apply a simple refinement strategy: append note to spec.md
                publisher.emitStatus(state, "REFINING", state.getIteration());
                String note = "\n\n# Refinement " + iteration + ": apply critic fixes\n";
                state.appendSpecChunk(note);
                // rebuild bundle using updated spec buffer
                draftingService.buildBundle(state, featureIdea).block(Duration.ofSeconds(10));
                String newBundleJson = state.getFinalBundleJson();
                if (newBundleJson != null) {
                    try {
                        currentBundle = mapper.readValue(newBundleJson, SpecBundle.class);
                    } catch (Exception e) {
                        // keep currentBundle
                    }
                }
            }

            // after loop, emit final_bundle (ensure final bundle JSON exists)
            String finalJson = state.getFinalBundleJson();
            if (finalJson != null) {
                publisher.emitFinalBundle(state, finalJson);
            }

            return Mono.empty();
        }).then();
    }
}
