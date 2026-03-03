package com.agent.agent.userstory.tech;

import java.util.List;
import java.util.Map;

/**
 * Represents a tech reference preset: allowed tech list + version catalog.
 */
public class TechReference {
    private final List<String> allowed;
    private final Map<String, String> versionCatalog;

    public TechReference(List<String> allowed, Map<String, String> versionCatalog) {
        this.allowed = allowed;
        this.versionCatalog = versionCatalog;
    }

    public List<String> getAllowed() {
        return allowed;
    }

    public Map<String, String> getVersionCatalog() {
        return versionCatalog;
    }
}
