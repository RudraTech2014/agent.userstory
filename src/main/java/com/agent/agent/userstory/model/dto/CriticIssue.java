package com.agent.agent.userstory.model.dto;

public class CriticIssue {
    private String severity;
    private String file;
    private String problem;
    private String fix;

    public CriticIssue() {}

    public CriticIssue(String severity, String file, String problem, String fix) {
        this.severity = severity;
        this.file = file;
        this.problem = problem;
        this.fix = fix;
    }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public String getProblem() { return problem; }
    public void setProblem(String problem) { this.problem = problem; }

    public String getFix() { return fix; }
    public void setFix(String fix) { this.fix = fix; }
}
