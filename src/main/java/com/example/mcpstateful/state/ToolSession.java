package com.example.mcpstateful.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a stateful tool session that maintains conversation state
 * across multiple tool calls in a multi-turn conversation.
 */
public class ToolSession {
    private String toolName;
    private ConversationState state = ConversationState.WAITING_FOR_PARAMS;
    private Map<String, Object> collectedParams = new HashMap<>();
    private Map<String, String> requiredParams = new HashMap<>();
    private List<String> missingParams = new ArrayList<>();
    private String promptMessage;

    public ToolSession(String toolName, Map<String, String> requiredParams) {
        this.toolName = toolName;
        this.requiredParams = new HashMap<>(requiredParams);
        this.missingParams = new ArrayList<>(requiredParams.keySet());
    }

    /**
     * Check if all required parameters have been collected.
     */
    public boolean isComplete() {
        return missingParams.isEmpty();
    }

    /**
     * Add a parameter to the session and remove it from missing parameters.
     */
    public void addParam(String name, Object value) {
        collectedParams.put(name, value);
        missingParams.remove(name);
    }

    /**
     * Get the next missing parameter.
     */
    public String getNextMissingParam() {
        return missingParams.isEmpty() ? null : missingParams.get(0);
    }

    // Getters and Setters
    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public Map<String, Object> getCollectedParams() {
        return collectedParams;
    }

    public void setCollectedParams(Map<String, Object> collectedParams) {
        this.collectedParams = collectedParams;
    }

    public Map<String, String> getRequiredParams() {
        return requiredParams;
    }

    public void setRequiredParams(Map<String, String> requiredParams) {
        this.requiredParams = requiredParams;
    }

    public List<String> getMissingParams() {
        return missingParams;
    }

    public void setMissingParams(List<String> missingParams) {
        this.missingParams = missingParams;
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    public void setPromptMessage(String promptMessage) {
        this.promptMessage = promptMessage;
    }
}