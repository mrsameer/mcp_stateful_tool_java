package com.example.mcpstateful.state;

/**
 * Enumeration representing the different states of a multi-turn conversation session.
 */
public enum ConversationState {
    /**
     * Session is waiting for required parameters to be provided.
     */
    WAITING_FOR_PARAMS,
    
    /**
     * All required parameters have been collected and the tool is ready to execute.
     */
    READY_TO_EXECUTE,
    
    /**
     * The tool has been executed successfully and the session is complete.
     */
    COMPLETED
}