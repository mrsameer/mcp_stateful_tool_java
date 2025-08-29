package com.example.mcpstateful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for MCP Stateful Tool Java server.
 * 
 * This Spring Boot application demonstrates stateful multi-turn conversations
 * using the Spring AI MCP framework.
 */
@SpringBootApplication
public class McpStatefulToolApplication {

    public static void main(String[] args) {
        System.out.println("🤖 Starting MCP Stateful Tool Java Server...");
        SpringApplication.run(McpStatefulToolApplication.class, args);
        System.out.println("✅ MCP Server is ready for connections");
    }
}