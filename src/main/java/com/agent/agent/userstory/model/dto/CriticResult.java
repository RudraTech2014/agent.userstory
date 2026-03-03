package com.agent.agent.userstory.model.dto;

import java.util.List;

public class CriticResult {
    private String status; // PASS or FAIL
    private List<CriticIssue> issues;

    public CriticResult() {}

    public CriticResult(String status, List<CriticIssue> issues) {
        this.status = status;
        this.issues = issues;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<CriticIssue> getIssues() { return issues; }
    public void setIssues(List<CriticIssue> issues) { this.issues = issues; }
}
