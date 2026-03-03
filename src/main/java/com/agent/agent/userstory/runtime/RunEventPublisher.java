package com.agent.agent.userstory.runtime;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

/**
 * Publishes standard SSE events into a run's sink.
 */
@Component
public class RunEventPublisher {

    public void emitStatus(RunState state, String phase, int iteration) {
        try {
            ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                    .event("status")
                    .data(String.format("{\"phase\":\"%s\",\"iteration\":%d}", phase, iteration))
                    .build();
            state.getSink().tryEmitNext(ev);
        } catch (Exception e) {
            // best-effort
        }
    }

    public void emitSpecMdDelta(RunState state, String chunk) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("spec_md_delta")
                .data(chunk)
                .build();
        state.getSink().tryEmitNext(ev);
    }

    public void emitCritic(RunState state, String criticJson) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("critic")
                .data(criticJson)
                .build();
        state.getSink().tryEmitNext(ev);
    }

    public void emitFinalBundle(RunState state, String bundleJson) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("final_bundle")
                .data(bundleJson)
                .build();
        state.getSink().tryEmitNext(ev);
    }

    public void emitError(RunState state, String message) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("error")
                .data(message)
                .build();
        state.getSink().tryEmitNext(ev);
    }
}
