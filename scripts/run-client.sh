#!/bin/bash

# Java MCP Client Runner
# This script compiles and runs the Java MCP client

echo "🚀 Java MCP Client Runner"
echo "========================="

# Set Java 21 environment
export JAVA_HOME=/Users/shaiksameer/Library/Java/JavaVirtualMachines/openjdk-21.0.2/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "☕ Using Java: $(java -version 2>&1 | head -1)"

# Check if server is running
echo "📡 Checking if server is running..."
if curl -s http://localhost:8080/mcp/health > /dev/null 2>&1; then
    echo "✅ Server is running"
else
    echo "❌ Server is not running. Please start the server first with:"
    echo "   mvn spring-boot:run"
    echo ""
    echo "   Or in another terminal:"
    echo "   ./run.sh"
    exit 1
fi

# Build the project
echo ""
echo "🔨 Building project..."
mvn compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"
echo ""

# Choose which client to run
echo "Choose which client to run:"
echo "1. Interactive Client (like Python interactive_client.py)"
echo "2. Test Client (like Python test_client.py - runs automated demos)"
echo ""
read -p "Enter choice (1 or 2): " choice

case $choice in
    1)
        echo ""
        echo "🎯 Starting Interactive Java Client..."
        echo "======================================"
        mvn exec:java -Dexec.mainClass="com.example.mcpstateful.client.InteractiveMcpClient" -q
        ;;
    2)
        echo ""
        echo "🧪 Starting Test Client..."
        echo "=========================="
        mvn exec:java -Dexec.mainClass="com.example.mcpstateful.client.StatefulMcpTestClient" -q
        ;;
    *)
        echo "❌ Invalid choice. Please enter 1 or 2."
        exit 1
        ;;
esac