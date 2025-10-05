package com.gabrielsk.pathfinding;

/**
 * Configuration options for pathfinding behavior
 */
public class PathfindingOptions {
    public boolean allowLava = false;
    public boolean allowWater = true;
    public boolean canBreakBlocks = false;
    public boolean canFly = false;
    public int maxFallDistance = 3;
    public double playerSpeed = 1.0;
    
    public static PathfindingOptions safe() {
        PathfindingOptions options = new PathfindingOptions();
        options.allowLava = false;
        options.maxFallDistance = 3;
        return options;
    }
    
    public static PathfindingOptions aggressive() {
        PathfindingOptions options = new PathfindingOptions();
        options.canBreakBlocks = true;
        options.maxFallDistance = 10;
        return options;
    }
}
