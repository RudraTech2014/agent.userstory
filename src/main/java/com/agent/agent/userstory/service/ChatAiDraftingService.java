package com.agent.agent.userstory.service;

import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.service.AiDraftingService;
import com.agent.agent.userstory.tech.TechReferenceCatalog;
import com.agent.agent.userstory.tech.TechReferenceKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

@Service
public class ChatAiDraftingService {

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
            return fallback.streamSpecMd(state, featureIdea, key);
        }

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

        return draftClient.prompt()
                .system(system)
                .user(user)
                .stream()
                .content() // Flux<String>
                .doOnNext(text -> {
                    if (text != null && !text.isEmpty()) {
                        state.appendSpecChunk(text);
                        publisher.emitSpecMdDelta(state, text);
                    }
                })
                .then(buildBundleFromStructuredOutput(state, featureIdea))   // cleaner than then().flatMap(...)
                .onErrorResume(ex -> fallback.streamSpecMd(state, featureIdea, key));
    }

    public Mono<Void> buildBundleFromStructuredOutput(RunState state, String featureIdea) {
        return fallback.buildBundle(state, featureIdea);
    }
}
