package com.agent.agent.userstory.service;

import com.agent.agent.userstory.model.dto.SpecBundle;
import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.tech.TechReferenceKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Drafting service that streams spec.md content into RunState.
 *
 * This is a simulated streaming implementation for local tests. In a production
 * version this would call Spring AI ChatClient streaming APIs and forward
 * each chunk to the RunEventPublisher.
 */
@Service
public class AiDraftingService {

    private final RunEventPublisher publisher;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiDraftingService(RunEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Simulates streaming spec.md into the run sink.
     * After streaming completes, the bundle build step runs and emits final_bundle.
     */
    public Mono<Void> streamSpecMd(RunState state, String featureIdea, TechReferenceKey key) {
        // Emit entering drafting status
        publisher.emitStatus(state, "DRAFTING", state.getIteration());

        // Simulated chunks for spec.md (in real world, these come from ChatClient streaming)
        String[] chunks = new String[] {
                "# Title: " + featureIdea + "\n\n",
                "Short overview paragraph describing the feature and goals.\n\n",
                "Scenario: HAPPY_PATH\nGiven ... When ... Then ...\n\n",
                "Scenario: EDGE_CASE\nGiven ... When ... Then ...\n\n",
                "Scenario: ERROR_CASE\nGiven ... When ... Then ...\n\n"
        };

        // Simulate streaming with short delays
        return Flux.fromArray(chunks)
                .delayElements(Duration.ofMillis(150))
                .doOnNext(chunk -> {
                    // append to buffer
                    state.appendSpecChunk(chunk);
                    // emit spec_md_delta event
                    publisher.emitSpecMdDelta(state, chunk);
                })
                .then()
                .flatMap(v -> buildBundle(state, featureIdea))
                .doOnSuccess(v -> {
                    // finished drafting + building
                    state.setPhase(RunState.Phase.DRAFTING);
                    publisher.emitStatus(state, "DRAFTING_COMPLETE", state.getIteration());
                });
    }

    /**
     * Simulated bundle builder. Uses the spec.md from the state's buffer verbatim.
     * Emits building_bundle status and final_bundle SSE when done.
     */
    public Mono<Void> buildBundle(RunState state, String featureIdea) {
        publisher.emitStatus(state, "BUILDING_BUNDLE", state.getIteration());

        return Mono.fromCallable(() -> {
            // create a simple SpecBundle using spec.md verbatim
            String featureKey = featureIdea == null ? "feature" : featureIdea.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
            Map<String, String> files = new HashMap<>();
            files.put("spec.md", state.getSpecMd());
            files.put("plan.md", "Plan for " + featureIdea + "\n- backend classes\n- frontend components\n");
            files.put("data-model.md", "Data model: TODO\n");
            files.put("contracts/api-spec.json", "{ \"paths\": {} }");
            files.put("quickstart.md", "Quickstart: TODO\n");
            files.put("research.md", "Research notes: TODO\n");

            SpecBundle bundle = new SpecBundle(featureKey, files);
            String json = mapper.writeValueAsString(bundle);
            state.setFinalBundleJson(json);
            return json;
        })
        .doOnSuccess(json -> publisher.emitFinalBundle(state, json))
        .then();
    }
}
