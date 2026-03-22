package com.agent.agent.userstory.service;

import com.agent.agent.userstory.model.dto.SpecBundle;
import com.agent.agent.userstory.runtime.RunEventPublisher;
import com.agent.agent.userstory.runtime.RunStore;
import com.agent.agent.userstory.runtime.RunState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class ChatAiDraftingServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void buildBundleFromStructuredOutputBuildsNonPlaceholderArtifacts() throws Exception {
        RunStore store = new RunStore();
        RunEventPublisher publisher = new RunEventPublisher();
        AiDraftingService fallback = new AiDraftingService(publisher);
        ChatAiDraftingService service = new ChatAiDraftingService(null, fallback, publisher);

        RunState state = store.createRun();
        state.appendSpecChunk("# Title\n\nScenario: HAPPY_PATH\nScenario: EDGE_CASE\nScenario: ERROR_CASE\n");

        service.buildBundleFromStructuredOutput(state, "Streaming Spec").block(Duration.ofSeconds(2));

        SpecBundle bundle = mapper.readValue(state.getFinalBundleJson(), SpecBundle.class);
        Assertions.assertNotNull(bundle.getFiles().get("data-model.md"));
        Assertions.assertFalse(bundle.getFiles().get("data-model.md").contains("TODO"));
        Assertions.assertFalse(bundle.getFiles().get("quickstart.md").contains("TODO"));
        Assertions.assertFalse(bundle.getFiles().get("research.md").contains("TODO"));
        Assertions.assertTrue(bundle.getFiles().get("contracts/api-spec.json").contains("openapi"));
    }
}
