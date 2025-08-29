package com.example.mcpstateful.tools;

import com.example.mcpstateful.state.ToolSession;
import com.example.mcpstateful.mcp.McpTool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Tool for listing all active conversation sessions.
 * 
 * This tool provides visibility into the current state of all
 * active multi-turn conversations, helping with session management
 * and debugging.
 */
@Component
@McpTool(
    name = "list_sessions",
    description = "List all active conversation sessions"
)
public class SessionListTool extends StatefulToolBase {

    @Override
    public String getToolName() {
        return "list_sessions";
    }

    @Override
    public Map<String, String> getRequiredParameters() {
        // This tool has no required parameters
        return Map.of();
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        Map<String, ToolSession> sessions = sessionManager.listSessions();
        
        if (sessions.isEmpty()) {
            return "No active sessions.";
        }

        StringBuilder result = new StringBuilder("**Active Sessions:**\n\n");
        
        for (Map.Entry<String, ToolSession> entry : sessions.entrySet()) {
            String sessionId = entry.getKey();
            ToolSession session = entry.getValue();
            
            result.append(String.format(
                "**Session ID:** `%s`\n" +
                "**Tool:** %s\n" +
                "**State:** %s\n" +
                "**Collected params:** %s\n" +
                "**Missing params:** %s\n\n",
                sessionId,
                session.getToolName(),
                session.getState().name().toLowerCase(),
                session.getCollectedParams().keySet(),
                session.getMissingParams()
            ));
            
            if (sessions.size() > 1) {
                result.append("---\n");
            }
        }

        return result.toString().trim();
    }
}