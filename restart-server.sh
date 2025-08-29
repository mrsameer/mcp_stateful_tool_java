#!/bin/bash

# Server Restart Script
# This script stops any running server and starts a fresh one

echo "🔄 Restarting MCP Server"
echo "========================"

# Set Java 21 environment
export JAVA_HOME=/Users/shaiksameer/Library/Java/JavaVirtualMachines/openjdk-21.0.2/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "☕ Using Java: $(java -version 2>&1 | head -1)"

# Stop any running Spring Boot applications on port 8080
echo "🛑 Stopping existing server processes..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "   No existing processes on port 8080"

# Clean and compile
echo "🧹 Cleaning and compiling..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"
echo ""

# Start the server
echo "🚀 Starting MCP server..."
echo "=========================="
mvn spring-boot:run