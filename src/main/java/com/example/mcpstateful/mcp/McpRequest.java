package com.example.mcpstateful.mcp;

import java.util.Map;

/**
 * Represents an MCP request message.
 */
public class McpRequest {
    private String jsonrpc = "2.0";
    private Object id;
    private String method;
    private Map<String, Object> params;

    // Constructors
    public McpRequest() {}

    public McpRequest(Object id, String method, Map<String, Object> params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    // Getters and Setters
    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}