package com.example.mcpstateful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main application class for MCP Stateful Tool Java server.
 * 
 * This Spring Boot application demonstrates stateful multi-turn conversations
 * using Spring AI function callbacks with MCP protocol support.
 */
@SpringBootApplication
public class McpStatefulToolApplication {

    public static void main(String[] args) {
        System.out.println("🤖 Starting MCP Stateful Tool Java Server...");
        SpringApplication.run(McpStatefulToolApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void serverReady() {
        System.out.println("✅ Spring AI MCP Server is ready for connections");
        System.out.println("⚠️  NOTE: Spring AI MCP 1.1.0-SNAPSHOT uses session-based transport");
        System.out.println("🔗 For MCP Inspector, try connecting to:");
        System.out.println("  - WebSocket: ws://localhost:8080/mcp/ws");
        System.out.println("  - SSE: http://localhost:8080/sse");
        System.out.println("  - REST (List Tools): POST http://localhost:8080/mcp/tools/list");
        System.out.println("  - REST (Call Tool): POST http://localhost:8080/mcp/tools/call");
        System.out.println("  - Health Check: http://localhost:8080/mcp/health");
        System.out.println("📋 Spring AI MCP Protocol Features:");
        System.out.println("  - Version: 2024-11-05");
        System.out.println("  - Transport: WebFlux (WebSocket, SSE, Streamable HTTP)");
        System.out.println("  - Tools: calculate, create_file, build_profile, list_sessions");
        System.out.println("  - Stateful conversations: Enabled");
        System.out.println("💡 Note: Spring AI MCP handles session management automatically");
    }
}