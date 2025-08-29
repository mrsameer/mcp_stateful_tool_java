package com.example.mcpstateful.client;

import com.example.mcpstateful.client.McpClient.ToolInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test client for demonstrating MCP stateful server functionality.
 * 
 * This class replicates the Python test_client.py functionality,
 * running automated demonstrations of multi-turn tool interactions.
 */
public class StatefulMcpTestClient {
    
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("Session ID: `([^`]+)`");
    
    private final McpClient client;
    private final Map<String, String> activeSessions;
    
    /**
     * Create a test client.
     */
    public StatefulMcpTestClient() {
        this.client = new McpClient();
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Create a test client with custom server URL.
     */
    public StatefulMcpTestClient(String serverUrl) {
        this.client = new McpClient(serverUrl);
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Connect to the MCP server.
     */
    public void connect() throws McpClientException {
        client.connect();
        System.out.println("✅ Connected to MCP stateful server");
    }
    
    /**
     * List available tools.
     */
    public List<ToolInfo> listTools() throws McpClientException {
        return client.listTools();
    }
    
    /**
     * Call a tool and return the response text.
     */
    public String callTool(String name, Map<String, Object> arguments) throws McpClientException {
        if (arguments == null) {
            arguments = new HashMap<>();
        }
        return client.callTool(name, arguments);
    }
    
    /**
     * Interactive demonstration of multi-turn file creation.
     */
    public void interactiveFileCreation() throws McpClientException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🗂️  INTERACTIVE FILE CREATION DEMO");
        System.out.println("=".repeat(50));
        
        // Step 1: Start without parameters
        System.out.println("\n1️⃣  Starting file creation with no parameters...");
        String response = callTool("create_file", new HashMap<>());
        System.out.println("📝 Server response:\n" + response);
        
        // Extract session ID from response
        String sessionId = extractSessionId(response);
        if (sessionId == null) {
            System.out.println("❌ Could not extract session ID");
            return;
        }
        
        System.out.println("🔍 Extracted session ID: " + sessionId);
        
        // Step 2: Provide file path
        System.out.println("\n2️⃣  Providing file path...");
        Map<String, Object> args = new HashMap<>();
        args.put("session_id", sessionId);
        args.put("file_path", "/tmp/mcp_test_file.txt");
        
        response = callTool("create_file", args);
        System.out.println("📝 Server response:\n" + response);
        
        // Step 3: Provide content to complete
        System.out.println("\n3️⃣  Providing content to complete file creation...");
        args = new HashMap<>();
        args.put("session_id", sessionId);
        args.put("content", "Hello from Java MCP Client!\n\n" +
            "This file was created through a stateful multi-turn conversation:\n" +
            "- Turn 1: Started with no parameters\n" +
            "- Turn 2: Added file path\n" +
            "- Turn 3: Added content\n\n" +
            "Mission accomplished! 🎉");
        
        response = callTool("create_file", args);
        System.out.println("📝 Server response:\n" + response);
        
        // Verify file was created
        Path filePath = Path.of("/tmp/mcp_test_file.txt");
        if (Files.exists(filePath)) {
            System.out.println("\n✅ File verification: SUCCESS");
            try {
                String content = Files.readString(filePath);
                System.out.println("📄 File contents:\n" + "-".repeat(30));
                System.out.println(content);
                System.out.println("-".repeat(30));
            } catch (IOException e) {
                System.out.println("❌ Error reading file: " + e.getMessage());
            }
        } else {
            System.out.println("\n❌ File verification: FAILED");
        }
    }
    
    /**
     * Interactive demonstration of multi-turn calculation.
     */
    public void interactiveCalculation() throws McpClientException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🧮 INTERACTIVE CALCULATION DEMO");
        System.out.println("=".repeat(50));
        
        // Step 1: Start calculation without expression
        System.out.println("\n1️⃣  Starting calculation without expression...");
        String response = callTool("calculate", new HashMap<>());
        System.out.println("📝 Server response:\n" + response);
        
        // Extract session ID
        String sessionId = extractSessionId(response);
        if (sessionId == null) {
            System.out.println("❌ Could not extract session ID");
            return;
        }
        
        System.out.println("🔍 Extracted session ID: " + sessionId);
        
        // Step 2: Provide mathematical expression
        System.out.println("\n2️⃣  Providing mathematical expression...");
        Map<String, Object> args = new HashMap<>();
        args.put("session_id", sessionId);
        args.put("expression", "sqrt(144) + pow(2, 4) * 3 - 10");
        
        response = callTool("calculate", args);
        System.out.println("📝 Server response:\n" + response);
        
        // Step 3: Another calculation with formatting
        System.out.println("\n3️⃣  New calculation with fraction format...");
        args = new HashMap<>();
        args.put("expression", "355/113");  // Approximation of π
        args.put("format", "fraction");
        
        response = callTool("calculate", args);
        System.out.println("📝 Server response:\n" + response);
    }
    
    /**
     * Interactive demonstration of multi-turn profile building.
     */
    public void interactiveProfileBuilding() throws McpClientException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("👤 INTERACTIVE PROFILE BUILDING DEMO");
        System.out.println("=".repeat(50));
        
        // Step 1: Start with just name
        System.out.println("\n1️⃣  Starting profile with just name...");
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Alex Rivera");
        
        String response = callTool("build_profile", args);
        System.out.println("📝 Server response:\n" + response);
        
        // Extract session ID
        String sessionId = extractSessionId(response);
        if (sessionId == null) {
            System.out.println("❌ Could not extract session ID");
            return;
        }
        
        System.out.println("🔍 Extracted session ID: " + sessionId);
        
        // Step 2: Add email
        System.out.println("\n2️⃣  Adding email address...");
        args = new HashMap<>();
        args.put("session_id", sessionId);
        args.put("email", "alex.rivera@techcorp.com");
        
        response = callTool("build_profile", args);
        System.out.println("📝 Server response:\n" + response);
        
        // Step 3: Add age
        System.out.println("\n3️⃣  Adding age...");
        args = new HashMap<>();
        args.put("session_id", sessionId);
        args.put("age", 29);
        
        response = callTool("build_profile", args);
        System.out.println("📝 Server response:\n" + response);
        
        // Step 4: Complete with preferences
        System.out.println("\n4️⃣  Completing with preferences and saving...");
        args = new HashMap<>();
        args.put("session_id", sessionId);
        args.put("preferences", "software engineering, rock climbing, coffee brewing, drone photography");
        args.put("save_to_file", true);
        
        response = callTool("build_profile", args);
        System.out.println("📝 Server response:\n" + response);
    }
    
    /**
     * Test session management features.
     */
    public void testSessionManagement() throws McpClientException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔧 SESSION MANAGEMENT DEMO");
        System.out.println("=".repeat(50));
        
        // Start multiple incomplete sessions
        System.out.println("\n1️⃣  Starting multiple incomplete sessions...");
        
        // Start file creation
        Map<String, Object> args = new HashMap<>();
        args.put("file_path", "/tmp/incomplete1.txt");
        callTool("create_file", args);
        
        // Start calculation
        callTool("calculate", new HashMap<>());
        
        // Start profile
        args = new HashMap<>();
        args.put("name", "Incomplete User");
        callTool("build_profile", args);
        
        System.out.println("✅ Started 3 incomplete sessions");
        
        // List active sessions
        System.out.println("\n2️⃣  Listing active sessions...");
        String response = callTool("list_sessions", new HashMap<>());
        System.out.println("📝 Active sessions:\n" + response);
    }
    
    /**
     * Close the client connection.
     */
    public void close() {
        client.close();
    }
    
    /**
     * Run the complete demonstration.
     */
    public void runCompleteDemo() {
        try {
            connect();
            
            // List available tools
            System.out.println("\n📋 Available tools:");
            List<ToolInfo> tools = listTools();
            for (ToolInfo tool : tools) {
                System.out.println("  • " + tool.getName() + ": " + tool.getDescription());
            }
            
            // Run interactive demonstrations
            interactiveFileCreation();
            interactiveCalculation();
            interactiveProfileBuilding();
            testSessionManagement();
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🎉 ALL DEMOS COMPLETED SUCCESSFULLY!");
            System.out.println("=".repeat(50));
            System.out.println("\n✅ Key features demonstrated:");
            System.out.println("  • Multi-turn parameter collection");
            System.out.println("  • Session state persistence");
            System.out.println("  • Progressive conversation flow");
            System.out.println("  • Parameter validation and prompting");
            System.out.println("  • Session management and cleanup");
            System.out.println("  • Real MCP protocol communication");
            
        } catch (Exception e) {
            System.err.println("\n❌ Demo failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }
    
    private String extractSessionId(String response) {
        Matcher matcher = SESSION_ID_PATTERN.matcher(response);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * Main method to run the MCP client demo.
     */
    public static void main(String[] args) {
        System.out.println("🚀 MCP Stateful Server Java Client Demo");
        System.out.println("=" .repeat(50));
        
        String serverUrl = "http://localhost:8080/mcp";
        if (args.length > 0) {
            serverUrl = args[0];
        }
        
        StatefulMcpTestClient client = new StatefulMcpTestClient(serverUrl);
        client.runCompleteDemo();
    }
}