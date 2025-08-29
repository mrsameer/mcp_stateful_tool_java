package com.example.mcpstateful.mcp;

/**
 * Represents an MCP response message.
 */
public class McpResponse {
    private String jsonrpc = "2.0";
    private Object id;
    private Object result;
    private McpError error;

    // Constructors
    public McpResponse() {}

    public McpResponse(Object id, Object result) {
        this.id = id;
        this.result = result;
    }

    public McpResponse(Object id, McpError error) {
        this.id = id;
        this.error = error;
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public McpError getError() {
        return error;
    }

    public void setError(McpError error) {
        this.error = error;
    }

    /**
     * MCP Error representation.
     */
    public static class McpError {
        private int code;
        private String message;
        private Object data;

        public McpError() {}

        public McpError(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public McpError(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        // Getters and Setters
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}