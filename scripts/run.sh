#!/bin/bash

# MCP Stateful Tool Java - Run Script
echo "🤖 MCP Stateful Tool Java Server"
echo "================================="

lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "   No existing processes on port 8080"

# Set JAVA_HOME dynamically on macOS, fallback to existing if set
if [[ "$OSTYPE" == "darwin"* ]]; then
    if command -v /usr/libexec/java_home &> /dev/null; then
        export JAVA_HOME=$(/usr/libexec/java_home)
    fi
elif [ -z "$JAVA_HOME" ]; then
    echo "⚠️  JAVA_HOME not set. Please set JAVA_HOME environment variable."
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+' | head -1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 or higher is required. Current version: Java $JAVA_VERSION"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"
echo "✅ Maven detected"
echo ""

# Parse command line arguments
case "${1:-run}" in
    "run" | "server")
        echo "🚀 Starting MCP Stateful Server (Java)..."
        echo "💡 Server will be ready for MCP protocol communication"
        echo ""
        mvn spring-boot:run
        ;;
    "test")
        echo "🧪 Running tests..."
        mvn test
        ;;
    "build")
        echo "🔨 Building project..."
        mvn clean install
        ;;
    "clean")
        echo "🧹 Cleaning project..."
        mvn clean
        ;;
    "help" | "--help" | "-h")
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  run, server  - Start the MCP server (default)"
        echo "  test         - Run unit tests"
        echo "  build        - Build the project"
        echo "  clean        - Clean build artifacts"
        echo "  help         - Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0           - Start the server"
        echo "  $0 run       - Start the server"
        echo "  $0 test      - Run tests"
        echo "  $0 build     - Build project"
        echo ""
        echo "Client Connection Options:"
        echo "  1. MCP Inspector (Recommended):"
        echo "     npx @modelcontextprotocol/inspector"
        echo "     Connect to: http://localhost:8080/mcp/stream?clientId=inspector"
        echo ""
        echo "  2. Java Client:"
        echo "     ./scripts/run-client.sh"
        echo ""
        echo "  3. Python client from ../mcp_stateful_tool:"
        echo "     cd ../mcp_stateful_tool && uv run python interactive_client.py"
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo "Run '$0 help' for usage information."
        exit 1
        ;;
esac