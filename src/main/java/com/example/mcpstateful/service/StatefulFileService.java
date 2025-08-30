package com.example.mcpstateful.service;

import com.example.mcpstateful.state.SessionManager;
import com.example.mcpstateful.state.ToolSession;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Stateful File Service using Spring AI's @Tool annotation.
 * Demonstrates stateful file creation across multiple turns.
 */
@Service
public class StatefulFileService {

    @Autowired
    private SessionManager sessionManager;

    /**
     * Create a file by collecting filename and content across multiple interactions.
     */
    @Tool(description = "Create a file with specified content using multi-turn parameter collection. " +
          "Can collect file_path and content parameters across multiple interactions.")
    public String createFile(String filePath, String content, String sessionId) {
        
        // Define required parameters
        Map<String, String> requiredParams = new java.util.LinkedHashMap<>();
        requiredParams.put("file_path", "Path where the file should be created (e.g., '/tmp/example.txt')");
        requiredParams.put("content", "Content to write to the file");

        // Get or create session
        ToolSession session;
        String currentSessionId;
        
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            session = sessionManager.getSession(sessionId);
            currentSessionId = sessionId;
            if (session == null) {
                session = sessionManager.createSession(sessionId, "create_file", requiredParams);
            }
        } else {
            currentSessionId = sessionManager.generateSessionId();
            session = sessionManager.createSession(currentSessionId, "create_file", requiredParams);
        }

        // Collect provided parameters
        if (filePath != null && !filePath.trim().isEmpty()) {
            // Validate file path
            if (filePath.trim().isEmpty()) {
                return "Error: File path cannot be empty.\n\nSession ID: `" + currentSessionId + "`\nPlease provide a valid file path.";
            }
            session.addParam("file_path", filePath);
        }
        
        if (content != null && !content.trim().isEmpty()) {
            session.addParam("content", content);
        }

        // Check if we have all required parameters
        if (!session.isComplete()) {
            String missingParam = session.getNextMissingParam();
            String paramDescription = requiredParams.get(missingParam);
            
            return String.format(
                "I need more information to create the file.\n\n" +
                "Missing parameter: **%s**\n" +
                "Description: %s\n\n" +
                "Session ID: `%s`\n" +
                "Please call the tool again with this parameter.",
                missingParam,
                paramDescription,
                currentSessionId
            );
        }

        // Create the file
        try {
            String path = (String) session.getCollectedParams().get("file_path");
            String fileContent = (String) session.getCollectedParams().get("content");

            Path filePath2 = Paths.get(path);
            
            // Create parent directories if they don't exist
            Path parentDir = filePath2.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // Write the file
            Files.writeString(filePath2, fileContent);

            // Clean up session after successful completion
            sessionManager.deleteSession(currentSessionId);

            return String.format(
                "✅ Successfully created file: %s\n" +
                "Content length: %d characters\n" +
                "File size: %d bytes",
                path,
                fileContent.length(),
                Files.size(filePath2)
            );

        } catch (IOException e) {
            // Keep session active on error for retry
            return String.format(
                "❌ Error creating file: %s\n\n" +
                "Session ID: `%s`\n" +
                "Please call the tool again with corrected parameters.",
                e.getMessage(),
                currentSessionId
            );
        }
    }

    /**
     * List active sessions for debugging/management.
     */
    @Tool(description = "List all active conversation sessions for debugging and management")
    public String listSessions() {
        Map<String, ToolSession> sessions = sessionManager.listSessions();
        
        if (sessions.isEmpty()) {
            return "No active sessions found.";
        }

        StringBuilder result = new StringBuilder("Active Sessions:\n\n");
        for (Map.Entry<String, ToolSession> entry : sessions.entrySet()) {
            String sessionId = entry.getKey();
            ToolSession session = entry.getValue();
            
            result.append(String.format(
                "Session ID: `%s`\n" +
                "Tool: %s\n" +
                "Collected Parameters: %s\n" +
                "Missing Parameters: %s\n" +
                "Complete: %s\n\n",
                sessionId,
                session.getToolName(),
                session.getCollectedParams().keySet(),
                session.getMissingParams(),
                session.isComplete() ? "✅" : "❌"
            ));
        }

        return result.toString().trim();
    }
}