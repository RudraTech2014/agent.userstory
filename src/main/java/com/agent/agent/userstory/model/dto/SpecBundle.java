package com.agent.agent.userstory.model.dto;

import java.util.Map;

public class SpecBundle {
    private String featureKey;
    private Map<String, String> files;

    public SpecBundle() {}

    public SpecBundle(String featureKey, Map<String, String> files) {
        this.featureKey = featureKey;
        this.files = files;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
}
