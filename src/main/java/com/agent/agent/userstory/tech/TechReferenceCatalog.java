package com.agent.agent.userstory.tech;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hard-coded catalog of tech reference presets.
 */
public class TechReferenceCatalog {

    public static final Map<TechReferenceKey, TechReference> CATALOG;

    static {
        Map<String, String> javaAngularVersions = new HashMap<>();
        javaAngularVersions.put("Spring Boot", "3.5.10");
        javaAngularVersions.put("Angular", "21.1.4");
        javaAngularVersions.put("PostgreSQL", "18.2");
        javaAngularVersions.put("Spring AI", "1.1.x");

        TechReference javaAngular = new TechReference(
                List.of("Spring Boot", "Angular", "PostgreSQL", "Spring AI"),
                javaAngularVersions
        );

        Map<TechReferenceKey, TechReference> map = new HashMap<>();
        map.put(TechReferenceKey.JAVA_ANGULAR, javaAngular);
        CATALOG = Collections.unmodifiableMap(map);
    }

    public static TechReference get(TechReferenceKey key) {
        return CATALOG.get(key);
    }
}
