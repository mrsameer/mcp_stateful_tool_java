package com.example.mcpstateful.tools;

import com.example.mcpstateful.state.SessionManager;
import com.example.mcpstateful.state.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

/**
 * Base class for stateful tools that support multi-turn conversations.
 * 
 * This class provides common functionality for managing conversation
 * state and collecting parameters progressively across multiple tool calls.
 */
public abstract class StatefulToolBase {

    @Autowired
    protected SessionManager sessionManager;

    /**
     * Create a parameter request response for missing parameters.
     */
    protected String createParameterRequestResponse(ToolSession session, String missingParam, String sessionId) {
        String paramDescription = session.getRequiredParams().get(missingParam);
        if (paramDescription == null) {
            paramDescription = missingParam;
        }

        return String.format(
            "I need more information to complete the %s operation.\n\n" +
            "Missing parameter: **%s**\n" +
            "Description: %s\n\n" +
            "Session ID: `%s`\n" +
            "Please call the tool again with this parameter.",
            session.getToolName(),
            missingParam,
            paramDescription,
            sessionId
        );
    }

    /**
     * Get or create a session for the tool execution.
     */
    protected ToolSession getOrCreateSession(Map<String, Object> arguments, 
                                           String toolName, 
                                           Map<String, String> requiredParams) {
        String sessionId = (String) arguments.get("session_id");
        
        if (sessionId != null) {
            ToolSession session = sessionManager.getSession(sessionId);
            if (session != null) {
                return session;
            } else {
                // Session ID provided but not found, create new one with the same ID
                return sessionManager.createSession(sessionId, toolName, requiredParams);
            }
        }
        
        // Create new session with new ID
        sessionId = sessionManager.generateSessionId();
        return sessionManager.createSession(sessionId, toolName, requiredParams);
    }

    /**
     * Extract session ID from arguments, creating one if it doesn't exist.
     */
    protected String getSessionId(Map<String, Object> arguments, ToolSession session) {
        String sessionId = (String) arguments.get("session_id");
        
        if (sessionId == null) {
            // Find session ID by looking through active sessions
            for (Map.Entry<String, ToolSession> entry : sessionManager.listSessions().entrySet()) {
                if (entry.getValue() == session) {
                    return entry.getKey();
                }
            }
            // If not found, generate new one
            sessionId = sessionManager.generateSessionId();
        }
        
        return sessionId;
    }

    /**
     * Abstract method that each tool must implement to define its specific logic.
     */
    public abstract String execute(Map<String, Object> arguments);

    /**
     * Abstract method to get the tool name.
     */
    public abstract String getToolName();

    /**
     * Abstract method to get the required parameters for this tool.
     */
    public abstract Map<String, String> getRequiredParameters();
}