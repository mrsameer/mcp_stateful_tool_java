package com.example.mcpstateful.tools;

import com.example.mcpstateful.state.ToolSession;
import com.example.mcpstateful.mcp.McpTool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Stateful tool for creating files with multi-turn parameter collection.
 * 
 * This tool demonstrates how to collect file path and content progressively
 * across multiple interactions, providing a better user experience for
 * complex operations.
 */
@Component
@McpTool(
    name = "create_file",
    description = "Create a file with specified content. Can collect parameters across multiple interactions."
)
public class CreateFileTool extends StatefulToolBase {

    @Override
    public String getToolName() {
        return "create_file";
    }

    @Override
    public Map<String, String> getRequiredParameters() {
        return Map.of(
            "file_path", "The path where the file should be created (e.g., /tmp/example.txt)",
            "content", "The content to write to the file"
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        // Get or create session
        ToolSession session = getOrCreateSession(arguments, getToolName(), getRequiredParameters());
        String sessionId = getSessionId(arguments, session);

        // Collect provided parameters
        for (String paramName : getRequiredParameters().keySet()) {
            if (arguments.containsKey(paramName)) {
                session.addParam(paramName, arguments.get(paramName));
            }
        }

        // Optional parameters
        if (arguments.containsKey("encoding")) {
            session.addParam("encoding", arguments.get("encoding"));
        } else {
            session.addParam("encoding", "UTF-8");
        }

        // Check if we have all required parameters
        if (!session.isComplete()) {
            String missingParam = session.getNextMissingParam();
            return createParameterRequestResponse(session, missingParam, sessionId);
        }

        // Execute the file creation
        try {
            String filePath = (String) session.getCollectedParams().get("file_path");
            String content = (String) session.getCollectedParams().get("content");
            String encoding = (String) session.getCollectedParams().get("encoding");

            Path path = Paths.get(filePath);
            
            // Create parent directories if they don't exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // Write the file
            Files.writeString(path, content, Charset.forName(encoding));

            // Clean up session
            sessionManager.deleteSession(sessionId);

            return String.format("Successfully created file: %s\nSize: %d characters", 
                               filePath, content.length());

        } catch (IOException e) {
            // Clean up session on error
            sessionManager.deleteSession(sessionId);
            return String.format("Error creating file: %s", e.getMessage());
        }
    }
}