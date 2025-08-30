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
        SseEmitter emitter = new SseEmitter(10000L); // Short timeout for StreamableHttp
        
        try {
            // Process the incoming message immediately
            if (message != null) {
                processMCPMessageAndClose(clientId, message, emitter);
            } else {
                emitter.complete();
            }
        } catch (Exception e) {
            emitter.completeWithError(e);
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
        
        // Don't send unsolicited messages - wait for client requests
        // The MCP protocol requires client to initiate with 'initialize' request

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
     * Process MCP protocol message and close the connection (for StreamableHttp).
     */
    private void processMCPMessageAndClose(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
            // Validate message structure
            if (message == null) {
                sendErrorAndClose(emitter, null, -32700, "Parse error: Message cannot be null");
                return;
            }
            
            // Validate JSON-RPC version
            String jsonrpc = (String) message.get("jsonrpc");
            if (!"2.0".equals(jsonrpc)) {
                sendErrorAndClose(emitter, message.get("id"), -32600, "Invalid request: Missing or invalid jsonrpc field");
                return;
            }
            
            String method = (String) message.get("method");
            if (method == null || method.trim().isEmpty()) {
                sendErrorAndClose(emitter, message.get("id"), -32600, "Invalid request: Missing method field");
                return;
            }
            
            Object id = message.get("id");
            
            if ("tools/call".equals(method)) {
                handleToolCallAndClose(clientId, message, emitter);
            } else if ("tools/list".equals(method)) {
                handleToolListAndClose(clientId, message, emitter);
            } else if ("initialize".equals(method)) {
                handleInitializeAndClose(clientId, message, emitter);
            } else {
                // Unknown method
                sendErrorAndClose(emitter, id, -32601, "Method not found: " + method);
            }
            
        } catch (Exception e) {
            sendErrorAndClose(emitter, message != null ? message.get("id") : null, -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Process MCP protocol message.
     */
    private void processMCPMessage(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
            // Validate message structure
            if (message == null) {
                sendError(emitter, null, -32700, "Parse error: Message cannot be null");
                return;
            }
            
            // Validate JSON-RPC version
            String jsonrpc = (String) message.get("jsonrpc");
            if (!"2.0".equals(jsonrpc)) {
                sendError(emitter, message.get("id"), -32600, "Invalid request: Missing or invalid jsonrpc field");
                return;
            }
            
            String method = (String) message.get("method");
            if (method == null || method.trim().isEmpty()) {
                sendError(emitter, message.get("id"), -32600, "Invalid request: Missing method field");
                return;
            }
            
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
            Object messageId = message.get("id");
            if (messageId == null) {
                messageId = System.currentTimeMillis(); // Generate valid ID if null
            }
            
            Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", messageId,
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

        Object messageId = message.get("id");
        if (messageId == null) {
            messageId = System.currentTimeMillis(); // Generate valid ID if null
        }
        
        Map<String, Object> response = Map.of(
            "jsonrpc", "2.0",
            "id", messageId,
            "result", Map.of("tools", toolList)
        );

        emitter.send(SseEmitter.event()
            .name("message")
            .data(objectMapper.writeValueAsString(response)));
    }

    /**
     * Handle tool execution request and close connection.
     */
    private void handleToolCallAndClose(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
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
                Object messageId = message.get("id");
                if (messageId == null) {
                    messageId = System.currentTimeMillis(); // Generate valid ID if null
                }
                
                Map<String, Object> response = Map.of(
                    "jsonrpc", "2.0",
                    "id", messageId,
                    "result", Map.of(
                        "content", List.of(
                            Map.of("type", "text", "text", result)
                        )
                    )
                );

                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(response)));
                emitter.complete();

            } else {
                // Tool not found
                sendErrorAndClose(emitter, message.get("id"), -32601, "Tool not found: " + toolName);
            }
        } catch (Exception e) {
            sendErrorAndClose(emitter, message.get("id"), -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Handle tool listing request and close connection.
     */
    private void handleToolListAndClose(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
            List<Map<String, Object>> toolList = tools.stream()
                .map(this::createToolDefinition)
                .toList();

            Object messageId = message.get("id");
            if (messageId == null) {
                messageId = System.currentTimeMillis(); // Generate valid ID if null
            }
            
            Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", messageId,
                "result", Map.of("tools", toolList)
            );

            emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(response)));
            emitter.complete();
        } catch (Exception e) {
            sendErrorAndClose(emitter, message.get("id"), -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Handle initialization request and close connection.
     */
    private void handleInitializeAndClose(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
            Object messageId = message.get("id");
            if (messageId == null) {
                messageId = System.currentTimeMillis(); // Generate valid ID if null
            }
            
            Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", messageId,
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
            emitter.complete();
        } catch (Exception e) {
            sendErrorAndClose(emitter, message.get("id"), -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Handle initialization request.
     */
    private void handleInitialize(String clientId, Map<String, Object> message, SseEmitter emitter) throws IOException {
        Object messageId = message.get("id");
        if (messageId == null) {
            messageId = System.currentTimeMillis(); // Generate valid ID if null
        }
        
        Map<String, Object> response = Map.of(
            "jsonrpc", "2.0",
            "id", messageId,
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
     * Send error response and close connection (for StreamableHttp).
     */
    private void sendErrorAndClose(SseEmitter emitter, Object id, int code, String message) {
        try {
            // Ensure valid ID for error response
            if (id == null) {
                id = System.currentTimeMillis();
            }
            
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
            emitter.complete();
                
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * Send error response.
     */
    private void sendError(SseEmitter emitter, Object id, int code, String message) {
        try {
            // Ensure valid ID for error response
            if (id == null) {
                id = System.currentTimeMillis();
            }
            
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
