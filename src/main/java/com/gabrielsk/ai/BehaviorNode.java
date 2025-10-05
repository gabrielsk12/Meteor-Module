package com.gabrielsk.ai;

/**
 * Base interface for all behavior tree nodes
 * Implements the core behavior tree pattern for dynamic AI decision making
 */
public interface BehaviorNode {
    
    /**
     * Tick this node, executing its behavior
     * @return Status of execution
     */
    Status tick();
    
    /**
     * Reset the node to initial state
     */
    void reset();
    
    /**
     * Status enum for node execution results
     */
    enum Status {
        SUCCESS,   // Node completed successfully
        FAILURE,   // Node failed
        RUNNING    // Node is still executing
    }
}
