package com.agent.agent.userstory.runtime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import java.time.Duration;
import java.util.List;

class RunStoreTest {

    @Test
    void sinkReplaysEventsToLateSubscribers() {
        RunStore store = new RunStore();
        RunState state = store.createRun();

        state.getSink().tryEmitNext(ServerSentEvent.<String>builder()
                .event("status")
                .data("{\"phase\":\"DRAFTING\"}")
                .build());
        state.getSink().tryEmitNext(ServerSentEvent.<String>builder()
                .event("done")
                .data("{\"phase\":\"DONE\"}")
                .build());
        state.getSink().tryEmitComplete();

        List<ServerSentEvent<String>> events = state.getSink()
                .asFlux()
                .collectList()
                .block(Duration.ofSeconds(1));

        Assertions.assertNotNull(events);
        Assertions.assertEquals(2, events.size());
        Assertions.assertEquals("status", events.get(0).event());
        Assertions.assertEquals("done", events.get(1).event());
    }
}
