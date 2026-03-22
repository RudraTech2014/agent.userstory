package com.agent.agent.userstory.service;

import com.agent.agent.userstory.model.dto.CriticResult;
import com.agent.agent.userstory.model.dto.SpecBundle;
import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunArchiveService;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import com.agent.agent.userstory.tech.TechReferenceKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SpecAgentProService {

    private static final Logger log = LoggerFactory.getLogger(SpecAgentProService.class);

    private final RunStore runStore;
    private final RunEventPublisher publisher;
    private final RunArchiveService archiveService;
    private final ChatAiDraftingService draftingService;
    private final AiDraftingService bundleService;
    private final AiCriticService criticService;
    private final ObjectMapper mapper = new ObjectMapper();

    public SpecAgentProService(RunStore runStore, RunEventPublisher publisher, RunArchiveService archiveService, ChatAiDraftingService draftingService, AiDraftingService bundleService, AiCriticService criticService) {
        this.runStore = runStore;
        this.publisher = publisher;
        this.archiveService = archiveService;
        this.draftingService = draftingService;
        this.bundleService = bundleService;
        this.criticService = criticService;
    }

    public Mono<Void> startRun(RunState state, String featureIdea, TechReferenceKey key) {
        return draftingService.streamSpecMd(state, featureIdea, key)
                .then(Mono.defer(() -> buildCriticLoop(state, featureIdea, key)))
                .doOnSuccess(v -> {
                    state.setPhase(RunState.Phase.DONE);
                    publisher.emitStatus(state, "DONE", state.getIteration());
                    publisher.emitDone(state);
                    archiveService.archiveSuccess(state);
                    runStore.removeRun(state.getRunId());
                    log.info("Run {} completed in {} ms", state.getRunId(), Duration.between(state.getCreatedAt(), Instant.now()).toMillis());
                })
                .doOnError(err -> {
                    publisher.emitError(state, "Run failed: " + err.getMessage());
                    archiveService.archiveError(state, err.getMessage());
                    runStore.removeRun(state.getRunId());
                    log.error("Run {} failed in {} ms: {}", state.getRunId(), Duration.between(state.getCreatedAt(), Instant.now()).toMillis(), err.getMessage());
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
        return runCriticIteration(state, featureIdea, key, initialBundle, 0, 2)
                .then(Mono.fromRunnable(() -> {
                    String finalJson = state.getFinalBundleJson();
                    if (finalJson != null) {
                        publisher.emitFinalBundle(state, finalJson);
                    }
                }));
    }

    private Mono<Void> runCriticIteration(RunState state, String featureIdea, TechReferenceKey key, SpecBundle bundle, int iteration, int maxIterations) {
        return Mono.defer(() -> {
            state.incrementIteration();
            publisher.emitStatus(state, "CRITIC", state.getIteration());

            CriticResult result = criticService.critique(bundle, key);
            try {
                String criticJson = mapper.writeValueAsString(result);
                publisher.emitCritic(state, criticJson);
                state.setCriticJson(criticJson);
            } catch (Exception e) {
                // ignore
            }

            if ("PASS".equalsIgnoreCase(result.getStatus()) || iteration >= maxIterations) {
                return Mono.empty();
            }

            publisher.emitStatus(state, "REFINING", state.getIteration());
            String note = "\n\n# Refinement " + (iteration + 1) + ": apply critic fixes\n";
            state.appendSpecChunk(note);

            return bundleService.buildBundle(state, featureIdea)
                    .then(Mono.fromCallable(() -> {
                        String newBundleJson = state.getFinalBundleJson();
                        if (newBundleJson == null) {
                            return bundle;
                        }
                        try {
                            return mapper.readValue(newBundleJson, SpecBundle.class);
                        } catch (Exception e) {
                            return bundle;
                        }
                    }))
                    .flatMap(updated -> runCriticIteration(state, featureIdea, key, updated, iteration + 1, maxIterations));
        });
    }
}
