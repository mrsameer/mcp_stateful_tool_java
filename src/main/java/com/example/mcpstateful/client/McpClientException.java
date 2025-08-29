package com.example.mcpstateful.client;

/**
 * Exception thrown by MCP client operations.
 */
public class McpClientException extends Exception {
    
    public McpClientException(String message) {
        super(message);
    }
    
    public McpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}