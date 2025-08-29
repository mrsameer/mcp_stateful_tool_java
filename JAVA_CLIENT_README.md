# Java MCP Client

This directory contains a Java implementation of an MCP (Model Context Protocol) client that mirrors the functionality of the Python `interactive_client.py` and `test_client.py`.

## 🏗️ Architecture

The Java client consists of several key components:

```
src/main/java/com/example/mcpstateful/client/
├── McpClient.java                 # Core MCP client with HTTP communication
├── McpClientException.java        # Exception handling for client operations
├── InteractiveMcpClient.java      # Interactive CLI client (like Python interactive_client.py)
└── StatefulMcpTestClient.java     # Automated test client (like Python test_client.py)
```

## 🚀 Quick Start

### 1. Start the Server
First, start the MCP server:
```bash
mvn spring-boot:run
```

### 2. Run the Java Client

#### Option A: Interactive Client
```bash
# Compile and run interactive client
./run-client.sh
# Choose option 1

# Or directly with Maven
mvn exec:java -Dexec.mainClass="com.example.mcpstateful.client.InteractiveMcpClient"
```

#### Option B: Automated Test Client
```bash
# Compile and run test client
./run-client.sh
# Choose option 2

# Or directly with Maven
mvn exec:java -Dexec.mainClass="com.example.mcpstateful.client.StatefulMcpTestClient"
```

## 📋 Available Clients

### 1. InteractiveMcpClient
An interactive command-line interface that provides:

- **Commands**:
  - `help` - Show available tools and commands
  - `tools` - List all available tools
  - `sessions` - Show active sessions
  - `create_file` - Start file creation workflow
  - `calculate` - Start calculation workflow
  - `build_profile` - Start profile building workflow
  - `quit` - Exit the client

- **Multi-turn Conversations**: Each tool can collect parameters across multiple interactions
- **Session Management**: Automatic session tracking and state management
- **User-friendly Interface**: Emojis and clear prompts for better UX

### 2. StatefulMcpTestClient
An automated demonstration client that:

- **Runs Complete Demos**: Automatically demonstrates all MCP tools
- **Tests Multi-turn Flows**: Shows parameter collection across multiple turns
- **Validates Results**: Checks file creation, calculations, and profile building
- **Session Management**: Demonstrates session tracking and cleanup

### 3. McpClient (Base Class)
The core client providing:

- **HTTP Communication**: REST API communication with MCP server
- **Tool Discovery**: List available tools and their schemas
- **Tool Execution**: Call tools with arguments and handle responses
- **Error Handling**: Comprehensive exception handling
- **Connection Management**: Initialize, connect, and cleanup

## 🔄 Comparison with Python Client

| Feature | Python Client | Java Client |
|---------|---------------|-------------|
| **Protocol** | MCP over stdio | MCP over HTTP |
| **Dependencies** | `mcp` library | Spring Boot RestTemplate |
| **Interactive UI** | Terminal-based | Terminal-based |
| **Multi-turn** | ✅ Supported | ✅ Supported |
| **Session Tracking** | ✅ Automatic | ✅ Automatic |
| **Error Handling** | Basic exceptions | Typed exceptions |
| **Type Safety** | Runtime | Compile-time |

## 💻 Usage Examples

### Interactive File Creation
```java
// Start file creation
client.callTool("create_file", new HashMap<>());

// Add file path
Map<String, Object> args = Map.of(
    "session_id", sessionId,
    "file_path", "/tmp/my_file.txt"
);
client.callTool("create_file", args);

// Add content
args = Map.of(
    "session_id", sessionId,
    "content", "Hello from Java!"
);
client.callTool("create_file", args);
```

### Mathematical Calculations
```java
// Start calculation
client.callTool("calculate", new HashMap<>());

// Provide expression
Map<String, Object> args = Map.of(
    "session_id", sessionId,
    "expression", "sqrt(144) + pow(2, 4) * 3 - 10",
    "format", "decimal"
);
client.callTool("calculate", args);
```

### Profile Building
```java
// Start with name
Map<String, Object> args = Map.of("name", "John Doe");
client.callTool("build_profile", args);

// Add email progressively
args = Map.of(
    "session_id", sessionId,
    "email", "john@example.com"
);
client.callTool("build_profile", args);

// Complete with preferences
args = Map.of(
    "session_id", sessionId,
    "preferences", "coding, music, travel",
    "save_to_file", true
);
client.callTool("build_profile", args);
```

## 🔧 Configuration

### Server URL
The client connects to `http://localhost:8080/mcp` by default. You can customize this:

```java
// Custom server URL
McpClient client = new McpClient("http://custom-server:8080/mcp");
```

### Timeout and Error Handling
The client uses Spring's RestTemplate with default timeouts. For production use, consider:

```java
RestTemplate restTemplate = new RestTemplate();
restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
// Configure timeouts, connection pooling, etc.
```

## 🧪 Testing

The automated test client demonstrates:
- ✅ Multi-turn file creation workflow
- ✅ Mathematical expression evaluation
- ✅ Progressive profile building
- ✅ Session management and tracking
- ✅ Parameter validation and error handling

## 🎯 Key Benefits of Java Implementation

1. **Type Safety**: Compile-time checking of method calls and parameters
2. **Enterprise Ready**: Built on Spring Boot ecosystem
3. **IDE Support**: Full IntelliJ IDEA / Eclipse integration
4. **Performance**: JVM optimization and efficient memory management
5. **Maintainability**: Strong typing and clear interfaces
6. **Scalability**: Built-in connection pooling and thread safety

## 🔮 Future Enhancements

Potential improvements for production use:
- Connection pooling configuration
- Retry mechanisms and circuit breakers
- Metrics and monitoring integration
- Configuration externalization
- SSL/TLS support for secure communication
- Authentication and authorization

---

**Built with ❤️ to demonstrate Java MCP client capabilities alongside the Python implementation**