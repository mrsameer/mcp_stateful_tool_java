# 🤖 MCP Stateful Tool Java

A **Spring AI MCP** server demonstrating **stateful multi-turn conversations** in Java. This server maintains conversation state across multiple tool calls, allowing complex operations to be broken down into manageable, interactive steps.

## 🎯 What This Demonstrates

- **Multi-turn Conversations**: Tools that collect parameters across multiple interactions
- **Session State Management**: Persistent conversation state with unique session IDs
- **Progressive Parameter Collection**: Ask for missing parameters step-by-step
- **Spring AI MCP Integration**: Full Spring AI MCP-compliant server
- **Enterprise Java Patterns**: Clean architecture with Spring Boot

## 🏗️ Architecture

```
┌─────────────────────┐    MCP Protocol     ┌─────────────────────┐
│                     │   (Spring AI MCP)   │                     │
│    MCP Client       │◄──────────────────►│   Spring Boot       │
│                     │                     │   MCP Server        │
│ - Python Client     │                     │                     │
│ - Java Client       │                     │ - Tool Registry     │
│ - Interactive UI    │                     │ - Session Manager   │
│ - Session Tracker   │                     │ - State Persistence │
└─────────────────────┘                     └─────────────────────┘
                                                        │
                                                        ▼
                                            ┌─────────────────────┐
                                            │   Spring Components │
                                            │                     │
                                            │ • @McpTool          │
                                            │ • SessionManager    │
                                            │ • StatefulToolBase  │
                                            │ • Configuration     │
                                            └─────────────────────┘
```

## 📁 Project Structure

```
mcp_stateful_tool_java/
├── 📄 README.md                           # This file
├── 📄 JAVA_CLIENT_README.md               # Java client documentation  
├── 📄 pom.xml                            # Maven dependencies
├── 📄 run-client.sh                      # Script to run Java clients
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/example/mcpstateful/
│   │   │   ├── 📄 McpStatefulToolApplication.java  # Main application
│   │   │   ├── 📁 client/
│   │   │   │   ├── 📄 McpClient.java              # Core MCP client
│   │   │   │   ├── 📄 McpClientException.java     # Client exceptions
│   │   │   │   ├── 📄 InteractiveMcpClient.java   # Interactive CLI client
│   │   │   │   └── 📄 StatefulMcpTestClient.java  # Automated test client
│   │   │   ├── 📁 config/
│   │   │   │   └── 📄 McpConfig.java              # MCP configuration
│   │   │   ├── 📁 mcp/
│   │   │   │   ├── 📄 McpController.java          # HTTP MCP endpoints
│   │   │   │   ├── 📄 McpRequest.java             # MCP request model
│   │   │   │   ├── 📄 McpResponse.java            # MCP response model
│   │   │   │   └── 📄 McpTool.java                # MCP tool annotation
│   │   │   ├── 📁 state/
│   │   │   │   ├── 📄 ConversationState.java      # Session states
│   │   │   │   ├── 📄 ToolSession.java            # Session data model
│   │   │   │   └── 📄 SessionManager.java         # Session management
│   │   │   └── 📁 tools/
│   │   │       ├── 📄 StatefulToolBase.java       # Base tool class
│   │   │       ├── 📄 CreateFileTool.java         # File creation tool
│   │   │       ├── 📄 CalculatorTool.java         # Calculator tool
│   │   │       ├── 📄 ProfileBuilderTool.java     # Profile builder
│   │   │       └── 📄 SessionListTool.java        # Session listing
│   │   └── 📁 resources/
│   │       └── 📄 application.yml                 # Spring configuration
│   └── 📁 test/
│       └── 📁 java/com/example/mcpstateful/
│           └── 📄 McpStatefulToolApplicationTests.java  # Unit tests
```

## 🛠️ Available Tools

### 1. `create_file` - Multi-turn File Creation
**Purpose**: Create files by collecting path and content progressively

**Flow**:
```
Turn 1: {} → "Need file_path"
Turn 2: {session_id, file_path} → "Need content"  
Turn 3: {session_id, content} → "File created!"
```

**Java Implementation**:
```java
@McpTool(name = "create_file", description = "Create a file with specified content...")
public class CreateFileTool extends StatefulToolBase {
    // Multi-turn parameter collection logic
}
```

### 2. `calculate` - Multi-turn Calculator
**Purpose**: Perform mathematical calculations with expression collection

**Features**:
- JavaScript expression evaluation
- Multiple output formats (decimal, fraction, scientific)
- Mathematical function support (sqrt, pow, sin, cos, etc.)

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
- Python 3.10+ (for client)

### Installation
```bash
# Clone the project
cd /path/to/parent/directory
git clone <repository>
cd mcp_stateful_tool_java

# Build the project
mvn clean install
```

### Usage

#### 1. Start the Spring Boot MCP Server
```bash
mvn spring-boot:run
```

#### 2. Connect with Client

**Option A: Java Client (Recommended)**
```bash
# Interactive Java client (similar to Python interactive_client.py)
./run-client.sh
# Choose option 1 for interactive client

# Or automated demo client (similar to Python test_client.py)
./run-client.sh
# Choose option 2 for automated demos
```

**Option B: Python Client**
Since this is a Spring AI MCP server, you can also use the Python client from the sibling project:

```bash
# In another terminal
cd ../mcp_stateful_tool
uv run python interactive_client.py
```

**Option C: Direct Maven Execution**
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
@McpTool(
    name = "create_file",
    description = "Create a file with specified content. Can collect parameters across multiple interactions."
)
public class CreateFileTool extends StatefulToolBase {
    // Tool implementation
}
```

### MCP Server Configuration
```java
@Configuration
public class McpConfig {
    @Bean
    public McpServer mcpServer(CreateFileTool createFileTool, ...) {
        return McpServer.builder()
            .transport(new StdioServerTransport())
            .tool(createFileTool)
            .serverInfo("mcp-stateful-server-java", "1.0.0")
            .build();
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
    org.springframework.ai.mcp: DEBUG
```

### Session Configuration
```java
@Component
public class SessionManager {
    // Thread-safe concurrent session storage
    private final Map<String, ToolSession> sessions = new ConcurrentHashMap<>();
    
    // UUID-based session ID generation
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
```

## 🔍 Key Java Patterns Used

### 1. **Template Method Pattern**
```java
public abstract class StatefulToolBase {
    // Common session management logic
    protected abstract String execute(Map<String, Object> arguments);
    protected abstract Map<String, String> getRequiredParameters();
}
```

### 2. **Strategy Pattern**
```java
@McpTool(name = "calculate")
public class CalculatorTool extends StatefulToolBase {
    // Specific calculation strategy
}
```

### 3. **Dependency Injection**
```java
@Component
public class CreateFileTool extends StatefulToolBase {
    @Autowired
    protected SessionManager sessionManager;
}
```

### 4. **Builder Pattern**
```java
McpServer.builder()
    .transport(new StdioServerTransport())
    .tool(createFileTool)
    .serverInfo("mcp-stateful-server-java", "1.0.0")
    .build();
```

## 📊 Comparison with Python Version

| Feature | Python Version | Java Version |
|---------|---------------|--------------|
| **Framework** | Raw MCP Protocol | Spring AI MCP |
| **State Management** | In-memory dict | ConcurrentHashMap |
| **Tool Registration** | Manual registry | @McpTool annotations |
| **Dependency Injection** | Manual wiring | Spring IoC |
| **Testing** | pytest | JUnit 5 + AssertJ |
| **Type Safety** | Runtime typing | Compile-time safety |
| **Concurrency** | asyncio | Thread-safe collections |
| **Configuration** | Python files | YAML + annotations |

## 🔮 Advanced Features

### Custom Tool Implementation
```java
@Component
@McpTool(name = "custom_tool", description = "Your custom tool")
public class CustomTool extends StatefulToolBase {
    
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
        .tool(createFileTool)
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

## 📜 License

MIT License - See LICENSE file for details

---

**Built with ❤️ using Spring AI MCP to demonstrate enterprise-grade stateful conversational tools**