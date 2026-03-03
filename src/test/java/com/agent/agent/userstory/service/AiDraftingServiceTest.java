package com.agent.agent.userstory.service;

import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class AiDraftingServiceTest {

    @Test
    public void simulatedStreamAppendsSpecMd() {
        RunStore store = new RunStore();
        RunEventPublisher publisher = new RunEventPublisher();
        AiDraftingService svc = new AiDraftingService(publisher);

        RunState state = store.createRun();

        // Run the simulated streaming (blocking with timeout)
        svc.streamSpecMd(state, "Awesome feature", null).block(Duration.ofSeconds(5));

        String spec = state.getSpecMd();
        Assertions.assertTrue(spec.contains("Scenario: HAPPY_PATH"), "spec.md should contain HAPPY_PATH scenario");
        Assertions.assertTrue(spec.contains("Scenario: EDGE_CASE"), "spec.md should contain EDGE_CASE scenario");
        Assertions.assertTrue(spec.contains("Scenario: ERROR_CASE"), "spec.md should contain ERROR_CASE scenario");
    }
}
