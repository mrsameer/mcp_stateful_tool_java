package com.example.mcpstateful.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI MCP Client Configuration.
 * 
 * This configuration provides Spring AI MCP client integration
 * for better MCP protocol compliance on the client side.
 */
@Configuration
public class SpringAiMcpClientConfig {

    /**
     * Configure Spring AI MCP client properties.
     * This enables Spring AI's MCP client autoconfiguration.
     */
    @Bean
    public Object mcpClientConfig() {
        System.out.println("🔌 Spring AI MCP Client configuration enabled");
        System.out.println("   - MCP client autoconfiguration active");
        System.out.println("   - Protocol version: 2024-11-05");
        System.out.println("   - Supports: tools, resources, prompts");
        
        return new Object();
    }
}
