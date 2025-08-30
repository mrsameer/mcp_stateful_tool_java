package com.example.mcpstateful.service;

import com.example.mcpstateful.state.SessionManager;
import com.example.mcpstateful.state.ToolSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatefulProfileBuilderService {

    @Autowired
    private SessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "Build a user profile by collecting information across multiple interactions. Can gather name, email, preferences, and other details progressively.")
    public String buildProfile(String name, String email, Integer age, String preferences, String sessionId) {
        // Define required parameters
        Map<String, String> requiredParams = new java.util.LinkedHashMap<>();
        requiredParams.put("name", "User's full name");
        requiredParams.put("email", "User's email address");
        requiredParams.put("age", "User's age (as a number)");
        requiredParams.put("preferences", "User preferences or interests (comma-separated list)");

        // Get or create session
        ToolSession session;
        String currentSessionId;
        
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            session = sessionManager.getSession(sessionId);
            currentSessionId = sessionId;
            if (session == null) {
                session = sessionManager.createSession(sessionId, "build_profile", requiredParams);
            }
        } else {
            currentSessionId = sessionManager.generateSessionId();
            session = sessionManager.createSession(currentSessionId, "build_profile", requiredParams);
        }

        // Collect provided parameters
        if (name != null && !name.trim().isEmpty()) {
            session.addParam("name", name);
        }
        if (email != null && !email.trim().isEmpty()) {
            session.addParam("email", email);
        }
        if (age != null) {
            session.addParam("age", age);
        }
        if (preferences != null && !preferences.trim().isEmpty()) {
            session.addParam("preferences", preferences);
        }

        // Check if we have all required parameters
        if (!session.isComplete()) {
            String missingParam = session.getNextMissingParam();
            String paramDescription = requiredParams.get(missingParam);
            
            return String.format(
                "I need more information to build the profile.\n\n" +
                "Missing parameter: **%s**\n" +
                "Description: %s\n\n" +
                "Session ID: `%s`\n" +
                "Please call the tool again with this parameter.",
                missingParam,
                paramDescription,
                currentSessionId
            );
        }

        // Build the profile
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("name", session.getCollectedParams().get("name"));
            profile.put("email", session.getCollectedParams().get("email"));
            profile.put("age", session.getCollectedParams().get("age"));
            
            // Parse preferences
            String preferencesStr = (String) session.getCollectedParams().get("preferences");
            List<String> preferencesList = Arrays.stream(preferencesStr.split(","))
                    .map(String::trim)
                    .toList();
            profile.put("preferences", preferencesList);
            profile.put("created_at", LocalDateTime.now().toString());

            String profileJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(profile);

            String result = "Profile created successfully!\n\n" + profileJson;

            // Clean up session
            sessionManager.deleteSession(currentSessionId);

            return result;

        } catch (Exception e) {
            // Keep session active on error for retry
            return String.format(
                "Error creating profile: %s\n\n" +
                "Session ID: `%s`\n" +
                "Please call the tool again with corrected parameters.",
                e.getMessage(),
                currentSessionId
            );
        }
    }
}
