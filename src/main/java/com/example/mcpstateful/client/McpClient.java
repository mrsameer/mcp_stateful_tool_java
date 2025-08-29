package com.example.mcpstateful.client;

import com.example.mcpstateful.mcp.McpRequest;
import com.example.mcpstateful.mcp.McpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java client for connecting to MCP stateful server.
 * 
 * This client replicates the functionality of the Python interactive client,
 * providing methods to connect, list tools, and call tools with stateful sessions.
 */
public class McpClient {
    
    private static final String DEFAULT_SERVER_URL = "http://localhost:8081/mcp";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String serverUrl;
    private final AtomicInteger requestId;
    private boolean connected;
    
    /**
     * Create an MCP client with default server URL.
     */
    public McpClient() {
        this(DEFAULT_SERVER_URL);
    }
    
    /**
     * Create an MCP client with custom server URL.
     */
    public McpClient(String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.serverUrl = serverUrl;
        this.requestId = new AtomicInteger(1);
        this.connected = false;
    }
    
    /**
     * Connect to the MCP server and initialize the session.
     */
    public void connect() throws McpClientException {
        try {
            McpRequest initRequest = new McpRequest(
                requestId.getAndIncrement(),
                "initialize",
                Map.of(
                    "protocolVersion", "2024-11-05",
                    "capabilities", Map.of(),
                    "clientInfo", Map.of(
                        "name", "java-mcp-client",
                        "version", "1.0.0"
                    )
                )
            );
            
            McpResponse response = sendRequest("/initialize", initRequest);
            if (response.getError() != null) {
                throw new McpClientException("Initialization failed: " + response.getError().getMessage());
            }
            
            connected = true;
            System.out.println("✅ Connected to MCP server");
            
        } catch (Exception e) {
            throw new McpClientException("Failed to connect to server: " + e.getMessage(), e);
        }
    }
    
    /**
     * List available tools from the server.
     */
    @SuppressWarnings("unchecked")
    public List<ToolInfo> listTools() throws McpClientException {
        checkConnection();
        
        try {
            McpRequest request = new McpRequest(
                requestId.getAndIncrement(),
                "tools/list",
                Map.of()
            );
            
            McpResponse response = sendRequest("/tools/list", request);
            if (response.getError() != null) {
                throw new McpClientException("Failed to list tools: " + response.getError().getMessage());
            }
            
            Map<String, Object> result = (Map<String, Object>) response.getResult();
            List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
            
            return tools.stream()
                .map(tool -> new ToolInfo(
                    (String) tool.get("name"),
                    (String) tool.get("description"),
                    (Map<String, Object>) tool.get("inputSchema")
                ))
                .toList();
                
        } catch (Exception e) {
            throw new McpClientException("Failed to list tools: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call a tool with the given arguments.
     */
    @SuppressWarnings("unchecked")
    public String callTool(String toolName, Map<String, Object> arguments) throws McpClientException {
        checkConnection();
        
        try {
            if (arguments == null) {
                arguments = new HashMap<>();
            }
            
            McpRequest request = new McpRequest(
                requestId.getAndIncrement(),
                "tools/call",
                Map.of(
                    "name", toolName,
                    "arguments", arguments
                )
            );
            
            McpResponse response = sendRequest("/tools/call", request);
            if (response.getError() != null) {
                throw new McpClientException("Tool call failed: " + response.getError().getMessage());
            }
            
            // Extract text content from response
            Map<String, Object> result = (Map<String, Object>) response.getResult();
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            
            if (content != null && !content.isEmpty()) {
                Map<String, Object> firstContent = content.get(0);
                if ("text".equals(firstContent.get("type"))) {
                    return (String) firstContent.get("text");
                }
            }
            
            return "No response received";
            
        } catch (Exception e) {
            throw new McpClientException("Failed to call tool '" + toolName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Check server health.
     */
    public boolean isHealthy() {
        try {
            String healthUrl = serverUrl.replace("/mcp", "/mcp/health");
            Map<String, Object> health = restTemplate.getForObject(healthUrl, Map.class);
            return "healthy".equals(health.get("status"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Close the client connection.
     */
    public void close() {
        connected = false;
        System.out.println("👋 Disconnected from MCP server");
    }
    
    private McpResponse sendRequest(String endpoint, McpRequest request) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = objectMapper.writeValueAsString(request);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        String url = serverUrl + endpoint;
        return restTemplate.postForObject(url, entity, McpResponse.class);
    }
    
    private void checkConnection() throws McpClientException {
        if (!connected) {
            throw new McpClientException("Not connected to server. Call connect() first.");
        }
    }
    
    /**
     * Information about an available tool.
     */
    public static class ToolInfo {
        private final String name;
        private final String description;
        private final Map<String, Object> inputSchema;
        
        public ToolInfo(String name, String description, Map<String, Object> inputSchema) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Map<String, Object> getInputSchema() {
            return inputSchema;
        }
        
        @Override
        public String toString() {
            return String.format("ToolInfo{name='%s', description='%s'}", name, description);
        }
    }
}