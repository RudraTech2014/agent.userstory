package com.agent.agent.userstory.controller;

import com.agent.agent.userstory.runtime.RunState;
import com.agent.agent.userstory.runtime.RunStore;
import com.agent.agent.userstory.service.SpecAgentProService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SpecProRunController.class, ApiExceptionHandler.class})
class SpecProRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RunStore runStore;

    @MockBean
    private SpecAgentProService agentService;

    @Test
    void createRunRejectsBlankFeatureIdea() throws Exception {
        mockMvc.perform(post("/api/specpro/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"featureIdea\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createRunRejectsInvalidTechReferenceKey() throws Exception {
        mockMvc.perform(post("/api/specpro/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"featureIdea\":\"Search\",\"techReferenceKey\":\"BAD_KEY\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TECH_REFERENCE_KEY"));
    }

    @Test
    void createRunReturnsRunIdForValidPayload() throws Exception {
        RunState state = new RunState("run-2", Sinks.many().multicast().onBackpressureBuffer());
        Mockito.when(runStore.createRun()).thenReturn(state);
        Mockito.when(agentService.startRun(any(RunState.class), anyString(), any())).thenReturn(Mono.empty());

        mockMvc.perform(post("/api/specpro/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"featureIdea\":\"Search\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").value("run-2"));
    }
}
