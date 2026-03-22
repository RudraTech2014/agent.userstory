package com.agent.agent.userstory.service;

import com.agent.agent.userstory.model.dto.CriticResult;
import com.agent.agent.userstory.model.dto.SpecBundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class AiCriticServiceTest {

    private final AiCriticService service = new AiCriticService();

    @Test
    void critiqueFailsWhenArtifactsContainPlaceholders() {
        SpecBundle bundle = new SpecBundle("feature", Map.of(
                "spec.md", "Scenario: HAPPY_PATH\nScenario: EDGE_CASE\nScenario: ERROR_CASE",
                "plan.md", "Plan",
                "data-model.md", "TODO",
                "contracts/api-spec.json", "{}",
                "quickstart.md", "TODO",
                "research.md", "TODO"
        ));

        CriticResult result = service.critique(bundle, null);
        Assertions.assertEquals("FAIL", result.getStatus());
        Assertions.assertTrue(result.getIssues().stream().anyMatch(i -> i.getFile().equals("data-model.md")));
        Assertions.assertTrue(result.getIssues().stream().anyMatch(i -> i.getFile().equals("contracts/api-spec.json")));
    }

    @Test
    void critiquePassesForWellFormedBundle() {
        SpecBundle bundle = new SpecBundle("feature", Map.of(
                "spec.md", "Scenario: HAPPY_PATH\nScenario: EDGE_CASE\nScenario: ERROR_CASE",
                "plan.md", "Plan",
                "data-model.md", "Entity definitions",
                "contracts/api-spec.json", "{\"openapi\":\"3.0.3\"}",
                "quickstart.md", "How to run",
                "research.md", "Findings"
        ));

        CriticResult result = service.critique(bundle, null);
        Assertions.assertEquals("PASS", result.getStatus());
    }
}
