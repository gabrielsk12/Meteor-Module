package com.gabrielsk.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Composite nodes that contain child nodes
 * Implements different execution strategies (sequence, selector, parallel)
 */
public abstract class CompositeNode implements BehaviorNode {
    protected final List<BehaviorNode> children = new ArrayList<>();
    protected int currentChildIndex = 0;
    
    public CompositeNode(BehaviorNode... children) {
        this.children.addAll(Arrays.asList(children));
    }
    
    public void addChild(BehaviorNode child) {
        children.add(child);
    }
    
    @Override
    public void reset() {
        currentChildIndex = 0;
        for (BehaviorNode child : children) {
            child.reset();
        }
    }
    
    /**
     * Sequence node - executes children in order until one fails
     * Returns SUCCESS only if ALL children succeed
     */
    public static class Sequence extends CompositeNode {
        public Sequence(BehaviorNode... children) {
            super(children);
        }
        
        @Override
        public Status tick() {
            while (currentChildIndex < children.size()) {
                Status status = children.get(currentChildIndex).tick();
                
                if (status == Status.FAILURE) {
                    reset();
                    return Status.FAILURE;
                }
                
                if (status == Status.RUNNING) {
                    return Status.RUNNING;
                }
                
                // SUCCESS - move to next child
                currentChildIndex++;
            }
            
            // All children succeeded
            reset();
            return Status.SUCCESS;
        }
    }
    
    /**
     * Selector node - tries children in order until one succeeds
     * Returns SUCCESS if ANY child succeeds
     */
    public static class Selector extends CompositeNode {
        public Selector(BehaviorNode... children) {
            super(children);
        }
        
        @Override
        public Status tick() {
            while (currentChildIndex < children.size()) {
                Status status = children.get(currentChildIndex).tick();
                
                if (status == Status.SUCCESS) {
                    reset();
                    return Status.SUCCESS;
                }
                
                if (status == Status.RUNNING) {
                    return Status.RUNNING;
                }
                
                // FAILURE - try next child
                currentChildIndex++;
            }
            
            // All children failed
            reset();
            return Status.FAILURE;
        }
    }
    
    /**
     * Parallel node - executes all children simultaneously
     * Succeeds when successThreshold children succeed
     * Fails when too many children fail to reach threshold
     */
    public static class Parallel extends CompositeNode {
        private final int successThreshold;
        private int successCount = 0;
        private int failureCount = 0;
        
        public Parallel(int successThreshold, BehaviorNode... children) {
            super(children);
            this.successThreshold = successThreshold;
        }
        
        @Override
        public Status tick() {
            successCount = 0;
            failureCount = 0;
            
            for (BehaviorNode child : children) {
                Status status = child.tick();
                
                if (status == Status.SUCCESS) {
                    successCount++;
                } else if (status == Status.FAILURE) {
                    failureCount++;
                }
            }
            
            if (successCount >= successThreshold) {
                reset();
                return Status.SUCCESS;
            }
            
            if (children.size() - failureCount < successThreshold) {
                reset();
                return Status.FAILURE;
            }
            
            return Status.RUNNING;
        }
        
        @Override
        public void reset() {
            super.reset();
            successCount = 0;
            failureCount = 0;
        }
    }
    
    /**
     * RandomSelector - picks a random child to execute
     * Useful for varied behavior patterns
     */
    public static class RandomSelector extends CompositeNode {
        private BehaviorNode selectedChild = null;
        
        public RandomSelector(BehaviorNode... children) {
            super(children);
        }
        
        @Override
        public Status tick() {
            if (selectedChild == null && !children.isEmpty()) {
                int randomIndex = (int) (Math.random() * children.size());
                selectedChild = children.get(randomIndex);
            }
            
            if (selectedChild == null) {
                return Status.FAILURE;
            }
            
            Status status = selectedChild.tick();
            
            if (status != Status.RUNNING) {
                selectedChild = null; // Reset for next tick
            }
            
            return status;
        }
        
        @Override
        public void reset() {
            super.reset();
            selectedChild = null;
        }
    }
}
