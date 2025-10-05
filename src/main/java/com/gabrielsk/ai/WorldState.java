package com.gabrielsk.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the state of the world for GOAP planning
 * Contains key-value pairs describing conditions
 */
public class WorldState {
    private final Map<String, Object> state;
    
    public WorldState() {
        this.state = new HashMap<>();
    }
    
    private WorldState(Map<String, Object> state) {
        this.state = new HashMap<>(state);
    }
    
    /**
     * Set a value in the world state
     */
    public void set(String key, Object value) {
        state.put(key, value);
    }
    
    /**
     * Get a value from the world state
     */
    public Object get(String key) {
        return state.get(key);
    }
    
    /**
     * Get a boolean value (defaults to false if not present)
     */
    public boolean getBoolean(String key) {
        Object value = state.get(key);
        return value instanceof Boolean && (Boolean) value;
    }
    
    /**
     * Get an integer value (defaults to 0 if not present)
     */
    public int getInt(String key) {
        Object value = state.get(key);
        return value instanceof Integer ? (Integer) value : 0;
    }
    
    /**
     * Get a double value (defaults to 0.0 if not present)
     */
    public double getDouble(String key) {
        Object value = state.get(key);
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        return 0.0;
    }
    
    /**
     * Check if a key exists
     */
    public boolean has(String key) {
        return state.containsKey(key);
    }
    
    /**
     * Remove a key
     */
    public void remove(String key) {
        state.remove(key);
    }
    
    /**
     * Check if this state satisfies the goal state
     * Goal is satisfied if all goal conditions are met
     */
    public boolean isSatisfiedBy(WorldState currentState) {
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            Object currentValue = currentState.get(entry.getKey());
            
            if (!Objects.equals(currentValue, entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Create a deep copy of this world state
     */
    public WorldState copy() {
        return new WorldState(state);
    }
    
    /**
     * Get the underlying state map
     */
    public Map<String, Object> getState() {
        return new HashMap<>(state);
    }
    
    /**
     * Merge another world state into this one
     */
    public void merge(WorldState other) {
        state.putAll(other.state);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldState)) return false;
        WorldState that = (WorldState) o;
        return state.equals(that.state);
    }
    
    @Override
    public int hashCode() {
        return state.hashCode();
    }
    
    @Override
    public String toString() {
        return "WorldState" + state;
    }
}
