package com.example.mcpstateful.config;

import com.example.mcpstateful.service.StatefulCalculatorService;
import com.example.mcpstateful.service.StatefulFileService;
import com.example.mcpstateful.service.StatefulProfileBuilderService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI MCP Configuration using the official approach.
 * 
 * This configuration uses Spring AI MCP's built-in ToolCallbackProvider
 * with MethodToolCallbackProvider.builder() as shown in the official documentation.
 */
@Configuration
public class SpringAiMcpConfig {

    /**
     * Configure the MCP tools using the official Spring AI MCP approach.
     * This creates a ToolCallbackProvider that automatically discovers @Tool methods.
     */
    @Bean
    public ToolCallbackProvider statefulMcpTools(
            StatefulCalculatorService calculatorService,
            StatefulFileService fileService,
            StatefulProfileBuilderService profileBuilderService
    ) {
        System.out.println("🚀 Configuring Spring AI MCP Server with stateful tools:");
        System.out.println("  • calculate: Mathematical calculations with multi-turn conversations");
        System.out.println("  • create_file: File creation with progressive parameter collection");  
        System.out.println("  • list_sessions: Session management and debugging");
        System.out.println("  • build_profile: User profile creation with progressive parameter collection");
        
        System.out.println("📋 MCP Protocol Features:");
        System.out.println("   - Protocol version: 2024-11-05");
        System.out.println("   - Transport: WebFlux SSE (Server-Sent Events)");
        System.out.println("   - Stateful conversations: Supported");
        System.out.println("   - Session management: Active");
        System.out.println("   - Multi-turn execution: Enabled");
        
        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorService, fileService, profileBuilderService)
                .build();
    }
}
