package com.example.mcpstateful.function;

/**
 * Simple interface for MCP function callbacks.
 * 
 * This interface mimics Spring AI's FunctionCallback but is self-contained
 * for this MCP server implementation.
 */
public interface FunctionCallback {
    
    /**
     * Get the name of this function.
     */
    String getName();
    
    /**
     * Get the description of this function.
     */
    String getDescription();
    
    /**
     * Call this function with the provided arguments as JSON string.
     */
    String call(String functionArguments);
}