package com.example.mcpstateful.tools;

import com.example.mcpstateful.state.ToolSession;
import com.example.mcpstateful.function.FunctionCallback;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Stateful calculator tool that can collect expression and operation type across turns.
 * 
 * This tool demonstrates progressive parameter collection for mathematical calculations,
 * supporting different output formats and complex mathematical expressions.
 */
@Service
public class CalculatorTool extends StatefulToolBase implements FunctionCallback {

    private final ScriptEngine scriptEngine;

    public CalculatorTool() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        
        // Try alternative names if JavaScript is not available
        if (engine == null) {
            engine = manager.getEngineByName("js");
        }
        if (engine == null) {
            engine = manager.getEngineByName("nashorn");
        }
        if (engine == null) {
            engine = manager.getEngineByName("rhino");
        }
        
        this.scriptEngine = engine;
        
        // Log available engines for debugging
        if (this.scriptEngine == null) {
            System.out.println("⚠️  No JavaScript engine found. Using built-in expression evaluator.");
            System.out.println("   Supported operations: +, -, *, /, (, ), sqrt, pow, sin, cos, tan, pi, e");
            if (!manager.getEngineFactories().isEmpty()) {
                System.out.println("   Available engines:");
                manager.getEngineFactories().forEach(factory -> {
                    System.out.println("     - " + factory.getEngineName() + " (" + factory.getNames() + ")");
                });
            }
        } else {
            System.out.println("✅ Using JavaScript engine: " + this.scriptEngine.getClass().getSimpleName());
        }
    }

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "Perform mathematical calculations. Can collect parameters across multiple interactions.";
    }

    @Override
    public String call(String functionArguments) {
        // For stateful tools, this method is not the primary entry point
        // The main execution happens through the execute() method
        return execute(Map.of());
    }

    @Override
    public String getToolName() {
        return "calculate";
    }

    @Override
    public Map<String, String> getRequiredParameters() {
        return Map.of(
            "expression", "Mathematical expression to evaluate (e.g., '2 + 2', 'Math.sqrt(16)', '(10 + 5) * 2')"
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        // Get or create session
        ToolSession session = getOrCreateSession(arguments, getToolName(), getRequiredParameters());
        String sessionId = getSessionId(arguments, session);

        // Collect parameters
        for (String paramName : getRequiredParameters().keySet()) {
            if (arguments.containsKey(paramName)) {
                session.addParam(paramName, arguments.get(paramName));
            }
        }

        // Optional parameters
        if (arguments.containsKey("format")) {
            session.addParam("format", arguments.get("format"));
        } else {
            session.addParam("format", "decimal");
        }

        // Check completeness
        if (!session.isComplete()) {
            String missingParam = session.getNextMissingParam();
            return createParameterRequestResponse(session, missingParam, sessionId);
        }

        // Perform calculation
        try {
            String expression = (String) session.getCollectedParams().get("expression");
            String format = (String) session.getCollectedParams().get("format");

            double numResult;
            
            // Always use our built-in evaluator for reliability
            System.out.println("🧮 Evaluating expression: " + expression);
            numResult = evaluateSimpleExpression(expression);

            // Format result
            String formatted;
            switch (format.toLowerCase()) {
                case "fraction":
                    formatted = convertToFraction(numResult);
                    break;
                case "scientific":
                    formatted = String.format("%.2e", numResult);
                    break;
                default: // decimal
                    if (numResult == Math.floor(numResult)) {
                        formatted = String.valueOf((long) numResult);
                    } else {
                        formatted = String.valueOf(numResult);
                    }
                    break;
            }

            // Clean up session
            sessionManager.deleteSession(sessionId);

            return String.format("Expression: %s\nResult: %s", expression, formatted);

        } catch (Exception e) {
            // Don't clean up session on error, allow retry
            return String.format("Error in calculation: %s\n\nSession ID: `%s`\nPlease call the tool again with a corrected expression.", 
                e.getMessage(), sessionId);
        }
    }

    /**
     * Convert decimal to fraction representation (simplified).
     */
    private String convertToFraction(double decimal) {
        if (decimal == Math.floor(decimal)) {
            return String.valueOf((long) decimal);
        }

        // Simple fraction conversion for common decimals
        int denominator = 1000000; // Precision limit
        int numerator = (int) Math.round(decimal * denominator);
        
        // Find GCD to simplify
        int gcd = gcd(Math.abs(numerator), denominator);
        numerator /= gcd;
        denominator /= gcd;

        if (denominator == 1) {
            return String.valueOf(numerator);
        } else {
            return numerator + "/" + denominator;
        }
    }

    /**
     * Calculate Greatest Common Divisor.
     */
    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
    
    /**
     * Enhanced expression evaluator that handles mathematical operations without JavaScript engine.
     */
    private double evaluateSimpleExpression(String expression) {
        return evaluateExpression(expression.trim());
    }
    
    private double evaluateExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");
        
        // Handle constants
        expr = expr.replace("pi", String.valueOf(Math.PI));
        expr = expr.replace("e", String.valueOf(Math.E));
        
        // Handle mathematical functions
        expr = handleMathFunctions(expr);
        
        // Evaluate the expression using operator precedence
        return parseExpression(expr);
    }
    
    private String handleMathFunctions(String expr) {
        // Handle sqrt function
        while (expr.contains("sqrt(")) {
            int start = expr.indexOf("sqrt(");
            int end = findMatchingParen(expr, start + 4);
            if (end == -1) throw new RuntimeException("Unmatched parentheses in sqrt function");
            
            String inner = expr.substring(start + 5, end);
            double result = Math.sqrt(parseExpression(inner));
            expr = expr.substring(0, start) + result + expr.substring(end + 1);
        }
        
        // Handle pow function - pow(base, exponent)
        while (expr.contains("pow(")) {
            int start = expr.indexOf("pow(");
            int end = findMatchingParen(expr, start + 3);
            if (end == -1) throw new RuntimeException("Unmatched parentheses in pow function");
            
            String inner = expr.substring(start + 4, end);
            String[] parts = inner.split(",", 2);
            if (parts.length != 2) throw new RuntimeException("pow function requires 2 arguments: base, exponent");
            
            double base = parseExpression(parts[0]);
            double exponent = parseExpression(parts[1]);
            double result = Math.pow(base, exponent);
            expr = expr.substring(0, start) + result + expr.substring(end + 1);
        }
        
        // Handle trigonometric functions
        expr = handleTrigFunction(expr, "sin", Math::sin);
        expr = handleTrigFunction(expr, "cos", Math::cos);
        expr = handleTrigFunction(expr, "tan", Math::tan);
        
        return expr;
    }
    
    private String handleTrigFunction(String expr, String funcName, java.util.function.DoubleUnaryOperator func) {
        while (expr.contains(funcName + "(")) {
            int start = expr.indexOf(funcName + "(");
            int end = findMatchingParen(expr, start + funcName.length());
            if (end == -1) throw new RuntimeException("Unmatched parentheses in " + funcName + " function");
            
            String inner = expr.substring(start + funcName.length() + 1, end);
            double result = func.applyAsDouble(parseExpression(inner));
            expr = expr.substring(0, start) + result + expr.substring(end + 1);
        }
        return expr;
    }
    
    private int findMatchingParen(String expr, int start) {
        int count = 1;
        for (int i = start + 1; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') count++;
            else if (expr.charAt(i) == ')') count--;
            if (count == 0) return i;
        }
        return -1;
    }
    
    private double parseExpression(String expr) {
        try {
            // Remove outer parentheses if they wrap the entire expression
            while (expr.startsWith("(") && findMatchingParen(expr, 0) == expr.length() - 1) {
                expr = expr.substring(1, expr.length() - 1);
            }
            
            // Handle simple numbers
            if (expr.matches("-?\\d+(\\.\\d+)?")) {
                return Double.parseDouble(expr);
            }
            
            // Find the last + or - not inside parentheses
            int lastAddSub = findLastOperator(expr, "+-");
            if (lastAddSub != -1) {
                char op = expr.charAt(lastAddSub);
                String left = expr.substring(0, lastAddSub);
                String right = expr.substring(lastAddSub + 1);
                
                if (op == '+') {
                    return parseExpression(left) + parseExpression(right);
                } else {
                    return parseExpression(left) - parseExpression(right);
                }
            }
            
            // Find the last * or / not inside parentheses
            int lastMulDiv = findLastOperator(expr, "*/");
            if (lastMulDiv != -1) {
                char op = expr.charAt(lastMulDiv);
                String left = expr.substring(0, lastMulDiv);
                String right = expr.substring(lastMulDiv + 1);
                
                if (op == '*') {
                    return parseExpression(left) * parseExpression(right);
                } else {
                    double divisor = parseExpression(right);
                    if (divisor == 0) throw new ArithmeticException("Division by zero");
                    return parseExpression(left) / divisor;
                }
            }
            
            // Handle power operator ^
            int lastPower = findLastOperator(expr, "^");
            if (lastPower != -1) {
                String left = expr.substring(0, lastPower);
                String right = expr.substring(lastPower + 1);
                return Math.pow(parseExpression(left), parseExpression(right));
            }
            
            return Double.parseDouble(expr);
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to evaluate expression: " + expr + 
                ". Error: " + e.getMessage());
        }
    }
    
    private int findLastOperator(String expr, String operators) {
        int level = 0;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') level++;
            else if (c == '(') level--;
            else if (level == 0 && operators.indexOf(c) != -1) {
                // Make sure it's not a negative sign at the beginning or after another operator
                if (c == '-' && (i == 0 || "+-*/^(".indexOf(expr.charAt(i - 1)) != -1)) {
                    continue;
                }
                return i;
            }
        }
        return -1;
    }
}