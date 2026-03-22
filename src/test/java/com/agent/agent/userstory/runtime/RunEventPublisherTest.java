package com.agent.agent.userstory.runtime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;

class RunEventPublisherTest {

    @Test
    void emitDonePublishesDoneAndCompletesSink() {
        RunEventPublisher publisher = new RunEventPublisher();
        RunState state = new RunState("run-1", Sinks.many().replay().all());

        publisher.emitDone(state);

        List<ServerSentEvent<String>> events = state.getSink().asFlux().collectList().block(Duration.ofSeconds(1));
        Assertions.assertNotNull(events);
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals("done", events.get(0).event());
    }
}
