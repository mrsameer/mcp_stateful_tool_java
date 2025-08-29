package com.example.mcpstateful.service;

import com.example.mcpstateful.tools.*;
import com.example.mcpstateful.function.FunctionCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Streaming service for MCP tool execution.
 * Provides reactive streams for real-time tool responses.
 */
@Service
public class McpStreamingService {

    @Autowired
    private List<FunctionCallback> tools;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Flux<String>> activeStreams = new ConcurrentHashMap<>();

    /**
     * Execute tool with streaming response.
     */
    public Flux<String> executeToolStreaming(String toolName, Map<String, Object> arguments, String sessionId) {
        return Flux.create(sink -> {
            try {
                // Find the tool
                FunctionCallback tool = tools.stream()
                    .filter(t -> t.getName().equals(toolName))
                    .findFirst()
                    .orElse(null);

                if (tool == null) {
                    sink.error(new RuntimeException("Tool not found: " + toolName));
                    return;
                }

                // Execute tool with streaming
                if (tool instanceof StatefulToolBase statefulTool) {
                    // For stateful tools, we can stream the execution steps
                    streamStatefulToolExecution(statefulTool, arguments, sessionId, sink);
                } else {
                    // For regular tools, execute and stream the result
                    String result = tool.call(objectMapper.writeValueAsString(arguments));
                    streamResult(result, sink);
                }

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * Stream stateful tool execution.
     */
    private void streamStatefulToolExecution(StatefulToolBase tool, Map<String, Object> arguments, 
                                           String sessionId, reactor.core.publisher.FluxSink<String> sink) {
        try {
            // Send execution start
            sink.next(createStreamMessage("execution_start", Map.of(
                "tool", tool.getToolName(),
                "sessionId", sessionId,
                "timestamp", System.currentTimeMillis()
            )));

            // Execute the tool
            String result = tool.execute(arguments);

            // Stream the result in chunks for demonstration
            String[] chunks = result.split("(?<=\\G.{50})"); // Split into 50-char chunks
            
            for (int i = 0; i < chunks.length; i++) {
                sink.next(createStreamMessage("chunk", Map.of(
                    "index", i,
                    "total", chunks.length,
                    "content", chunks[i],
                    "timestamp", System.currentTimeMillis()
                )));
                
                // Add small delay between chunks for streaming effect
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Send execution complete
            sink.next(createStreamMessage("execution_complete", Map.of(
                "tool", tool.getToolName(),
                "result", result,
                "timestamp", System.currentTimeMillis()
            )));

            sink.complete();

        } catch (Exception e) {
            sink.error(e);
        }
    }

    /**
     * Stream regular tool result.
     */
    private void streamResult(String result, reactor.core.publisher.FluxSink<String> sink) {
        try {
            // Send execution start
            sink.next(createStreamMessage("execution_start", Map.of(
                "timestamp", System.currentTimeMillis()
            )));

            // Send result
            sink.next(createStreamMessage("result", Map.of(
                "content", result,
                "timestamp", System.currentTimeMillis()
            )));

            // Send execution complete
            sink.next(createStreamMessage("execution_complete", Map.of(
                "timestamp", System.currentTimeMillis()
            )));

            sink.complete();

        } catch (Exception e) {
            sink.error(e);
        }
    }

    /**
     * Create a streaming message.
     */
    private String createStreamMessage(String type, Map<String, Object> data) {
        try {
            Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"Failed to create message\"}";
        }
    }

    /**
     * Get available tools for streaming.
     */
    public Mono<List<String>> getAvailableTools() {
        return Mono.just(tools.stream()
            .map(FunctionCallback::getName)
            .toList());
    }

    /**
     * Create a heartbeat stream for connection monitoring.
     */
    public Flux<String> createHeartbeatStream(String clientId) {
        return Flux.interval(Duration.ofSeconds(30))
            .map(tick -> createStreamMessage("heartbeat", Map.of(
                "clientId", clientId,
                "tick", tick,
                "timestamp", System.currentTimeMillis()
            )));
    }

    /**
     * Stop a specific stream.
     */
    public void stopStream(String streamId) {
        Flux<String> stream = activeStreams.remove(streamId);
        if (stream != null) {
            // The stream will be automatically cleaned up
        }
    }

    /**
     * Get active stream count.
     */
    public int getActiveStreamCount() {
        return activeStreams.size();
    }
}
