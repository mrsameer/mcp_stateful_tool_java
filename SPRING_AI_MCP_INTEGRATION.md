# Spring AI MCP Integration Guide

This guide explains how Spring AI MCP (Model Context Protocol) is integrated into the MCP Stateful Tool Java server and how to leverage it for better MCP protocol compliance.

## 🚀 What is Spring AI MCP?

Spring AI MCP is Spring's official integration with the Model Context Protocol, providing:

- **Official MCP Protocol Support**: Full compliance with MCP specification
- **Automatic Tool Discovery**: Spring Boot autoconfiguration for MCP servers
- **Tool Registry Management**: Centralized tool registration and management
- **Protocol Compliance**: Built-in MCP protocol handling
- **Client Integration**: Spring AI MCP client support

## 🔧 Current Integration

### Dependencies Added

The project now includes these Spring AI MCP dependencies:

```xml
<!-- Spring AI MCP Server Starter -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring AI MCP Server WebMVC Starter -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring AI MCP Core -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring AI Autoconfigure MCP Server -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-mcp-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration Classes

#### 1. SpringAiMcpConfig
- **Purpose**: Main Spring AI MCP server configuration
- **Features**: 
  - Tool registration with Spring AI MCP
  - MCP protocol compliance
  - Automatic tool discovery
- **Location**: `src/main/java/com/example/mcpstateful/config/SpringAiMcpConfig.java`

#### 2. SpringAiMcpClientConfig
- **Purpose**: Spring AI MCP client configuration
- **Features**:
  - MCP client autoconfiguration
  - Protocol version support
  - Client-side MCP integration
- **Location**: `src/main/java/com/example/mcpstateful/config/SpringAiMcpClientConfig.java`

## 🛠️ How It Works

### 1. Tool Registration
Spring AI MCP automatically discovers and registers tools that implement `FunctionCallback`:

```java
@Service
public class CalculatorTool extends StatefulToolBase implements FunctionCallback {
    @Override
    public String getName() {
        return "calculate";
    }
    
    @Override
    public String getDescription() {
        return "Perform mathematical calculations...";
    }
    
    // ... other methods
}
```

### 2. MCP Protocol Compliance
The Spring AI MCP integration provides:

- **Tool Listing**: Automatic `/tools/list` endpoint
- **Tool Execution**: Automatic `/tools/call` endpoint
- **Protocol Versioning**: MCP protocol 2024-11-05 support
- **Error Handling**: Standard MCP error responses
- **Schema Validation**: Automatic input schema generation

### 3. Session Management
Stateful tools maintain conversation state through:

- **Session IDs**: Unique identifiers for multi-turn conversations
- **Parameter Collection**: Progressive parameter gathering
- **State Persistence**: Conversation state across tool calls
- **Cleanup**: Automatic session cleanup on completion

## 🔄 MCP Protocol Flow

### 1. Initialization
```
Client → POST /mcp/initialize
Server → Returns protocol version, capabilities, server info
```

### 2. Tool Discovery
```
Client → POST /mcp/tools/list
Server → Returns registered tools with schemas
```

### 3. Tool Execution
```
Client → POST /mcp/tools/call
Server → Executes tool, returns result or parameter request
```

### 4. Multi-turn Conversation
```
Turn 1: Client calls tool with minimal parameters
Turn 2: Server requests missing parameters
Turn 3: Client provides missing parameters
Turn 4: Server executes tool and returns result
```

## 📡 Available Endpoints

### REST Endpoints
- `POST /mcp/initialize` - Initialize MCP connection
- `POST /mcp/tools/list` - List available tools
- `POST /mcp/tools/call` - Execute a tool
- `GET /mcp/health` - Health check

### Streaming Endpoints
- `WebSocket: /mcp/ws` - WebSocket MCP communication
- `SSE: /mcp/stream?clientId=<id>` - Server-Sent Events
- `Reactive: /mcp/reactive/tools/stream` - Reactive streams

## 🧪 Testing Spring AI MCP

### 1. Start the Server
```bash
mvn spring-boot:run
```

### 2. Test MCP Protocol
```bash
# Test initialization
curl -X POST http://localhost:8080/mcp/initialize \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "jsonrpc": "2.0", "method": "initialize"}'

# Test tool listing
curl -X POST http://localhost:8080/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{"id": 2, "jsonrpc": "2.0", "method": "tools/list"}'

# Test tool execution
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "id": 3,
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "calculate",
      "arguments": {"expression": "2 + 2"}
    }
  }'
```

### 3. Use MCP Inspector
1. Open `http://localhost:8080/mcp-inspector-test.html`
2. Connect to WebSocket: `ws://localhost:8080/mcp/ws`
3. Test tools interactively

## 🚀 Advanced Features

### 1. Custom Tool Implementation
```java
@Component
public class CustomTool extends StatefulToolBase implements FunctionCallback {
    
    @Override
    public String getName() {
        return "custom_tool";
    }
    
    @Override
    public String getDescription() {
        return "Your custom tool description";
    }
    
    @Override
    public String getToolName() {
        return "custom_tool";
    }
    
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
        return "Result";
    }
}
```

### 2. Session Persistence
```java
@Component
public class PersistentSessionManager extends SessionManager {
    @Autowired
    private SessionRepository sessionRepository;
    
    // Implement database persistence
    public void saveSession(ToolSession session) {
        sessionRepository.save(session);
    }
}
```

### 3. Custom Transport
```java
@Bean
public McpServer mcpServer() {
    return McpServer.builder()
        .transport(new HttpServerTransport(8080))  // HTTP instead of stdio
        .tool(createMcpTool(createFileTool))
        .build();
}
```

## 🔮 Future Enhancements

### 1. Enhanced Tool Schemas
- JSON Schema validation
- Type checking
- Parameter constraints
- Default values

### 2. Resource Management
- File system access
- Database connections
- API integrations
- Streaming responses

### 3. Prompt Management
- Template-based prompts
- Dynamic prompt generation
- Context-aware responses
- Multi-language support

### 4. Monitoring & Observability
- Tool execution metrics
- Performance monitoring
- Error tracking
- Usage analytics

## 📚 Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [Spring AI MCP Examples](https://github.com/spring-projects/spring-ai)
- [MCP SDK Documentation](https://github.com/modelcontextprotocol/sdk-java)

## 🎯 Benefits of Spring AI MCP Integration

1. **Official Support**: Spring-maintained MCP integration
2. **Protocol Compliance**: Full MCP specification support
3. **Tool Discovery**: Automatic tool registration and discovery
4. **Session Management**: Built-in conversation state handling
5. **Error Handling**: Standard MCP error responses
6. **Schema Generation**: Automatic input schema creation
7. **Client Support**: Spring AI MCP client integration
8. **Extensibility**: Easy to add new tools and features

---

**Built with ❤️ using Spring AI MCP for enterprise-grade MCP protocol compliance**
