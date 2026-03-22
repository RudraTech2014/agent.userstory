package com.agent.agent.userstory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.model.chat=none",
		"spring.ai.openai.api-key=test-key",
		"spring.ai.openai.speech.api-key=test-key",
		"spring.ai.google.genai.api-key=test-key",
		"spring.ai.google.genai.project-id=test-project",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
