package com.example.mcpstateful.controller;

import com.example.mcpstateful.tools.*;
import com.example.mcpstateful.function.FunctionCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Protocol Controller for handling MCP Inspector connections.
 * Provides endpoints that follow the MCP protocol specification.
 */
@RestController
@RequestMapping("/mcp")
public class McpProtocolController {

    @Autowired
    private List<FunctionCallback> tools;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, SseEmitter> mcpConnections = new ConcurrentHashMap<>();

    /**
     * MCP Protocol endpoint for streaming connections.
     * Accepts both GET (for connection) and POST (for messages).
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectMCP(@RequestParam String clientId) {
        return createMCPConnection(clientId);
    }

    /**
     * MCP Protocol endpoint for sending messages.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMCPMessage(@RequestParam String clientId, @RequestBody Map<String, Object> message) {
        SseEmitter emitter = createMCPConnection(clientId);
        
        // Process the incoming message
        if (message != null) {
            processMCPMessage(clientId, message, emitter);
        }
        
        return emitter;
    }

    /**
     * Create MCP connection with SSE emitter.
     */
    private SseEmitter createMCPConnection(String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Store the connection
        mcpConnections.put(clientId, emitter);
        
        // Send connection confirmation
        try {
            Map<String, Object> initMessage = new HashMap<>();
            initMessage.put("jsonrpc", "2.0");
            initMessage.put("id", null);
            initMessage.put("result", Map.of(
                "type", "connection_established",
                "clientId", clientId,
                "message", "MCP connection established",
                "timestamp", System.currentTimeMillis()
            ));
            
            emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(initMessage)));
                
            // Send initial tool list automatically
            sendInitialToolList(clientId, emitter);
                
        } catch (IOException e) {
            // Handle error
        }

        // Handle connection lifecycle
        emitter.onCompletion(() -> {
            mcpConnections.remove(clientId);
        });

        emitter.onTimeout(() -> {
            mcpConnections.remove(clientId);
        });

        emitter.onError((ex) -> {
            mcpConnections.remove(clientId);
        });

        return emitter;
    }

    /**
     * Process MCP protocol message.
     */
    private void processMCPMessage(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
            String method = (String) message.get("method");
            Object id = message.get("id");
            
            if ("tools/call".equals(method)) {
                handleToolCall(clientId, message, emitter);
            } else if ("tools/list".equals(method)) {
                handleToolList(clientId, message, emitter);
            } else if ("initialize".equals(method)) {
                handleInitialize(clientId, message, emitter);
            } else {
                // Unknown method
                sendError(emitter, id, -32601, "Method not found: " + method);
            }
            
        } catch (Exception e) {
            sendError(emitter, message.get("id"), -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Handle tool execution request.
     */
    private void handleToolCall(String clientId, Map<String, Object> message, SseEmitter emitter) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) message.get("params");
        
        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        if (arguments == null) {
            arguments = Map.of();
        }

        // Find the tool
        FunctionCallback tool = tools.stream()
            .filter(t -> t.getName().equals(toolName))
            .findFirst()
            .orElse(null);

        if (tool == null) {
            // Try alternative tool names for backward compatibility
            if ("calculator".equals(toolName)) {
                tool = tools.stream()
                    .filter(t -> "calculate".equals(t.getName()))
                    .findFirst()
                    .orElse(null);
            }
        }

        if (tool != null) {
            // Execute the tool
            String result;
            if (tool instanceof StatefulToolBase statefulTool) {
                result = statefulTool.execute(arguments);
            } else {
                result = tool.call("{}");
            }

            // Send success response
            Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", message.get("id"),
                "result", Map.of(
                    "content", List.of(
                        Map.of("type", "text", "text", result)
                    )
                )
            );

            emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(response)));

        } else {
            // Tool not found
            sendError(emitter, message.get("id"), -32601, "Tool not found: " + toolName);
        }
    }

    /**
     * Handle tool listing request.
     */
    private void handleToolList(String clientId, Map<String, Object> message, SseEmitter emitter) throws IOException {
        List<Map<String, Object>> toolList = tools.stream()
            .map(this::createToolDefinition)
            .toList();

        Map<String, Object> response = Map.of(
            "jsonrpc", "2.0",
            "id", message.get("id"),
            "result", Map.of("tools", toolList)
        );

        emitter.send(SseEmitter.event()
            .name("message")
            .data(objectMapper.writeValueAsString(response)));
    }

    /**
     * Handle initialization request.
     */
    private void handleInitialize(String clientId, Map<String, Object> message, SseEmitter emitter) throws IOException {
        Map<String, Object> response = Map.of(
            "jsonrpc", "2.0",
            "id", message.get("id"),
            "result", Map.of(
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
            )
        );

        emitter.send(SseEmitter.event()
            .name("message")
            .data(objectMapper.writeValueAsString(response)));
    }

    /**
     * Send error response.
     */
    private void sendError(SseEmitter emitter, Object id, int code, String message) {
        try {
            Map<String, Object> error = Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "error", Map.of(
                    "code", code,
                    "message", message
                )
            );

            emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(error)));
                
        } catch (IOException e) {
            // Handle error sending error message
        }
    }

    /**
     * Create tool definition for MCP protocol.
     */
    private Map<String, Object> createToolDefinition(FunctionCallback tool) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("session_id", Map.of(
            "type", "string",
            "description", "Session ID for stateful interaction (optional for new sessions)"
        ));

        if (tool instanceof StatefulToolBase statefulTool) {
            // Add tool-specific parameters
            Map<String, String> requiredParams = statefulTool.getRequiredParameters();
            if (requiredParams != null && !requiredParams.isEmpty()) {
                for (Map.Entry<String, String> param : requiredParams.entrySet()) {
                    properties.put(param.getKey(), Map.of(
                        "type", "string",
                        "description", param.getValue()
                    ));
                }
            }
        }

        // Create response with proper MCP tool format
        Map<String, Object> response = new HashMap<>();
        response.put("name", tool.getName());
        response.put("description", tool.getDescription());
        response.put("inputSchema", Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of()
        ));

        return response;
    }

    /**
     * Send initial tool list to MCP Inspector.
     */
    private void sendInitialToolList(String clientId, SseEmitter emitter) {
        try {
            List<Map<String, Object>> toolList = tools.stream()
                .map(this::createToolDefinition)
                .toList();

            // Send tools list as a proper MCP response
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", null);
            response.put("result", Map.of("tools", toolList));

            // Send with proper event name that MCP Inspector expects
            emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(response)));
                
        } catch (Exception e) {
            // Handle error
        }
    }

    /**
     * Get active MCP connections.
     */
    @GetMapping("/connections/mcp")
    public Map<String, Object> getMCPConnections() {
        return Map.of(
            "activeConnections", mcpConnections.size(),
            "clients", mcpConnections.keySet(),
            "timestamp", System.currentTimeMillis()
        );
    }
}
