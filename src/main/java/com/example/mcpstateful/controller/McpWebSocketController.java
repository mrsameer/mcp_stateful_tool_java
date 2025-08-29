package com.example.mcpstateful.controller;

import com.example.mcpstateful.tools.*;
import com.example.mcpstateful.function.FunctionCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket and SSE controller for MCP inspector streaming connections.
 * Provides real-time communication with MCP inspector tools.
 */
@Controller
public class McpWebSocketController {

    @Autowired
    private List<FunctionCallback> tools;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final List<String> activeConnections = new CopyOnWriteArrayList<>();

    /**
     * Handle WebSocket messages from MCP inspector.
     */
    @MessageMapping("/tools/call")
    @SendTo("/mcp/stream/response")
    public Map<String, Object> handleToolCall(Map<String, Object> message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.get("params");
            
            String name = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (arguments == null) {
                arguments = Map.of();
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
                    result = tool.call("{}");
                }
            } else {
                result = "Unknown tool: " + name;
            }

            // Stream the result to all connected clients
            Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", message.get("id"),
                "result", Map.of(
                    "content", List.of(
                        Map.of("type", "text", "text", result)
                    )
                )
            );

            // Send to WebSocket subscribers
            messagingTemplate.convertAndSend("/mcp/stream/response", response);

            // Send to SSE subscribers
            broadcastToSSE(response);

            return response;

        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "jsonrpc", "2.0",
                "id", message.get("id"),
                "error", Map.of(
                    "code", -32603,
                    "message", "Internal error: " + e.getMessage()
                )
            );

            // Send error to all subscribers
            messagingTemplate.convertAndSend("/mcp/stream/error", error);
            broadcastToSSE(error);

            return error;
        }
    }

    /**
     * SSE endpoint for MCP inspector connections (GET for connection, POST for messages).
     */
    @GetMapping("/mcp/stream")
    @ResponseBody
    public SseEmitter streamGet(@RequestParam String clientId) {
        return createSSEEmitter(clientId);
    }

    /**
     * SSE endpoint for MCP inspector connections (POST for sending messages).
     */
    @PostMapping("/mcp/stream")
    @ResponseBody
    public SseEmitter streamPost(@RequestParam String clientId, @RequestBody(required = false) Map<String, Object> message) {
        SseEmitter emitter = createSSEEmitter(clientId);
        
        // If a message was sent, process it
        if (message != null) {
            processIncomingMessage(clientId, message, emitter);
        }
        
        return emitter;
    }

    /**
     * Create and configure SSE emitter.
     */
    private SseEmitter createSSEEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Store the emitter
        sseEmitters.put(clientId, emitter);
        activeConnections.add(clientId);

        // Send initial connection message
        try {
            Map<String, Object> initMessage = Map.of(
                "type", "connection",
                "clientId", clientId,
                "message", "Connected to MCP server",
                "timestamp", System.currentTimeMillis()
            );
            emitter.send(SseEmitter.event()
                .name("init")
                .data(objectMapper.writeValueAsString(initMessage)));
        } catch (IOException e) {
            // Handle error
        }

        // Handle completion and errors
        emitter.onCompletion(() -> {
            sseEmitters.remove(clientId);
            activeConnections.remove(clientId);
        });

        emitter.onTimeout(() -> {
            sseEmitters.remove(clientId);
            activeConnections.remove(clientId);
        });

        emitter.onError((ex) -> {
            sseEmitters.remove(clientId);
            activeConnections.remove(clientId);
        });

        return emitter;
    }

    /**
     * Process incoming message from MCP inspector.
     */
    private void processIncomingMessage(String clientId, Map<String, Object> message, SseEmitter emitter) {
        try {
            // Handle MCP protocol messages
            if (message.containsKey("method") && "tools/call".equals(message.get("method"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) message.get("params");
                
                String name = (String) params.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                
                if (arguments == null) {
                    arguments = Map.of();
                }

                // Find and execute the tool
                FunctionCallback tool = tools.stream()
                    .filter(t -> t.getName().equals(name))
                    .findFirst()
                    .orElse(null);

                if (tool != null) {
                    String result;
                    if (tool instanceof StatefulToolBase statefulTool) {
                        result = statefulTool.execute(arguments);
                    } else {
                        result = tool.call("{}");
                    }

                    // Send result back to the client
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
                    // Send error for unknown tool
                    Map<String, Object> error = Map.of(
                        "jsonrpc", "2.0",
                        "id", message.get("id"),
                        "error", Map.of(
                            "code", -32601,
                            "message", "Method not found: " + name
                        )
                    );

                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(objectMapper.writeValueAsString(error)));
                }
            }
        } catch (Exception e) {
            try {
                Map<String, Object> error = Map.of(
                    "jsonrpc", "2.0",
                    "id", message.get("id"),
                    "error", Map.of(
                        "code", -32603,
                        "message", "Internal error: " + e.getMessage()
                    )
                );

                emitter.send(SseEmitter.event()
                    .name("error")
                    .data(objectMapper.writeValueAsString(error)));
            } catch (IOException ioException) {
                // Handle error sending error message
            }
        }
    }

    /**
     * Get list of active connections.
     */
    @GetMapping("/mcp/connections")
    @ResponseBody
    public Map<String, Object> getConnections() {
        return Map.of(
            "activeConnections", activeConnections.size(),
            "clients", activeConnections,
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Broadcast message to all SSE subscribers.
     */
    private void broadcastToSSE(Map<String, Object> message) {
        sseEmitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                // Remove failed emitter
                sseEmitters.remove(clientId);
                activeConnections.remove(clientId);
            }
        });
    }

    /**
     * Send message to specific client.
     */
    public void sendToClient(String clientId, Map<String, Object> message) {
        SseEmitter emitter = sseEmitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                // Remove failed emitter
                sseEmitters.remove(clientId);
                activeConnections.remove(clientId);
            }
        }
    }
}
