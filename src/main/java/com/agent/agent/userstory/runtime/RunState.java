package com.agent.agent.userstory.runtime;

import com.agent.agent.userstory.tech.TechReferenceKey;
import com.agent.agent.userstory.tech.TechReference;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Sinks;
import org.springframework.http.codec.ServerSentEvent;

/**
 * Mutable per-run state stored in RunStore.
 */
public class RunState {
    private final String runId;
    private final Sinks.Many<ServerSentEvent<String>> sink;
    private final StringBuilder specMdBuffer = new StringBuilder();
    private volatile String finalBundleJson;
    private volatile String criticJson;
    private final AtomicInteger iteration = new AtomicInteger(0);
    private volatile Phase phase = Phase.PENDING;
    private final Instant createdAt = Instant.now();
    private final TechReferenceKey techReferenceKey;

    public RunState(String runId, Sinks.Many<ServerSentEvent<String>> sink) {
        this(runId, sink, null);
    }

    public RunState(String runId, Sinks.Many<ServerSentEvent<String>> sink, TechReferenceKey techReferenceKey) {
        this.runId = runId;
        this.sink = sink;
        this.techReferenceKey = techReferenceKey;
    }

    public String getRunId() {
        return runId;
    }

    public Sinks.Many<ServerSentEvent<String>> getSink() {
        return sink;
    }

    public TechReferenceKey getTechReferenceKey() {
        return techReferenceKey;
    }

    public synchronized void appendSpecChunk(String chunk) {
        this.specMdBuffer.append(chunk);
    }

    public synchronized String getSpecMd() {
        return this.specMdBuffer.toString();
    }

    public String getFinalBundleJson() {
        return finalBundleJson;
    }

    public void setFinalBundleJson(String finalBundleJson) {
        this.finalBundleJson = finalBundleJson;
    }

    public String getCriticJson() {
        return criticJson;
    }

    public void setCriticJson(String criticJson) {
        this.criticJson = criticJson;
    }

    public int getIteration() {
        return iteration.get();
    }

    public int incrementIteration() {
        return iteration.incrementAndGet();
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum Phase {
        PENDING, DRAFTING, BUILDING_BUNDLE, CRITIC, REFINING, DONE, ERROR
    }
}
