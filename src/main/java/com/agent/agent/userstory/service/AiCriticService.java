package com.agent.agent.userstory.service;

import com.agent.agent.userstory.model.dto.CriticIssue;
import com.agent.agent.userstory.model.dto.CriticResult;
import com.agent.agent.userstory.model.dto.SpecBundle;
import com.agent.agent.userstory.tech.TechReferenceKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiCriticService {

    public CriticResult critique(SpecBundle bundle, TechReferenceKey key) {
        List<CriticIssue> issues = new ArrayList<>();
        String spec = null;
        if (bundle != null && bundle.getFiles() != null) {
            Map<String, String> files = bundle.getFiles();
            spec = files.get("spec.md");
        }

        if (spec == null || spec.isBlank()) {
            issues.add(new CriticIssue("BLOCKER", "spec.md", "spec.md is empty", "Provide a spec.md with 3 scenarios"));
        } else {
            int count = 0;
            if (spec.contains("HAPPY_PATH")) count++;
            if (spec.contains("EDGE_CASE")) count++;
            if (spec.contains("ERROR_CASE")) count++;
            if (count < 3) {
                issues.add(new CriticIssue("MAJOR", "spec.md", "spec.md must include exactly 3 scenarios: HAPPY_PATH, EDGE_CASE, ERROR_CASE", "Add missing scenarios"));
            }
        }

        if (bundle == null || bundle.getFiles() == null) {
            issues.add(new CriticIssue("BLOCKER", "bundle", "bundle files are missing", "Provide generated bundle artifacts"));
        } else {
            Map<String, String> files = bundle.getFiles();
            validateFile(issues, files, "plan.md");
            validateFile(issues, files, "data-model.md");
            validateFile(issues, files, "quickstart.md");
            validateFile(issues, files, "research.md");

            String api = files.get("contracts/api-spec.json");
            if (api == null || api.isBlank() || !api.contains("\"openapi\"")) {
                issues.add(new CriticIssue("MAJOR", "contracts/api-spec.json", "API contract is missing OpenAPI metadata", "Produce a minimal OpenAPI document"));
            }

            if (key != null) {
                String combined = String.join("\n", files.values());
                var reference = com.agent.agent.userstory.tech.TechReferenceCatalog.get(key);
                if (reference != null) {
                    for (String tech : reference.getAllowed()) {
                        if (!combined.toLowerCase().contains(tech.toLowerCase())) {
                            issues.add(new CriticIssue("MINOR", "bundle", "Artifact set does not reference allowed tech: " + tech, "Align docs with selected tech reference"));
                        }
                    }
                }
            }
        }

        String status = issues.isEmpty() ? "PASS" : "FAIL";
        return new CriticResult(status, issues);
    }

    private void validateFile(List<CriticIssue> issues, Map<String, String> files, String fileName) {
        String value = files.get(fileName);
        if (value == null || value.isBlank()) {
            issues.add(new CriticIssue("MAJOR", fileName, fileName + " is missing or empty", "Generate " + fileName + " with meaningful content"));
            return;
        }
        if (value.toUpperCase().contains("TODO")) {
            issues.add(new CriticIssue("MAJOR", fileName, fileName + " contains TODO placeholders", "Replace TODO placeholders with concrete content"));
        }
    }
}
