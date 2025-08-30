package com.example.mcpstateful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main application class for MCP Stateful Tool Java server with Streamable HTTP transport.
 * 
 * This Spring Boot application demonstrates stateful multi-turn conversations
 * using Spring AI MCP framework with Streamable HTTP protocol support.
 * 
 * Key features:
 * - Streamable HTTP transport (JSON-RPC over HTTP)
 * - MCP endpoint: http://localhost:8080/mcp
 * - Stateful tool conversations with session management
 * - Compatible with MCP Inspector and other MCP clients
 * - JSON-RPC 2.0 protocol implementation
 */
@SpringBootApplication
public class McpStatefulToolApplication {

    public static void main(String[] args) {
        System.out.println("🤖 Starting MCP Stateful Tool Java Server...");
        SpringApplication.run(McpStatefulToolApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void serverReady() {
        System.out.println("✅ Spring AI MCP Streamable HTTP Server is ready for connections");
        System.out.println("🌐 MCP Streamable HTTP Endpoint: http://localhost:8080/mcp");
        System.out.println("🔗 For MCP Inspector, connect to: http://localhost:8080/mcp");
        System.out.println("📋 Available API Endpoints:");
        System.out.println("  - MCP Protocol: POST http://localhost:8080/mcp");
        System.out.println("  - Health Check: GET http://localhost:8080/actuator/health");
        System.out.println("📋 MCP Streamable HTTP Features:");
        System.out.println("  - Protocol Version: 2024-11-05");
        System.out.println("  - Transport: Streamable HTTP (JSON-RPC over HTTP)");
        System.out.println("  - Tools: calculate, create_file, build_profile, list_sessions");
        System.out.println("  - Stateful Conversations: Enabled");
        System.out.println("  - Session Management: Automatic");
        System.out.println("💡 Connect with MCP Inspector using Streamable HTTP transport");
    }
}