package com.example.mcpstateful.service;

import com.example.mcpstateful.state.SessionManager;
import com.example.mcpstateful.state.ToolSession;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Stateful Calculator Service using Spring AI's @Tool annotation.
 * This demonstrates the proper way to use Spring AI MCP with stateful conversations.
 */
@Service
public class StatefulCalculatorService {

    @Autowired
    private SessionManager sessionManager;

    /**
     * Calculate mathematical expressions with multi-turn parameter collection.
     * Supports session-based stateful conversations.
     */
    @Tool(description = "Perform mathematical calculations with multi-turn parameter collection. " +
          "Can collect expression and format parameters across multiple interactions.")
    public String calculate(String expression, String format, String sessionId) {
        
        // Define required parameters for this tool
        Map<String, String> requiredParams = new java.util.LinkedHashMap<>();
        requiredParams.put("expression", "Mathematical expression to evaluate (e.g., '2 + 2', 'sqrt(16)', '(10 + 5) * 2')");

        // Get or create session for multi-turn conversation
        ToolSession session;
        String currentSessionId;
        
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            session = sessionManager.getSession(sessionId);
            currentSessionId = sessionId;
            if (session == null) {
                // Session not found, create new one with the same ID
                session = sessionManager.createSession(sessionId, "calculate", requiredParams);
            }
        } else {
            // Create new session
            currentSessionId = sessionManager.generateSessionId();
            session = sessionManager.createSession(currentSessionId, "calculate", requiredParams);
        }

        // Collect provided parameters
        if (expression != null && !expression.trim().isEmpty()) {
            session.addParam("expression", expression);
        }
        
        // Handle optional format parameter (default to decimal)
        String outputFormat = (format != null && !format.trim().isEmpty()) ? format : "decimal";
        session.addParam("format", outputFormat);

        // Check if we have all required parameters
        if (!session.isComplete()) {
            String missingParam = session.getNextMissingParam();
            String paramDescription = requiredParams.get(missingParam);
            
            return String.format(
                "I need more information to complete the calculation.\n\n" +
                "Missing parameter: **%s**\n" +
                "Description: %s\n\n" +
                "Session ID: `%s`\n" +
                "Please call the tool again with this parameter.",
                missingParam,
                paramDescription,
                currentSessionId
            );
        }

        // Perform the calculation
        try {
            String expr = (String) session.getCollectedParams().get("expression");
            String fmt = (String) session.getCollectedParams().get("format");

            // Evaluate the expression
            double result = evaluateExpression(expr);

            // Format the result
            String formattedResult = formatResult(result, fmt);

            // Clean up the session after successful completion
            sessionManager.deleteSession(currentSessionId);

            return String.format("Expression: %s\nResult: %s", expr, formattedResult);

        } catch (Exception e) {
            // Keep session active on error for retry
            return String.format(
                "Error in calculation: %s\n\n" +
                "Session ID: `%s`\n" +
                "Please call the tool again with a corrected expression.",
                e.getMessage(), 
                currentSessionId
            );
        }
    }

    /**
     * Simple expression evaluator for basic mathematical operations.
     */
    private double evaluateExpression(String expression) {
        try {
            // Clean up the expression
            expression = expression.replaceAll("\\s+", "");
            
            // Handle constants
            expression = expression.replace("pi", String.valueOf(Math.PI));
            expression = expression.replace("e", String.valueOf(Math.E));
            
            // Handle basic math functions
            expression = handleMathFunctions(expression);
            
            // Parse and evaluate
            return parseExpression(expression);
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to evaluate expression: " + expression + ". " + e.getMessage());
        }
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
        return expr;
    }

    private double parseExpression(String expr) {
        // Handle simple numbers
        if (expr.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(expr);
        }

        // Handle parentheses
        while (expr.startsWith("(") && findMatchingParen(expr, 0) == expr.length() - 1) {
            expr = expr.substring(1, expr.length() - 1);
        }

        // Find operators (right to left for correct precedence)
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

        // If we get here, try to parse as a number
        return Double.parseDouble(expr);
    }

    private int findLastOperator(String expr, String operators) {
        int level = 0;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') level++;
            else if (c == '(') level--;
            else if (level == 0 && operators.indexOf(c) != -1) {
                // Make sure it's not a negative sign at the beginning
                if (c == '-' && (i == 0 || "+-*/(".indexOf(expr.charAt(i - 1)) != -1)) {
                    continue;
                }
                return i;
            }
        }
        return -1;
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

    /**
     * Format the result according to the specified format.
     */
    private String formatResult(double result, String format) {
        switch (format.toLowerCase()) {
            case "fraction":
                return convertToFraction(result);
            case "scientific":
                return String.format("%.2e", result);
            case "integer":
                return String.valueOf((long) Math.round(result));
            default: // decimal
                if (result == Math.floor(result)) {
                    return String.valueOf((long) result);
                } else {
                    return String.valueOf(result);
                }
        }
    }

    /**
     * Convert decimal to simple fraction representation.
     */
    private String convertToFraction(double decimal) {
        if (decimal == Math.floor(decimal)) {
            return String.valueOf((long) decimal);
        }

        // Simple fraction conversion
        int denominator = 1000000;
        int numerator = (int) Math.round(decimal * denominator);
        
        // Find GCD to simplify
        int gcd = gcd(Math.abs(numerator), denominator);
        numerator /= gcd;
        denominator /= gcd;

        return denominator == 1 ? String.valueOf(numerator) : numerator + "/" + denominator;
    }

    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}