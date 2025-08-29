# MCP Inspector Connection Guide

This guide explains how to connect the Model Context Protocol (MCP) Inspector to your streaming MCP server.

## 🚀 Quick Start

1. **Start the server:**
   ```bash
   ./run.sh
   ```

2. **Open the test page:**
   ```
   http://localhost:8080/mcp-inspector-test.html
   ```

3. **Connect MCP Inspector:**
   - Use WebSocket: `ws://localhost:8080/mcp/ws`
   - Use SSE: `http://localhost:8080/mcp/stream?clientId=inspector`

## 🔌 Connection Endpoints

### WebSocket Endpoints
- **Primary:** `ws://localhost:8080/mcp/ws` (with SockJS fallback)
- **Direct:** `ws://localhost:8080/mcp/ws-direct` (no fallback)

### Server-Sent Events (SSE)
- **Stream:** `http://localhost:8080/mcp/stream?clientId=<your-client-id>`
- **Reactive:** `http://localhost:8080/mcp/reactive/tools/stream`

### REST Endpoints
- **Initialize:** `POST /mcp/initialize`
- **List Tools:** `POST /mcp/tools/list`
- **Call Tool:** `POST /mcp/tools/call`
- **Health:** `GET /mcp/health`

## 🛠️ Available Tools

The server provides these stateful tools:

1. **Calculator** (`calculator`)
   - Operations: add, subtract, multiply, divide
   - Example: `{"operation": "add", "a": 5, "b": 3}`

2. **Profile Builder** (`profile_builder`)
   - Builds user profiles progressively
   - Collects: name, age, occupation, interests

3. **Create File** (`create_file`)
   - Creates files with specified content
   - Parameters: filename, content

4. **Session List** (`session_list`)
   - Lists active tool sessions
   - Shows conversation state

## 📡 Streaming Features

### Real-time Tool Execution
- Tools execute with streaming responses
- Chunked output for long-running operations
- Progress updates during execution

### Connection Monitoring
- Heartbeat streams every 30 seconds
- Active connection tracking
- Real-time status updates

### Multi-client Support
- Multiple inspectors can connect simultaneously
- Independent session management
- Broadcast and targeted messaging

## 🔧 MCP Inspector Configuration

### WebSocket Connection
```javascript
const ws = new WebSocket('ws://localhost:8080/mcp/ws');

ws.onopen = () => {
    console.log('Connected to MCP server');
};

ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('Received:', message);
};
```

### SSE Connection
```javascript
const eventSource = new EventSource(
    'http://localhost:8080/mcp/stream?clientId=inspector'
);

eventSource.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('Received:', message);
};
```

## 📋 Message Format

### Tool Execution Request
```json
{
    "jsonrpc": "2.0",
    "id": 123,
    "method": "tools/call",
    "params": {
        "name": "calculator",
        "arguments": {
            "operation": "add",
            "a": 5,
            "b": 3
        }
    }
}
```

### Tool Response
```json
{
    "jsonrpc": "2.0",
    "id": 123,
    "result": {
        "content": [
            {
                "type": "text",
                "text": "Result: 8"
            }
        ]
    }
}
```

## 🧪 Testing

### 1. WebSocket Test
```bash
# Using wscat (install with: npm install -g wscat)
wscat -c ws://localhost:8080/mcp/ws

# Send a message
{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"calculator","arguments":{"operation":"add","a":5,"b":3}}}
```

### 2. SSE Test
```bash
# Using curl
curl -N "http://localhost:8080/mcp/stream?clientId=test"
```

### 3. REST Test
```bash
# List tools
curl -X POST http://localhost:8080/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'

# Call tool
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"id": 2, "params": {"name": "calculator", "arguments": {"operation": "add", "a": 5, "b": 3}}}'
```

## 🔍 Troubleshooting

### Connection Issues
- **WebSocket fails:** Check if port 8080 is open
- **SSE not working:** Verify CORS settings
- **Tool execution fails:** Check tool parameters

### Common Errors
- **Tool not found:** Verify tool name spelling
- **Invalid arguments:** Check JSON format
- **Session expired:** Reconnect or create new session

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.example.mcpstateful: DEBUG
    org.springframework.web.socket: DEBUG
```

## 🚀 Advanced Features

### Custom Tool Development
1. Extend `StatefulToolBase`
2. Implement `execute()` method
3. Define required parameters
4. Register as Spring component

### Session Management
- Automatic session creation
- Persistent conversation state
- Multi-turn tool execution
- Session cleanup

### Performance Optimization
- Reactive streams with Project Reactor
- Non-blocking I/O
- Connection pooling
- Memory-efficient streaming

## 📚 Additional Resources

- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Server-Sent Events MDN](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Project Reactor](https://projectreactor.io/)

## 🤝 Support

For issues or questions:
1. Check the server logs
2. Verify endpoint connectivity
3. Test with the provided HTML page
4. Review the troubleshooting section

---

**Happy MCP Inspecting! 🎉**
