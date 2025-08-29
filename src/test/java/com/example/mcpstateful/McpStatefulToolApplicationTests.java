package com.example.mcpstateful;

import com.example.mcpstateful.state.SessionManager;
import com.example.mcpstateful.state.ToolSession;
import com.example.mcpstateful.tools.CreateFileTool;
import com.example.mcpstateful.tools.CalculatorTool;
import com.example.mcpstateful.tools.ProfileBuilderTool;
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
    private CreateFileTool createFileTool;

    @Autowired
    private CalculatorTool calculatorTool;

    @Autowired
    private ProfileBuilderTool profileBuilderTool;

    @Test
    void contextLoads() {
        assertThat(sessionManager).isNotNull();
        assertThat(createFileTool).isNotNull();
        assertThat(calculatorTool).isNotNull();
        assertThat(profileBuilderTool).isNotNull();
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
        Map<String, Object> args1 = Map.of();
        String result1 = createFileTool.execute(args1);
        assertThat(result1).contains("file_path");
        assertThat(result1).contains("Session ID:");

        // This would normally be extracted from the response in a real client
        String sessionId = sessionManager.generateSessionId();
        sessionManager.createSession(sessionId, "create_file", createFileTool.getRequiredParameters());

        Map<String, Object> args2 = Map.of(
            "session_id", sessionId,
            "file_path", "/tmp/test_java.txt"
        );
        String result2 = createFileTool.execute(args2);
        assertThat(result2).contains("content");

        Map<String, Object> args3 = Map.of(
            "session_id", sessionId,
            "content", "Hello from Java MCP Server!"
        );
        String result3 = createFileTool.execute(args3);
        assertThat(result3).contains("Successfully created file");
    }

    @Test
    void testCalculatorTool() {
        // Test single-turn calculation
        Map<String, Object> args = Map.of(
            "expression", "2 + 2 * 3",
            "format", "decimal"
        );
        String result = calculatorTool.execute(args);
        assertThat(result).contains("Expression: 2 + 2 * 3");
        assertThat(result).contains("Result: 8");
    }

    @Test
    void testProfileBuilderFlow() {
        // Test profile building start
        Map<String, Object> args1 = Map.of("name", "Test User");
        String result1 = profileBuilderTool.execute(args1);
        assertThat(result1).contains("email");
        assertThat(result1).contains("Session ID:");
    }
}