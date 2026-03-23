package com.agent.agent.userstory.runtime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

import java.nio.file.Files;
import java.nio.file.Path;

class RunArchiveServiceTest {

    @Test
    void archiveSuccessWritesJsonFile() throws Exception {
        RunArchiveService service = new RunArchiveService();
        String runId = "archive-test-run";
        RunState state = new RunState(runId, Sinks.many().multicast().onBackpressureBuffer());
        state.setPhase(RunState.Phase.DONE);
        state.setFinalBundleJson("{\"ok\":true}");

        service.archiveSuccess(state);

        Path archived = Path.of("run-archive", runId + ".json");
        Assertions.assertTrue(Files.exists(archived));
        String json = Files.readString(archived);
        Assertions.assertTrue(json.contains("\"runId\""));
        Files.deleteIfExists(archived);
    }
}
