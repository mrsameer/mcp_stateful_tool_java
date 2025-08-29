package com.example.mcpstateful.tools;

import com.example.mcpstateful.state.ToolSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.mcpstateful.mcp.McpTool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateful tool that builds user profiles by collecting information across multiple turns.
 * 
 * This tool demonstrates how to progressively collect user information
 * and create structured profiles with optional file persistence.
 */
@Component
@McpTool(
    name = "build_profile",
    description = "Build a user profile by collecting information across multiple interactions. Can gather name, email, preferences, and other details progressively."
)
public class ProfileBuilderTool extends StatefulToolBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getToolName() {
        return "build_profile";
    }

    @Override
    public Map<String, String> getRequiredParameters() {
        return Map.of(
            "name", "User's full name",
            "email", "User's email address",
            "age", "User's age (as a number)",
            "preferences", "User preferences or interests (comma-separated list)"
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        // Get or create session
        ToolSession session = getOrCreateSession(arguments, getToolName(), getRequiredParameters());
        String sessionId = getSessionId(arguments, session);

        // Collect parameters
        for (String paramName : getRequiredParameters().keySet()) {
            if (arguments.containsKey(paramName)) {
                session.addParam(paramName, arguments.get(paramName));
            }
        }

        // Optional parameters
        if (arguments.containsKey("save_to_file")) {
            session.addParam("save_to_file", arguments.get("save_to_file"));
        } else {
            session.addParam("save_to_file", false);
        }

        // Check completeness
        if (!session.isComplete()) {
            String missingParam = session.getNextMissingParam();
            return createParameterRequestResponse(session, missingParam, sessionId);
        }

        // Build the profile
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("name", session.getCollectedParams().get("name"));
            profile.put("email", session.getCollectedParams().get("email"));
            profile.put("age", session.getCollectedParams().get("age"));
            
            // Parse preferences
            String preferencesStr = (String) session.getCollectedParams().get("preferences");
            List<String> preferences = Arrays.stream(preferencesStr.split(","))
                    .map(String::trim)
                    .toList();
            profile.put("preferences", preferences);
            profile.put("created_at", LocalDateTime.now().toString());

            String profileJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(profile);

            String result = "Profile created successfully!\n\n" + profileJson;

            // Save to file if requested
            boolean saveToFile = (Boolean) session.getCollectedParams().get("save_to_file");
            if (saveToFile) {
                String name = (String) profile.get("name");
                String filename = "profile_" + name.toLowerCase().replace(" ", "_") + ".json";
                
                try {
                    Files.writeString(Paths.get(filename), profileJson);
                    result += "\nProfile saved to: " + filename;
                } catch (IOException e) {
                    result += "\nWarning: Could not save to file: " + e.getMessage();
                }
            }

            // Clean up session
            sessionManager.deleteSession(sessionId);

            return result;

        } catch (Exception e) {
            // Clean up session on error
            sessionManager.deleteSession(sessionId);
            return String.format("Error creating profile: %s", e.getMessage());
        }
    }
}