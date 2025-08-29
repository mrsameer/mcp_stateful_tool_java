package com.example.mcpstateful.controller;

import com.example.mcpstateful.service.McpStreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reactive controller for MCP streaming endpoints.
 * Provides WebFlux-based streaming responses for MCP inspector.
 */
@RestController
@RequestMapping("/mcp/reactive")
public class McpReactiveController {

    @Autowired
    private McpStreamingService streamingService;

    /**
     * Stream tool execution with Server-Sent Events.
     */
    @GetMapping(value = "/tools/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamToolExecution(
            @RequestParam String toolName,
            @RequestParam(required = false) String sessionId,
            @RequestBody(required = false) Map<String, Object> arguments) {
        
        if (arguments == null) {
            arguments = Map.of();
        }
        
        return streamingService.executeToolStreaming(toolName, arguments, sessionId);
    }

    /**
     * Get available tools as a stream.
     */
    @GetMapping(value = "/tools/list/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAvailableTools() {
        return streamingService.getAvailableTools()
            .flatMapMany(tools -> Flux.fromIterable(tools))
            .map(toolName -> "data: " + toolName + "\n\n");
    }

    /**
     * Create a heartbeat stream for connection monitoring.
     */
    @GetMapping(value = "/heartbeat/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> heartbeatStream(@PathVariable String clientId) {
        return streamingService.createHeartbeatStream(clientId)
            .map(message -> "data: " + message + "\n\n");
    }

    /**
     * Get server status as a stream.
     */
    @GetMapping(value = "/status/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamServerStatus() {
        return Flux.interval(java.time.Duration.ofSeconds(5))
            .map(tick -> {
                Map<String, Object> status = Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "activeStreams", streamingService.getActiveStreamCount(),
                    "uptime", tick * 5,
                    "status", "running"
                );
                return "data: " + status.toString() + "\n\n";
            });
    }

    /**
     * Execute tool with chunked response.
     */
    @PostMapping("/tools/execute-chunked")
    public Flux<String> executeToolChunked(
            @RequestParam String toolName,
            @RequestParam(required = false) String sessionId,
            @RequestBody Map<String, Object> arguments) {
        
        return streamingService.executeToolStreaming(toolName, arguments, sessionId);
    }

    /**
     * Get connection info.
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> getConnectionInfo() {
        return Mono.just(Map.of(
            "server", "mcp-stateful-server-java",
            "version", "1.0.0",
            "protocol", "MCP 2024-11-05",
            "streaming", true,
            "activeStreams", streamingService.getActiveStreamCount(),
            "endpoints", Map.of(
                "websocket", "/mcp/ws",
                "sse", "/mcp/stream",
                "reactive", "/mcp/reactive",
                "rest", "/mcp"
            )
        ));
    }
}
