package com.gabrielsk.ai;

/**
 * Decorator nodes that modify the behavior of a single child node
 * Useful for adding conditions, loops, and other control flow
 */
public abstract class DecoratorNode implements BehaviorNode {
    protected final BehaviorNode child;
    
    public DecoratorNode(BehaviorNode child) {
        this.child = child;
    }
    
    @Override
    public void reset() {
        child.reset();
    }
    
    /**
     * Inverter - flips SUCCESS to FAILURE and vice versa
     */
    public static class Inverter extends DecoratorNode {
        public Inverter(BehaviorNode child) {
            super(child);
        }
        
        @Override
        public Status tick() {
            Status status = child.tick();
            
            if (status == Status.SUCCESS) {
                return Status.FAILURE;
            } else if (status == Status.FAILURE) {
                return Status.SUCCESS;
            }
            
            return status; // RUNNING stays RUNNING
        }
    }
    
    /**
     * Repeater - repeats child until it fails or max count reached
     */
    public static class Repeater extends DecoratorNode {
        private final int maxRepeats;
        private int repeatCount = 0;
        
        public Repeater(BehaviorNode child, int maxRepeats) {
            super(child);
            this.maxRepeats = maxRepeats;
        }
        
        @Override
        public Status tick() {
            while (repeatCount < maxRepeats) {
                Status status = child.tick();
                
                if (status == Status.RUNNING) {
                    return Status.RUNNING;
                }
                
                if (status == Status.FAILURE) {
                    reset();
                    return Status.FAILURE;
                }
                
                // SUCCESS - repeat
                repeatCount++;
                child.reset();
            }
            
            reset();
            return Status.SUCCESS;
        }
        
        @Override
        public void reset() {
            super.reset();
            repeatCount = 0;
        }
    }
    
    /**
     * UntilSuccess - repeats child until it succeeds
     */
    public static class UntilSuccess extends DecoratorNode {
        private final int maxAttempts;
        private int attempts = 0;
        
        public UntilSuccess(BehaviorNode child, int maxAttempts) {
            super(child);
            this.maxAttempts = maxAttempts;
        }
        
        @Override
        public Status tick() {
            while (attempts < maxAttempts) {
                Status status = child.tick();
                
                if (status == Status.RUNNING) {
                    return Status.RUNNING;
                }
                
                if (status == Status.SUCCESS) {
                    reset();
                    return Status.SUCCESS;
                }
                
                // FAILURE - try again
                attempts++;
                child.reset();
            }
            
            reset();
            return Status.FAILURE;
        }
        
        @Override
        public void reset() {
            super.reset();
            attempts = 0;
        }
    }
    
    /**
     * Succeeder - always returns SUCCESS regardless of child result
     */
    public static class Succeeder extends DecoratorNode {
        public Succeeder(BehaviorNode child) {
            super(child);
        }
        
        @Override
        public Status tick() {
            child.tick();
            return Status.SUCCESS;
        }
    }
    
    /**
     * Failer - always returns FAILURE regardless of child result
     */
    public static class Failer extends DecoratorNode {
        public Failer(BehaviorNode child) {
            super(child);
        }
        
        @Override
        public Status tick() {
            child.tick();
            return Status.FAILURE;
        }
    }
    
    /**
     * Cooldown - prevents child from executing too frequently
     */
    public static class Cooldown extends DecoratorNode {
        private final long cooldownMs;
        private long lastExecutionTime = 0;
        
        public Cooldown(BehaviorNode child, long cooldownMs) {
            super(child);
            this.cooldownMs = cooldownMs;
        }
        
        @Override
        public Status tick() {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastExecutionTime < cooldownMs) {
                return Status.FAILURE;
            }
            
            Status status = child.tick();
            
            if (status != Status.RUNNING) {
                lastExecutionTime = currentTime;
            }
            
            return status;
        }
        
        @Override
        public void reset() {
            super.reset();
            lastExecutionTime = 0;
        }
    }
}
