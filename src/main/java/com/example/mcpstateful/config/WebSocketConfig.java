package com.example.mcpstateful.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for MCP inspector connections.
 * Enables STOMP messaging and WebSocket endpoints.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for MCP inspector communication
        config.enableSimpleBroker("/mcp/stream");
        config.setApplicationDestinationPrefixes("/mcp");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint for MCP inspector
        registry.addEndpoint("/mcp/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Direct WebSocket endpoint without SockJS fallback
        registry.addEndpoint("/mcp/ws-direct")
                .setAllowedOriginPatterns("*");
    }
}
