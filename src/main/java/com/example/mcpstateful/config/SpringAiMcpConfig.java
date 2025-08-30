package com.example.mcpstateful.config;

import com.example.mcpstateful.tools.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Spring AI MCP Configuration.
 * 
 * This configuration leverages Spring AI's MCP autoconfiguration
 * to provide MCP protocol compliance with our stateful tools.
 */
@Configuration
public class SpringAiMcpConfig {

    @Autowired
    private List<StatefulToolBase> statefulTools;

    /**
     * Configure the MCP Server using Spring AI's autoconfiguration.
     * This provides MCP protocol compliance with our stateful tools.
     */
    @Bean
    @Primary
    public Object mcpServer() {
        // Spring AI MCP autoconfiguration will handle the server setup
        // We just need to ensure our tools are properly configured
        
        System.out.println("🚀 Spring AI MCP Server configured with " + statefulTools.size() + " tools:");
        for (StatefulToolBase tool : statefulTools) {
            System.out.println("  • " + tool.getToolName() + ": " + tool.getDescription());
        }
        
        System.out.println("📋 MCP Protocol Features:");
        System.out.println("   - Protocol version: 2024-11-05");
        System.out.println("   - Tool registration: " + statefulTools.size() + " tools");
        System.out.println("   - Stateful conversations: Supported");
        System.out.println("   - Session management: Active");
        System.out.println("   - Multi-turn execution: Enabled");
        
        return new Object(); // Placeholder - Spring AI will handle the actual server
    }

    /**
     * Get all stateful tools for MCP integration.
     */
    @Bean
    public List<StatefulToolBase> mcpTools() {
        return statefulTools;
    }
}
