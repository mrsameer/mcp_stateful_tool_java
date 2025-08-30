# 🤖 MCP Stateful Tool Java

A **Spring AI MCP** server demonstrating **stateful multi-turn conversations** in Java. This server maintains conversation state across multiple tool calls, allowing complex operations to be broken down into manageable, interactive steps.

## 🎯 What This Demonstrates

- **Multi-turn Conversations**: Tools that collect parameters across multiple interactions
- **Session State Management**: Persistent conversation state with unique session IDs
- **Progressive Parameter Collection**: Ask for missing parameters step-by-step
- **Spring AI MCP Integration**: Full Spring AI MCP-compliant server with official Spring support
- **Enterprise Java Patterns**: Clean architecture with Spring Boot
- **MCP Protocol Compliance**: Full Model Context Protocol specification support

## 🚀 Spring AI MCP Features

### Official Spring Integration
- **Spring AI MCP Server Starter**: Official MCP server implementation
- **Spring AI MCP WebMVC Starter**: WebMVC integration for HTTP-based MCP
- **Spring AI MCP Core**: Core MCP functionality and utilities
- **Spring AI MCP Autoconfiguration**: Automatic MCP server setup

### MCP Protocol Compliance
- **Protocol Version**: 2024-11-05 (latest MCP specification)
- **Tool Discovery**: Automatic tool registration and listing
- **Schema Generation**: Automatic input schema creation
- **Error Handling**: Standard MCP error responses
- **Session Management**: Built-in conversation state handling

### Enhanced Tool Management
- **Automatic Registration**: Tools are automatically discovered and registered
- **Schema Validation**: Input schemas are automatically generated
- **Type Safety**: Compile-time parameter validation
- **Extensibility**: Easy to add new tools and features

## 🏗️ Architecture

```
┌─────────────────────┐    MCP Protocol     ┌─────────────────────┐
│                     │   (Spring AI MCP)   │                     │
│    MCP Client       │◄──────────────────►│   Spring Boot       │
│                     │                     │   MCP Server        │
│ - Python Client     │                     │                     │
│ - Java Client       │                     │ - Spring AI MCP     │
│ - Interactive UI    │                     │ - Tool Registry     │
│ - Session Tracker   │                     │ - Session Manager   │
└─────────────────────┘                     └─────────────────────┘
                                                        │
                                                        ▼
                                            ┌─────────────────────┐
                                            │   Stateful Tools    │
                                            │                     │
                                            │ - Calculator        │
                                            │ - Profile Builder   │
                                            │ - File Creator      │
                                            │ - Session Manager   │
                                            └─────────────────────┘
```

## 🛠️ Available Tools

### 1. `calculate` - Mathematical Calculator
**Purpose**: Perform complex mathematical calculations with progressive parameter collection

**Features**:
- Mathematical expressions (+, -, *, /, ^, sqrt, pow, sin, cos, tan)
- Multiple output formats (decimal, fraction, scientific)
- Progressive parameter collection
- Error handling with retry support

**Example Multi-turn Flow**:
```bash
# Turn 1: Start calculation
{"name": "calculate", "arguments": {}}
# Response: "I need more information... Missing parameter: expression"

# Turn 2: Provide expression
{"name": "calculate", "arguments": {"expression": "2 + 2"}}
# Response: "Expression: 2 + 2\nResult: 4"
```

### 2. `create_file` - Multi-turn File Creator
**Purpose**: Create files by collecting filename and content across multiple interactions

**Features**:
- Progressive filename and content collection
- File path validation
- Content persistence
- Session-based state management

### 3. `build_profile` - Multi-turn Profile Builder
**Purpose**: Build user profiles by collecting information step-by-step

**Features**:
- Progressive data collection
- JSON profile generation
- Optional file persistence
- Input validation

### 4. `list_sessions` - Session Management
**Purpose**: Show all active conversation sessions

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- Python 3.10+ (for client - optional)

### Installation
```bash
# Clone the project
cd /path/to/parent/directory
git clone <repository>
cd mcp_stateful_tool_java

# Build the project
mvn clean install
```

## ✅ Recent Fixes & Improvements

### JSON-RPC Protocol Compliance
- **Fixed ZodError issues**: All responses now have valid IDs (no more null IDs)
- **Enhanced validation**: Comprehensive request validation for JSON-RPC 2.0 compliance
- **MCP Inspector compatibility**: Full StreamableHttp transport support
- **Error handling**: Proper error responses with valid message structure

### Connection Management  
- **StreamableHttp support**: Compatible with MCP Inspector's StreamableHttp transport
- **Connection lifecycle**: Proper request/response cycles with connection closure
- **Protocol flow**: Removed unsolicited messages, follows proper MCP handshake
- **Session management**: Enhanced session handling for multi-turn conversations

### Usage

#### 1. Start the Spring Boot MCP Server
```bash
mvn spring-boot:run
```

The server will start with Spring AI MCP integration enabled:
```
🚀 Spring AI MCP Server configured with 4 tools:
  • calculate: Perform mathematical calculations...
  • create_file: Create a file with specified content...
  • build_profile: Build user profiles progressively...
  • list_sessions: List active tool sessions...

📋 MCP Protocol Features:
   - Protocol version: 2024-11-05
   - Tool registration: 4 tools
   - Stateful conversations: Supported
   - Session management: Active
   - Multi-turn execution: Enabled
```

#### 2. Connect with MCP Inspector

**Option A: MCP Inspector (Recommended)**
```bash
# Start MCP Inspector in a separate terminal
npx @modelcontextprotocol/inspector

# Connect to: http://localhost:8080/mcp/stream?clientId=inspector
# The server is now fully compatible with MCP Inspector's StreamableHttp transport
```

**Option B: Java Client**
```bash
# Interactive Java client
./scripts/run-client.sh
# Choose option 1 for interactive client

# Or automated demo client
./scripts/run-client.sh
# Choose option 2 for automated demos
```

**Option C: Python Client**
Since this is a Spring AI MCP server, you can also use the Python client from the sibling project:

```bash
# In another terminal
cd ../mcp_stateful_tool
uv run python interactive_client.py
```

**Option D: Direct Maven Execution**
```bash
# Interactive client
mvn exec:java -Dexec.mainClass="com.example.mcpstateful.client.InteractiveMcpClient"

# Test client
mvn exec:java -Dexec.mainClass="com.example.mcpstateful.client.StatefulMcpTestClient"
```

## 💻 Spring AI MCP Integration

### Tool Registration
```java
@Component
public class CalculatorTool extends StatefulToolBase implements FunctionCallback {
    @Override
    public String getName() {
        return "calculate";
    }
    
    @Override
    public String getDescription() {
        return "Perform mathematical calculations...";
    }
    
    // Tool implementation
}
```

### MCP Server Configuration
```java
@Configuration
public class SpringAiMcpConfig {
    @Bean
    @Primary
    public Object mcpServer() {
        // Spring AI MCP autoconfiguration handles everything
        return new Object();
    }
}
```

### Session Management
```java
@Component
public class SessionManager {
    private final Map<String, ToolSession> sessions = new ConcurrentHashMap<>();
    
    public ToolSession createSession(String sessionId, String toolName, 
                                   Map<String, String> requiredParams) {
        // Create and manage sessions
    }
}
```

## 🔄 Multi-turn Conversation Example

```java
// Turn 1: Start file creation
Map<String, Object> args1 = Map.of();
String result1 = createFileTool.execute(args1);
// Result: "I need more information... Missing parameter: **file_path**"

// Turn 2: Provide file path
Map<String, Object> args2 = Map.of(
    "session_id", "abc-123-def-456",
    "file_path", "/tmp/java_demo.txt"
);
String result2 = createFileTool.execute(args2);
// Result: "I need more information... Missing parameter: **content**"

// Turn 3: Complete with content
Map<String, Object> args3 = Map.of(
    "session_id", "abc-123-def-456", 
    "content", "Hello from Spring AI MCP!"
);
String result3 = createFileTool.execute(args3);
// Result: "Successfully created file: /tmp/java_demo.txt"
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=McpStatefulToolApplicationTests#testCreateFileToolFlow

# Run with coverage
mvn test jacoco:report
```

### Test Coverage
- ✅ Session management lifecycle
- ✅ Multi-turn tool execution
- ✅ Parameter validation
- ✅ Error handling
- ✅ Session cleanup
- ✅ Spring AI MCP integration

## 🔧 Configuration

### Application Properties
```yaml
# application.yml
spring:
  application:
    name: mcp-stateful-server-java

logging:
  level:
    com.example.mcpstateful: INFO
    org.springframework.ai: DEBUG
```

### Spring AI MCP Configuration
The server automatically configures:
- MCP protocol endpoints
- Tool discovery and registration
- Input schema generation
- Error handling
- Session management

## 🔮 Advanced Features

### Custom Tool Implementation
```java
@Component
public class CustomTool extends StatefulToolBase implements FunctionCallback {
    
    @Override
    public Map<String, String> getRequiredParameters() {
        return Map.of(
            "param1", "Description of param1",
            "param2", "Description of param2"
        );
    }
    
    @Override
    public String execute(Map<String, Object> arguments) {
        // Your custom multi-turn logic
        ToolSession session = getOrCreateSession(arguments, getToolName(), getRequiredParameters());
        // ... implement your logic
    }
}
```

### Session Persistence
```java
@Component
public class PersistentSessionManager extends SessionManager {
    @Autowired
    private SessionRepository sessionRepository;
    
    // Implement database persistence
}
```

### Custom Transport
```java
@Bean
public McpServer mcpServer() {
    return McpServer.builder()
        .transport(new HttpServerTransport(8080))  // HTTP instead of stdio
        .tool(createMcpTool(createFileTool))
        .build();
}
```

## 🎯 Key Benefits of Java Version

- **Enterprise Ready**: Spring Boot ecosystem integration
- **Type Safety**: Compile-time parameter validation
- **Scalability**: Built-in connection pooling and threading
- **Monitoring**: Spring Actuator endpoints for health checks
- **Testing**: Comprehensive testing framework integration
- **IDE Support**: Full IntelliJ IDEA / Eclipse support
- **Spring AI MCP**: Official Spring MCP integration
- **Protocol Compliance**: Full MCP specification support

## 🔧 Troubleshooting

### Common Issues

**ZodError: Expected string, received null**
- ✅ **Fixed**: All JSON-RPC responses now generate valid IDs
- ✅ **Fixed**: Added comprehensive validation for required fields
- ✅ **Fixed**: Proper error handling with valid message structure

**MCP Inspector Connection Issues**
- ✅ **Fixed**: StreamableHttp transport compatibility 
- ✅ **Fixed**: Proper request/response cycles with connection closure
- ✅ **Fixed**: Removed unsolicited initialization messages

**"No connection established for request ID" Error**
- ✅ **Fixed**: Enhanced connection lifecycle management
- ✅ **Fixed**: Proper MCP protocol handshake implementation

### Server Status Check
```bash
# Check if server is running
curl http://localhost:8080/mcp/health

# Test MCP protocol compliance
curl -X POST "http://localhost:8080/mcp/stream?clientId=test" \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05"}}'
```

## 📚 Documentation

- [Spring AI MCP Integration Guide](SPRING_AI_MCP_INTEGRATION.md) - Detailed Spring AI MCP integration
- [MCP Inspector Connection Guide](MCP_INSPECTOR_README.md) - MCP Inspector setup and usage
- [Java Client README](JAVA_CLIENT_README.md) - Java client usage and examples
- [Setup Guide](SETUP.md) - Project setup and configuration

## 📜 License

MIT License - See LICENSE file for details

---

**Built with ❤️ using Spring AI MCP to demonstrate enterprise-grade stateful conversational tools with full MCP protocol compliance**