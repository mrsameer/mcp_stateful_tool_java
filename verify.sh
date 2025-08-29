#!/bin/bash

echo "🔍 MCP Stateful Tool Java - Project Verification"
echo "================================================"

# Check project structure
echo "📁 Project Structure:"
echo "====================="

if [ -f "pom.xml" ]; then
    echo "✅ pom.xml found"
else
    echo "❌ pom.xml missing"
fi

if [ -f "README.md" ]; then
    echo "✅ README.md found"
else
    echo "❌ README.md missing"
fi

if [ -f "SETUP.md" ]; then
    echo "✅ SETUP.md found"
else
    echo "❌ SETUP.md missing"
fi

if [ -d "src/main/java/com/example/mcpstateful" ]; then
    echo "✅ Main source directory found"
else
    echo "❌ Main source directory missing"
fi

if [ -d "src/test/java/com/example/mcpstateful" ]; then
    echo "✅ Test directory found"
else
    echo "❌ Test directory missing"
fi

echo ""

# Count Java files
JAVA_FILES=$(find src -name "*.java" | wc -l)
echo "📊 Statistics:"
echo "=============="
echo "Java files: $JAVA_FILES"
echo "Main classes: $(find src/main/java -name "*.java" | wc -l)"
echo "Test classes: $(find src/test/java -name "*.java" | wc -l)"

echo ""

# Show main components
echo "🔧 Key Components:"
echo "=================="

if [ -f "src/main/java/com/example/mcpstateful/McpStatefulToolApplication.java" ]; then
    echo "✅ Main Application"
else
    echo "❌ Main Application missing"
fi

if [ -f "src/main/java/com/example/mcpstateful/state/SessionManager.java" ]; then
    echo "✅ Session Manager"
else
    echo "❌ Session Manager missing"
fi

if [ -f "src/main/java/com/example/mcpstateful/mcp/McpController.java" ]; then
    echo "✅ MCP Controller"
else
    echo "❌ MCP Controller missing"
fi

# Count tools
TOOL_COUNT=$(find src/main/java/com/example/mcpstateful/tools -name "*Tool.java" | wc -l)
echo "✅ Tools: $TOOL_COUNT"

echo ""

# Show tools
echo "🛠️  Available Tools:"
echo "=================="
for tool in src/main/java/com/example/mcpstateful/tools/*Tool.java; do
    if [ -f "$tool" ]; then
        basename "$tool" .java | sed 's/Tool$//'
    fi
done

echo ""

# Check dependencies
echo "⚙️  Dependencies:"
echo "================"
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    echo "✅ Java: $JAVA_VERSION"
else
    echo "❌ Java not found"
fi

if command -v mvn >/dev/null 2>&1; then
    MVN_VERSION=$(mvn -version 2>&1 | head -1)
    echo "✅ Maven: $MVN_VERSION"
else
    echo "❌ Maven not found"
fi

echo ""

# Recommendations
echo "💡 Next Steps:"
echo "=============="
echo "1. Install JDK 21+ if not already installed"
echo "2. Run './run.sh build' to build the project"
echo "3. Run './run.sh test' to execute tests"
echo "4. Run './run.sh run' to start the server"
echo "5. Check SETUP.md for detailed instructions"

echo ""
echo "🎯 Project verification complete!"