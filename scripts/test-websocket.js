#!/usr/bin/env node

const WebSocket = require('ws');

console.log('🔌 Testing MCP WebSocket Connection...');

const ws = new WebSocket('ws://localhost:8080/mcp/ws');

ws.on('open', function open() {
    console.log('✅ WebSocket connected successfully');
    
    // Send a tool execution request
    const message = {
        jsonrpc: "2.0",
        id: Date.now(),
        method: "tools/call",
        params: {
            name: "calculate",
            arguments: {
                expression: "15 + 25"
            }
        }
    };
    
    console.log('📤 Sending message:', JSON.stringify(message, null, 2));
    ws.send(JSON.stringify(message));
});

ws.on('message', function message(data) {
    console.log('📥 Received:', data.toString());
    
    try {
        const parsed = JSON.parse(data.toString());
        if (parsed.result && parsed.result.content) {
            console.log('🎯 Tool result:', parsed.result.content[0].text);
        }
    } catch (e) {
        console.log('⚠️  Could not parse message as JSON');
    }
});

ws.on('close', function close() {
    console.log('🔌 WebSocket connection closed');
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
});

// Close connection after 5 seconds
setTimeout(() => {
    console.log('⏰ Closing connection...');
    ws.close();
    process.exit(0);
}, 5000);

