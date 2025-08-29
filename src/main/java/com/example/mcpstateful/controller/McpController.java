package com.example.mcpstateful.controller;

import com.example.mcpstateful.tools.*;
import com.example.mcpstateful.function.FunctionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Spring AI MCP-compatible controller.
 * 
 * This controller provides MCP protocol endpoints while leveraging
 * Spring AI's function callback system for tool execution.
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    @Autowired
    private List<FunctionCallback> tools;

    /**
     * Initialize MCP connection.
     */
    @PostMapping("/initialize")
    public Map<String, Object> initialize(@RequestBody Map<String, Object> request) {
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
        
        return Map.of(
            "jsonrpc", "2.0",
            "id", request.get("id"),
            "result", result
        );
    }

    /**
     * List available tools.
     */
    @PostMapping("/tools/list")
    public Map<String, Object> listTools(@RequestBody Map<String, Object> request) {
        List<Map<String, Object>> toolList = tools.stream()
            .map(this::createToolDefinition)
            .toList();

        Map<String, Object> result = Map.of("tools", toolList);
        return Map.of(
            "jsonrpc", "2.0",
            "id", request.get("id"),
            "result", result
        );
    }

    /**
     * Call a tool.
     */
    @PostMapping("/tools/call")
    public Map<String, Object> callTool(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) request.get("params");
            
            String name = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (arguments == null) {
                arguments = new HashMap<>();
            }

            // Find the matching tool
            FunctionCallback tool = tools.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElse(null);

            String result;
            if (tool != null) {
                if (tool instanceof StatefulToolBase statefulTool) {
                    result = statefulTool.execute(arguments);
                } else {
                    result = tool.call("{}"); // Fallback for non-stateful tools
                }
            } else {
                result = "Unknown tool: " + name;
            }

            Map<String, Object> content = Map.of(
                "content", List.of(
                    Map.of("type", "text", "text", result)
                )
            );

            return Map.of(
                "jsonrpc", "2.0",
                "id", request.get("id"),
                "result", content
            );

        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "code", -32603,
                "message", "Internal error: " + e.getMessage()
            );
            return Map.of(
                "jsonrpc", "2.0",
                "id", request.get("id"),
                "error", error
            );
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

    private Map<String, Object> createToolDefinition(FunctionCallback tool) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("session_id", Map.of(
            "type", "string",
            "description", "Session ID for stateful interaction (optional for new sessions)"
        ));

        if (tool instanceof StatefulToolBase statefulTool) {
            for (Map.Entry<String, String> param : statefulTool.getRequiredParameters().entrySet()) {
                properties.put(param.getKey(), Map.of(
                    "type", "string",
                    "description", param.getValue()
                ));
            }
        }

        return Map.of(
            "name", tool.getName(),
            "description", tool.getDescription(),
            "inputSchema", Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of()  // No required params to allow stateful collection
            )
        );
    }
}
