package com.agent.agent.userstory.model.dto;

public class SpecRunRequest {
    private String featureIdea;
    private String techReferenceKey;

    public String getFeatureIdea() {
        return featureIdea;
    }

    public void setFeatureIdea(String featureIdea) {
        this.featureIdea = featureIdea;
    }

    public String getTechReferenceKey() {
        return techReferenceKey;
    }

    public void setTechReferenceKey(String techReferenceKey) {
        this.techReferenceKey = techReferenceKey;
    }
}
