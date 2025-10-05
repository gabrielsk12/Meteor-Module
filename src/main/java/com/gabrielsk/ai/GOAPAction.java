package com.gabrielsk.ai;

/**
 * Represents an action that can be performed in the GOAP system
 * Each action has preconditions (what must be true to execute)
 * and effects (what changes after execution)
 */
public abstract class GOAPAction {
    protected final String name;
    protected final double baseCost;
    
    public GOAPAction(String name, double baseCost) {
        this.name = name;
        this.baseCost = baseCost;
    }
    
    /**
     * Check if preconditions are met in current world state
     */
    public abstract boolean checkPreconditions(WorldState state);
    
    /**
     * Apply the effects of this action to the world state
     */
    public abstract WorldState applyEffects(WorldState state);
    
    /**
     * Execute the action (actual implementation)
     * @return true if action completed successfully
     */
    public abstract boolean execute();
    
    /**
     * Get the cost of executing this action
     * Can be dynamic based on world state
     */
    public double getCost() {
        return baseCost;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
