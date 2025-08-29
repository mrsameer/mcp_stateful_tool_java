package com.example.mcpstateful.state;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Manages conversation sessions across tool calls.
 * 
 * This component maintains state for multi-turn conversations,
 * allowing tools to collect parameters progressively across
 * multiple interactions.
 */
@Component
public class SessionManager {
    
    private final Map<String, ToolSession> sessions = new ConcurrentHashMap<>();

    /**
     * Create a new tool session with required parameters.
     */
    public ToolSession createSession(String sessionId, String toolName, Map<String, String> requiredParams) {
        ToolSession session = new ToolSession(toolName, requiredParams);
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * Get an existing session by ID.
     */
    public ToolSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Update session parameters.
     */
    public ToolSession updateSession(String sessionId, Map<String, Object> updates) {
        ToolSession session = sessions.get(sessionId);
        if (session != null) {
            updates.forEach(session::addParam);
        }
        return session;
    }

    /**
     * Delete a session.
     */
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * List all active sessions.
     */
    public Map<String, ToolSession> listSessions() {
        return Map.copyOf(sessions);
    }

    /**
     * Generate a unique session ID.
     */
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Check if a session exists.
     */
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}