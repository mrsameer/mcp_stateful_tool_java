package com.example.mcpstateful.mcp;

import com.example.mcpstateful.tools.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST controller implementing MCP protocol over HTTP.
 * 
 * This controller provides MCP-compatible endpoints for
 * tool discovery and execution with stateful multi-turn
 * conversation support.
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    @Autowired
    private CreateFileTool createFileTool;

    @Autowired
    private CalculatorTool calculatorTool;

    @Autowired
    private ProfileBuilderTool profileBuilderTool;

    @Autowired
    private SessionListTool sessionListTool;

    /**
     * Initialize MCP connection.
     */
    @PostMapping("/initialize")
    public McpResponse initialize(@RequestBody McpRequest request) {
        Map<String, Object> result = Map.of(
            "protocolVersion", "2024-11-05",
            "capabilities", Map.of(
                "tools", Map.of(
                    "listChanged", false
                )
            ),
            "serverInfo", Map.of(
                "name", "mcp-stateful-server-java",
                "version", "1.0.0"
            )
        );
        
        return new McpResponse(request.getId(), result);
    }

    /**
     * List available tools.
     */
    @PostMapping("/tools/list")
    public McpResponse listTools(@RequestBody McpRequest request) {
        List<Map<String, Object>> tools = List.of(
            createToolDefinition("create_file", "Create a file with specified content. Can collect parameters across multiple interactions.", 
                createFileTool.getRequiredParameters()),
            createToolDefinition("calculate", "Perform mathematical calculations. Can collect parameters across multiple interactions.",
                calculatorTool.getRequiredParameters()),
            createToolDefinition("build_profile", "Build a user profile by collecting information across multiple interactions. Can gather name, email, preferences, and other details progressively.",
                profileBuilderTool.getRequiredParameters()),
            createToolDefinition("list_sessions", "List all active conversation sessions.",
                sessionListTool.getRequiredParameters())
        );

        Map<String, Object> result = Map.of("tools", tools);
        return new McpResponse(request.getId(), result);
    }

    /**
     * Call a tool.
     */
    @PostMapping("/tools/call")
    public McpResponse callTool(@RequestBody McpRequest request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) request.getParams();
            
            String name = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (arguments == null) {
                arguments = new HashMap<>();
            }

            String result = switch (name) {
                case "create_file" -> createFileTool.execute(arguments);
                case "calculate" -> calculatorTool.execute(arguments);
                case "build_profile" -> profileBuilderTool.execute(arguments);
                case "list_sessions" -> sessionListTool.execute(arguments);
                default -> "Unknown tool: " + name;
            };

            Map<String, Object> content = Map.of(
                "content", List.of(
                    Map.of("type", "text", "text", result)
                )
            );

            return new McpResponse(request.getId(), content);

        } catch (Exception e) {
            McpResponse.McpError error = new McpResponse.McpError(-32603, "Internal error: " + e.getMessage());
            return new McpResponse(request.getId(), error);
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "healthy",
            "server", "mcp-stateful-server-java",
            "version", "1.0.0"
        );
    }

    private Map<String, Object> createToolDefinition(String name, String description, Map<String, String> requiredParams) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("session_id", Map.of(
            "type", "string",
            "description", "Session ID for stateful interaction (optional for new sessions)"
        ));

        for (Map.Entry<String, String> param : requiredParams.entrySet()) {
            properties.put(param.getKey(), Map.of(
                "type", "string",
                "description", param.getValue()
            ));
        }

        return Map.of(
            "name", name,
            "description", description,
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of()  // No required params to allow stateful collection
            )
        );
    }
}