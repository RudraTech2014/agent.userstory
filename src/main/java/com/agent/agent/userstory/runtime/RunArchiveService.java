package com.agent.agent.userstory.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persists run metadata and artifacts to local disk for durability.
 */
@Service
public class RunArchiveService {

    private static final Logger log = LoggerFactory.getLogger(RunArchiveService.class);
    private static final Path ARCHIVE_DIR = Path.of("run-archive");

    private final ObjectMapper mapper = new ObjectMapper();

    public void archiveSuccess(RunState state) {
        Map<String, Object> payload = basePayload(state);
        payload.put("status", "SUCCESS");
        payload.put("finalBundleJson", state.getFinalBundleJson());
        write(state.getRunId(), payload);
    }

    public void archiveError(RunState state, String error) {
        Map<String, Object> payload = basePayload(state);
        payload.put("status", "ERROR");
        payload.put("error", error);
        payload.put("finalBundleJson", state.getFinalBundleJson());
        write(state.getRunId(), payload);
    }

    private Map<String, Object> basePayload(RunState state) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("runId", state.getRunId());
        payload.put("phase", state.getPhase().name());
        payload.put("iteration", state.getIteration());
        payload.put("createdAt", state.getCreatedAt().toString());
        payload.put("archivedAt", Instant.now().toString());
        payload.put("criticJson", state.getCriticJson());
        payload.put("specMd", state.getSpecMd());
        payload.put("techReferenceKey", state.getTechReferenceKey() == null ? null : state.getTechReferenceKey().name());
        return payload;
    }

    private void write(String runId, Map<String, Object> payload) {
        try {
            Files.createDirectories(ARCHIVE_DIR);
            Path output = ARCHIVE_DIR.resolve(runId + ".json");
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            Files.writeString(output, json + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.warn("Failed to archive run {}: {}", runId, e.getMessage());
        }
    }
}
