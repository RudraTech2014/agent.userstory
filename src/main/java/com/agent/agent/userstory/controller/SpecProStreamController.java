package com.agent.agent.userstory.controller;

import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * SSE streaming endpoint for runs.
 */
@RestController
@RequestMapping("/api/specpro/runs")
public class SpecProStreamController {

    private final RunStore runStore;

    public SpecProStreamController(RunStore runStore) {
        this.runStore = runStore;
    }

    @GetMapping(path = "/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamEvents(@PathVariable String runId) {
        return runStore.getRun(runId)
                .map(RunState::getSink)
                .map(sink -> sink.asFlux())
                .orElseGet(Flux::empty);
    }
}
