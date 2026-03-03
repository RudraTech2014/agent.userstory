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

        String status = issues.isEmpty() ? "PASS" : "FAIL";
        return new CriticResult(status, issues);
    }
}
