package com.example.mcpstateful.client;

import com.example.mcpstateful.client.McpClient.ToolInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interactive MCP client for chatting with the stateful server.
 * 
 * This class provides an interactive command-line interface similar to the Python
 * interactive_client.py, allowing users to interact with MCP tools through 
 * multi-turn conversations.
 */
public class InteractiveMcpClient {
    
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("Session ID: `([^`]+)`");
    
    private final McpClient client;
    private final BufferedReader reader;
    private final Map<String, String> activeSessions;
    
    /**
     * Create an interactive MCP client.
     */
    public InteractiveMcpClient() {
        this.client = new McpClient();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Create an interactive MCP client with custom server URL.
     */
    public InteractiveMcpClient(String serverUrl) {
        this.client = new McpClient(serverUrl);
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Start the interactive chat session.
     */
    public void start() {
        System.out.println("🤖 MCP Stateful Server - Interactive Java Client");
        System.out.println("=" .repeat(50));
        System.out.println("Commands:");
        System.out.println("  help              - Show available tools and commands");
        System.out.println("  tools             - List all available tools");
        System.out.println("  sessions          - Show active sessions");
        System.out.println("  list_sessions     - Show active sessions (same as sessions)");
        System.out.println("  create_file       - Start file creation");
        System.out.println("  calculate         - Start calculation");
        System.out.println("  build_profile     - Start profile building");
        System.out.println("  quit              - Exit chat");
        System.out.println("=" .repeat(50));
        
        try {
            // Connect to server
            System.out.println("📡 Connecting to server...");
            client.connect();
            
            // Show available tools
            List<ToolInfo> tools = client.listTools();
            System.out.println("📋 Available tools:");
            for (ToolInfo tool : tools) {
                System.out.println("  • " + tool.getName() + ": " + tool.getDescription());
            }
            System.out.println();
            
            // Main chat loop
            while (true) {
                try {
                    System.out.print("💬 You: ");
                    String userInput = reader.readLine();
                    
                    if (userInput == null || userInput.trim().isEmpty()) {
                        continue;
                    }
                    
                    String command = userInput.trim().toLowerCase();
                    if (command.equals("quit") || command.equals("exit") || command.equals("q")) {
                        System.out.println("👋 Goodbye!");
                        break;
                    }
                    
                    handleCommand(command);
                    
                } catch (IOException e) {
                    System.err.println("❌ Error reading input: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Error: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        } finally {
            client.close();
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println("Error closing reader: " + e.getMessage());
            }
        }
    }
    
    private void handleCommand(String command) throws Exception {
        switch (command) {
            case "help" -> showHelp();
            case "tools" -> showTools();
            case "sessions" -> showSessions();
            case "list_sessions" -> showSessions();  // Allow both 'sessions' and 'list_sessions'
            case "create_file" -> startFileCreation();
            case "calculate" -> startCalculation();
            case "build_profile" -> startProfileBuilding();
            default -> {
                // Try to call it as a tool directly
                try {
                    String response = client.callTool(command, new HashMap<>());
                    System.out.println("🤖 Server: " + response);
                } catch (Exception e) {
                    System.out.println("❌ Unknown command. Type 'help' for available commands.");
                    System.out.println("   Available commands: help, tools, sessions, list_sessions, create_file, calculate, build_profile, quit");
                }
            }
        }
    }
    
    private void showHelp() {
        System.out.println("\n📖 Help - Available Commands:");
        System.out.println("  help              - Show this help");
        System.out.println("  tools             - List available tools");
        System.out.println("  sessions          - Show active sessions");
        System.out.println("  list_sessions     - Show active sessions (same as sessions)");
        System.out.println("  create_file       - Start interactive file creation");
        System.out.println("  calculate         - Start interactive calculation");
        System.out.println("  build_profile     - Start interactive profile building");
        System.out.println("  quit              - Exit chat");
        System.out.println("\n💡 How it works:");
        System.out.println("  1. Each tool can work across multiple conversations");
        System.out.println("  2. The server will ask for missing parameters");
        System.out.println("  3. Sessions are tracked automatically");
        System.out.println("  4. Files and profiles are saved when complete");
        System.out.println();
    }
    
    private void showTools() throws McpClientException {
        List<ToolInfo> tools = client.listTools();
        System.out.println("\n📋 Available Tools:");
        for (ToolInfo tool : tools) {
            System.out.println("  • " + tool.getName());
            System.out.println("    " + tool.getDescription());
        }
        System.out.println();
    }
    
    private void showSessions() throws McpClientException {
        String response = client.callTool("list_sessions", new HashMap<>());
        System.out.println("\n📊 Active Sessions:");
        System.out.println(response);
        System.out.println();
    }
    
    private void startFileCreation() throws McpClientException, IOException {
        System.out.println("\n🗂️  Starting File Creation");
        System.out.println("-" .repeat(30));
        
        // Start with no parameters
        String response = client.callTool("create_file", new HashMap<>());
        System.out.println("🤖 Server: " + response);
        
        // Extract session ID
        String sessionId = extractSessionId(response);
        if (sessionId == null) {
            return;
        }
        
        System.out.println("🔍 Session ID: " + sessionId);
        
        // Get file path
        System.out.print("📁 Enter file path (e.g., /tmp/my_file.txt): ");
        String filePath = reader.readLine().trim();
        if (!filePath.isEmpty()) {
            Map<String, Object> args = new HashMap<>();
            args.put("session_id", sessionId);
            args.put("file_path", filePath);
            
            response = client.callTool("create_file", args);
            System.out.println("🤖 Server: " + response);
            
            // Get content if still needed
            if (response.contains("content")) {
                System.out.println("📝 Enter file content (press Enter twice when done):");
                StringBuilder contentBuilder = new StringBuilder();
                String line;
                boolean hasContent = false;
                
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() && hasContent && contentBuilder.toString().endsWith("\n")) {
                        break;
                    }
                    if (!line.isEmpty() || hasContent) {
                        contentBuilder.append(line).append("\n");
                        hasContent = true;
                    }
                }
                
                String content = contentBuilder.toString().trim();
                if (!content.isEmpty()) {
                    args = new HashMap<>();
                    args.put("session_id", sessionId);
                    args.put("content", content);
                    
                    response = client.callTool("create_file", args);
                    System.out.println("🤖 Server: " + response);
                }
            }
        }
        System.out.println();
    }
    
    private void startCalculation() throws McpClientException, IOException {
        System.out.println("\n🧮 Starting Calculation");
        System.out.println("-" .repeat(30));
        
        // Start calculation
        String response = client.callTool("calculate", new HashMap<>());
        System.out.println("🤖 Server: " + response);
        
        // Extract session ID
        String sessionId = extractSessionId(response);
        if (sessionId == null) {
            return;
        }
        
        System.out.println("🔍 Session ID: " + sessionId);
        
        // Get expression
        System.out.print("🔢 Enter mathematical expression: ");
        String expression = reader.readLine().trim();
        if (!expression.isEmpty()) {
            // Ask for format
            System.out.print("📊 Choose format (decimal/fraction/scientific) [decimal]: ");
            String formatChoice = reader.readLine().trim();
            if (formatChoice.isEmpty()) {
                formatChoice = "decimal";
            }
            
            Map<String, Object> args = new HashMap<>();
            args.put("session_id", sessionId);
            args.put("expression", expression);
            args.put("format", formatChoice);
            
            response = client.callTool("calculate", args);
            System.out.println("🤖 Server: " + response);
        }
        System.out.println();
    }
    
    private void startProfileBuilding() throws McpClientException, IOException {
        System.out.println("\n👤 Starting Profile Building");
        System.out.println("-" .repeat(30));
        
        // Get name first
        System.out.print("👤 Enter name: ");
        String name = reader.readLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Name is required");
            return;
        }
        
        // Start profile
        Map<String, Object> args = new HashMap<>();
        args.put("name", name);
        
        String response = client.callTool("build_profile", args);
        System.out.println("🤖 Server: " + response);
        
        // Extract session ID
        String sessionId = extractSessionId(response);
        if (sessionId == null) {
            return;
        }
        
        System.out.println("🔍 Session ID: " + sessionId);
        
        // Get email
        if (response.contains("email")) {
            System.out.print("📧 Enter email: ");
            String email = reader.readLine().trim();
            if (!email.isEmpty()) {
                args = new HashMap<>();
                args.put("session_id", sessionId);
                args.put("email", email);
                
                response = client.callTool("build_profile", args);
                System.out.println("🤖 Server: " + response);
                
                // Get age
                if (response.contains("age")) {
                    try {
                        System.out.print("🎂 Enter age: ");
                        int age = Integer.parseInt(reader.readLine().trim());
                        
                        args = new HashMap<>();
                        args.put("session_id", sessionId);
                        args.put("age", age);
                        
                        response = client.callTool("build_profile", args);
                        System.out.println("🤖 Server: " + response);
                        
                        // Get preferences
                        if (response.contains("preferences")) {
                            System.out.print("🎯 Enter preferences (comma-separated): ");
                            String preferences = reader.readLine().trim();
                            System.out.print("💾 Save to file? (y/n) [y]: ");
                            String saveChoice = reader.readLine().trim().toLowerCase();
                            boolean saveFile = !saveChoice.equals("n");
                            
                            if (!preferences.isEmpty()) {
                                args = new HashMap<>();
                                args.put("session_id", sessionId);
                                args.put("preferences", preferences);
                                args.put("save_to_file", saveFile);
                                
                                response = client.callTool("build_profile", args);
                                System.out.println("🤖 Server: " + response);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Age must be a number");
                    }
                }
            }
        }
        System.out.println();
    }
    
    private String extractSessionId(String response) {
        Matcher matcher = SESSION_ID_PATTERN.matcher(response);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * Main method to run the interactive client.
     */
    public static void main(String[] args) {
        String serverUrl = DEFAULT_SERVER_URL;
        if (args.length > 0) {
            serverUrl = args[0];
        }
        
        InteractiveMcpClient client = new InteractiveMcpClient(serverUrl);
        client.start();
    }
    
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080/mcp";
}