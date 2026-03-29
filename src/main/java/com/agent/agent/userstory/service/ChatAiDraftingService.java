package com.agent.agent.userstory.service;

import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.service.AiDraftingService;
import com.agent.agent.userstory.tech.TechReferenceCatalog;
import com.agent.agent.userstory.tech.TechReferenceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ChatAiDraftingService {

    private static final Logger log = LoggerFactory.getLogger(ChatAiDraftingService.class);

    private final ChatClient draftClient;
    private final AiDraftingService fallback;
    private final RunEventPublisher publisher;

    public ChatAiDraftingService(@Nullable @Qualifier("draftClient") ChatClient draftClient, AiDraftingService fallback, RunEventPublisher publisher) {
        this.draftClient = draftClient;
        this.fallback = fallback;
        this.publisher = publisher;
    }

    public Mono<Void> streamSpecMd(RunState state, String featureIdea, TechReferenceKey key) {
        if (draftClient == null) {
            log.info("No draftClient available for run {}, using fallback drafting service", state.getRunId());
            return fallback.streamSpecMd(state, featureIdea, key);
        }

        publisher.emitStatus(state, "DRAFTING", state.getIteration());

        String system =
                "You are a Principal Software Architect. Respond ONLY with the contents of a GitHub Spec Kit spec.md file in Markdown.\n" +
                        "Include exactly 3 scenarios: HAPPY_PATH, EDGE_CASE, ERROR_CASE.\n" +
                        "Do not include JSON or comments.\n" +
                        "Do not wrap the entire file in a single code fence.";

        String allowed = "";
        if (key != null) {
            var ref = TechReferenceCatalog.get(key);
            if (ref != null) {
                allowed = String.join(", ", ref.getAllowed());
            }
        }

        String user =
                "Feature Idea:\n" + featureIdea +
                        "\n\nConstraints: Use only allowed tech: " + (allowed.isBlank() ? "(no explicit constraints)" : allowed) +
                        "\n\nReturn spec.md only.";

        log.info("Run {} sending drafting request via draftClient={} for techReferenceKey={}",
                state.getRunId(), draftClient.getClass().getName(), key);

        Instant startedAt = Instant.now();
        AtomicBoolean emittedAnyChunk = new AtomicBoolean(false);
        AtomicInteger chunkCount = new AtomicInteger(0);

        return draftClient.prompt()
                .system(system)
                .user(user)
                .stream()
                .content() // Flux<String>
                .doOnNext(text -> {
                    if (text != null && !text.isEmpty()) {
                        emittedAnyChunk.set(true);
                        chunkCount.incrementAndGet();
                        state.appendSpecChunk(text);
                        publisher.emitSpecMdDelta(state, text);
                    }
                })
                .doOnComplete(() -> log.info("Run {} completed drafting stream in {} ms with {} chunk(s)",
                        state.getRunId(), Duration.between(startedAt, Instant.now()).toMillis(), chunkCount.get()))
                .then(Mono.defer(() -> {
                    if (!emittedAnyChunk.get()) {
                        log.warn("Draft stream produced no content for run {}, using fallback drafting service", state.getRunId());
                        return fallback.streamSpecMd(state, featureIdea, key);
                    }
                    return buildBundleFromStructuredOutput(state, featureIdea);
                }))
                .onErrorResume(ex -> {
                    log.warn("Draft stream failed for run {}, using fallback drafting service: {}", state.getRunId(), ex.getMessage());
                    return fallback.streamSpecMd(state, featureIdea, key);
                });
    }

    public Mono<Void> buildBundleFromStructuredOutput(RunState state, String featureIdea) {
        return fallback.buildBundle(state, featureIdea);
    }
}
