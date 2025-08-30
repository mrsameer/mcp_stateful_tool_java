package com.example.mcpstateful;

import com.example.mcpstateful.state.SessionManager;
import com.example.mcpstateful.state.ToolSession;

import com.example.mcpstateful.service.StatefulCalculatorService;
import com.example.mcpstateful.service.StatefulFileService;
import com.example.mcpstateful.service.StatefulProfileBuilderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpStatefulToolApplicationTests {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private StatefulFileService statefulFileService;

    @Autowired
    private StatefulCalculatorService statefulCalculatorService;

    @Autowired
    private StatefulProfileBuilderService statefulProfileBuilderService;

    

    @Test
    void contextLoads() {
        assertThat(sessionManager).isNotNull();
        assertThat(statefulFileService).isNotNull();
        assertThat(statefulCalculatorService).isNotNull();
        assertThat(statefulProfileBuilderService).isNotNull();
    }

    @Test
    void testSessionManager() {
        String sessionId = sessionManager.generateSessionId();
        assertThat(sessionId).isNotNull();
        assertThat(sessionId).hasSize(36); // UUID length

        Map<String, String> requiredParams = Map.of("param1", "desc1");
        ToolSession session = sessionManager.createSession(sessionId, "test_tool", requiredParams);
        
        assertThat(session).isNotNull();
        assertThat(session.getToolName()).isEqualTo("test_tool");
        assertThat(session.isComplete()).isFalse();

        session.addParam("param1", "value1");
        assertThat(session.isComplete()).isTrue();

        sessionManager.deleteSession(sessionId);
        assertThat(sessionManager.getSession(sessionId)).isNull();
    }

    @Test
    void testCreateFileToolFlow() {
        // Test multi-turn file creation
        String result1 = statefulFileService.createFile(null, null, null); // Initial call without params
        assertThat(result1).contains("file_path");
        assertThat(result1).contains("Session ID:");

        // Extract session ID from the response
        String sessionId = extractSessionId(result1);
        assertThat(sessionId).isNotNull();

        String result2 = statefulFileService.createFile("/tmp/test_java.txt", null, sessionId);
        assertThat(result2).contains("content");

        String result3 = statefulFileService.createFile("/tmp/test_java.txt", "Hello from Java MCP Server!", sessionId);
        assertThat(result3).contains("Successfully created file");
        assertThat(sessionManager.getSession(sessionId)).isNull(); // Session should be cleaned up
    }

    // Helper to extract session ID (simplified for this example)
    private String extractSessionId(String response) {
        int startIndex = response.indexOf("Session ID: `") + "Session ID: `".length();
        int endIndex = response.indexOf("`", startIndex);
        if (startIndex != -1 && endIndex != -1) {
            return response.substring(startIndex, endIndex);
        }
        return null;
    }

    @Test
    void testCalculatorTool() {
        // Test single-turn calculation
        String result = statefulCalculatorService.calculate("2 + 2 * 3", "decimal", null);
        assertThat(result).contains("Expression: 2 + 2 * 3");
        assertThat(result).contains("Result: 8");
    }

    @Test
    void testProfileBuilderFlow() {
        // Test profile building start
        String result1 = statefulProfileBuilderService.buildProfile("Test User", null, null, null, null);
        assertThat(result1).contains("email");
        assertThat(result1).contains("Session ID:");

        String sessionId = extractSessionId(result1);
        assertThat(sessionId).isNotNull();

        String result2 = statefulProfileBuilderService.buildProfile("Test User", "test@example.com", 30, null, sessionId);
        assertThat(result2).contains("preferences");

        String result3 = statefulProfileBuilderService.buildProfile("Test User", "test@example.com", 30, "reading, coding", sessionId);
        assertThat(result3).contains("Profile created successfully!");
        assertThat(sessionManager.getSession(sessionId)).isNull(); // Session should be cleaned up
    }
}