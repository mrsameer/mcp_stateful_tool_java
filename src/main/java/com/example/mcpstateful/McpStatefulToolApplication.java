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
        System.out.println("✅ MCP Server is ready for connections on http://localhost:8080/mcp");
        System.out.println("📚 Available endpoints:");
        System.out.println("  - POST /mcp/initialize");
        System.out.println("  - POST /mcp/tools/list");
        System.out.println("  - POST /mcp/tools/call");
        System.out.println("  - GET  /mcp/health");
    }
}