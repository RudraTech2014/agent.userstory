package com.agent.agent.userstory.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Sinks;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import com.agent.agent.userstory.tech.TechReferenceKey;

/**
 * Simple RunStore using Caffeine for TTL eviction.
 * Stores RunState keyed by runId.
 */
@Component
public class RunStore {

    private final Cache<String, RunState> cache;

    public RunStore() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30))
                .maximumSize(1000)
                .build();
    }

    public RunState createRun() {
        String runId = UUID.randomUUID().toString();
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();
        RunState state = new RunState(runId, sink);
        cache.put(runId, state);
        return state;
    }

    public RunState createRun(TechReferenceKey key) {
        String runId = UUID.randomUUID().toString();
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();
        RunState state = new RunState(runId, sink, key);
        cache.put(runId, state);
        return state;
    }

    public Optional<RunState> getRun(String runId) {
        return Optional.ofNullable(cache.getIfPresent(runId));
    }

    public void removeRun(String runId) {
        cache.invalidate(runId);
    }
}
