package com.agent.agent.userstory.runtime;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

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
            emitNext(state, ev);
        } catch (Exception e) {
            // best-effort
        }
    }

    public void emitSpecMdDelta(RunState state, String chunk) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("spec_md_delta")
                .data(chunk)
                .build();
        emitNext(state, ev);
    }

    public void emitCritic(RunState state, String criticJson) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("critic")
                .data(criticJson)
                .build();
        emitNext(state, ev);
    }

    public void emitFinalBundle(RunState state, String bundleJson) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("final_bundle")
                .data(bundleJson)
                .build();
        emitNext(state, ev);
    }

    public void emitError(RunState state, String message) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("error")
                .data(message)
                .build();
        emitNext(state, ev);
        emitComplete(state);
    }

    public void emitDone(RunState state) {
        ServerSentEvent<String> ev = ServerSentEvent.<String>builder()
                .event("done")
                .data(String.format("{\"runId\":\"%s\",\"phase\":\"DONE\",\"iteration\":%d}", state.getRunId(), state.getIteration()))
                .build();
        emitNext(state, ev);
        emitComplete(state);
    }

    private void emitNext(RunState state, ServerSentEvent<String> event) {
        state.getSink().emitNext(event, (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
    }

    private void emitComplete(RunState state) {
        state.getSink().emitComplete((signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
    }
}
