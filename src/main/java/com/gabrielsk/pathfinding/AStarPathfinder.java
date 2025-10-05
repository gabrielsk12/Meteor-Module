package com.gabrielsk.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.*;

/**
 * Advanced A* Pathfinding Algorithm
 * Optimized for Minecraft terrain with dynamic obstacle avoidance
 * Thread-safe and cache-optimized for performance
 */
public class AStarPathfinder {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final int MAX_NODES = 10000; // Prevent infinite loops
    private static final int CACHE_SIZE = 1000;
    
    // Thread pool for async pathfinding
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    // Path cache for frequently visited routes
    private static final Map<PathCacheKey, List<BlockPos>> pathCache = 
        new ConcurrentHashMap<>(CACHE_SIZE);
    
    // Movement cost multipliers
    private static final double WALK_COST = 1.0;
    private static final double DIAGONAL_COST = 1.414; // sqrt(2)
    private static final double JUMP_COST = 1.5;
    private static final double FALL_COST = 0.5; // Falling is "free"
    private static final double DANGER_COST = 100.0; // Lava, void
    
    /**
     * Find path using A* algorithm (synchronous)
     */
    public static List<BlockPos> findPath(BlockPos start, BlockPos goal, PathfindingOptions options) {
        // Check cache first
        PathCacheKey cacheKey = new PathCacheKey(start, goal);
        if (pathCache.containsKey(cacheKey)) {
            List<BlockPos> cachedPath = pathCache.get(cacheKey);
            if (isPathStillValid(cachedPath, options)) {
                return new ArrayList<>(cachedPath);
            }
        }
        
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<BlockPos, Node> allNodes = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();
        
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);
        
        int nodesExplored = 0;
        
        while (!openSet.isEmpty() && nodesExplored < MAX_NODES) {
            Node current = openSet.poll();
            nodesExplored++;
            
            if (current.pos.equals(goal) || current.pos.isWithinDistance(goal, 2)) {
                List<BlockPos> path = reconstructPath(current);
                pathCache.put(cacheKey, path);
                return path;
            }
            
            closedSet.add(current.pos);
            
            // Explore neighbors
            for (BlockPos neighbor : getNeighbors(current.pos, options)) {
                if (closedSet.contains(neighbor)) continue;
                
                double movementCost = getMovementCost(current.pos, neighbor, options);
                if (movementCost == Double.POSITIVE_INFINITY) continue; // Impassable
                
                double tentativeG = current.g + movementCost;
                
                Node neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, tentativeG, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeG < neighborNode.g) {
                    neighborNode.parent = current;
                    neighborNode.g = tentativeG;
                    neighborNode.f = neighborNode.g + neighborNode.h;
                    // Re-add to update priority
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }
        
        // No path found - return partial path to closest node
        return findClosestApproach(allNodes, goal);
    }
    
    /**
     * Find path asynchronously
     */
    public static CompletableFuture<List<BlockPos>> findPathAsync(BlockPos start, BlockPos goal, PathfindingOptions options) {
        return CompletableFuture.supplyAsync(() -> findPath(start, goal, options), executor);
    }
    
    /**
     * Heuristic function (Octile distance - accounts for diagonal movement)
     */
    private static double heuristic(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        
        // Octile distance
        int dMin = Math.min(dx, dz);
        int dMax = Math.max(dx, dz);
        return DIAGONAL_COST * dMin + WALK_COST * (dMax - dMin) + dy * JUMP_COST;
    }
    
    /**
     * Get valid neighboring positions
     */
    private static List<BlockPos> getNeighbors(BlockPos pos, PathfindingOptions options) {
        List<BlockPos> neighbors = new ArrayList<>(26); // 3x3x3 - 1
        World world = mc.world;
        if (world == null) return neighbors;
        
        // 8 horizontal directions + up/down
        int[][] offsets = {
            // Horizontal (same level)
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            // Diagonals
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
            // Jump up
            {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
            {1, 1, 1}, {1, 1, -1}, {-1, 1, 1}, {-1, 1, -1},
            // Fall down
            {1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1},
            {1, -1, 1}, {1, -1, -1}, {-1, -1, 1}, {-1, -1, -1},
            // Straight up/down
            {0, 1, 0}, {0, -1, 0}
        };
        
        for (int[] offset : offsets) {
            BlockPos neighbor = pos.add(offset[0], offset[1], offset[2]);
            
            if (isPositionValid(neighbor, options)) {
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
    
    /**
     * Calculate movement cost between two adjacent positions
     */
    private static double getMovementCost(BlockPos from, BlockPos to, PathfindingOptions options) {
        World world = mc.world;
        if (world == null) return Double.POSITIVE_INFINITY;
        
        Block blockAt = world.getBlockState(to).getBlock();
        Block blockBelow = world.getBlockState(to.down()).getBlock();
        Block blockAbove = world.getBlockState(to.up()).getBlock();
        
        // Check for impassable blocks
        if (!blockAt.equals(Blocks.AIR) && !blockAt.equals(Blocks.WATER) && !blockAt.equals(Blocks.LAVA)) {
            if (!options.canBreakBlocks) return Double.POSITIVE_INFINITY;
            return WALK_COST * 5; // High cost for breaking
        }
        
        // Danger detection
        if (blockAt.equals(Blocks.LAVA) || blockBelow.equals(Blocks.LAVA)) {
            if (!options.allowLava) return Double.POSITIVE_INFINITY;
            return DANGER_COST;
        }
        
        // Void check
        if (to.getY() < -60) {
            return Double.POSITIVE_INFINITY;
        }
        
        // Need solid ground
        boolean hasSolidGround = !blockBelow.equals(Blocks.AIR);
        if (!hasSolidGround && !options.canFly) {
            // Check if it's a controlled fall
            int fallDistance = 0;
            BlockPos checkPos = to;
            while (fallDistance < 10 && world.getBlockState(checkPos.down()).isAir()) {
                checkPos = checkPos.down();
                fallDistance++;
            }
            if (fallDistance > options.maxFallDistance) {
                return Double.POSITIVE_INFINITY;
            }
            return FALL_COST * fallDistance;
        }
        
        // Calculate basic movement cost
        int dx = Math.abs(to.getX() - from.getX());
        int dy = to.getY() - from.getY();
        int dz = Math.abs(to.getZ() - from.getZ());
        
        double cost = WALK_COST;
        
        // Diagonal movement
        if (dx > 0 && dz > 0) {
            cost = DIAGONAL_COST;
        }
        
        // Jumping cost
        if (dy > 0) {
            cost += JUMP_COST * dy;
        } else if (dy < 0) {
            cost += FALL_COST * Math.abs(dy);
        }
        
        // Water is slower
        if (blockAt.equals(Blocks.WATER)) {
            cost *= 2.0;
        }
        
        return cost;
    }
    
    /**
     * Check if position is valid for pathfinding
     */
    private static boolean isPositionValid(BlockPos pos, PathfindingOptions options) {
        World world = mc.world;
        if (world == null) return false;
        
        // Check loaded
        if (!world.isChunkLoaded(pos)) return false;
        
        Block blockAt = world.getBlockState(pos).getBlock();
        Block blockAbove = world.getBlockState(pos.up()).getBlock();
        
        // Need 2 blocks of air space (player height)
        boolean hasSpace = (blockAt.equals(Blocks.AIR) || blockAt.equals(Blocks.WATER)) &&
                          (blockAbove.equals(Blocks.AIR) || blockAbove.equals(Blocks.WATER));
        
        return hasSpace;
    }
    
    /**
     * Reconstruct path from goal to start
     */
    private static List<BlockPos> reconstructPath(Node goalNode) {
        List<BlockPos> path = new ArrayList<>();
        Node current = goalNode;
        
        while (current != null) {
            path.add(current.pos);
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Find closest approach when no complete path exists
     */
    private static List<BlockPos> findClosestApproach(Map<BlockPos, Node> allNodes, BlockPos goal) {
        Node closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        
        for (Node node : allNodes.values()) {
            double distance = node.pos.getSquaredDistance(goal);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = node;
            }
        }
        
        return closest != null ? reconstructPath(closest) : new ArrayList<>();
    }
    
    /**
     * Check if cached path is still valid
     */
    private static boolean isPathStillValid(List<BlockPos> path, PathfindingOptions options) {
        World world = mc.world;
        if (world == null || path.isEmpty()) return false;
        
        // Quick validation - check if key positions are still passable
        for (int i = 0; i < path.size(); i += Math.max(1, path.size() / 10)) {
            if (!isPositionValid(path.get(i), options)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Clear path cache
     */
    public static void clearCache() {
        pathCache.clear();
    }
    
    /**
     * Node class for A* algorithm
     */
    private static class Node {
        BlockPos pos;
        Node parent;
        double g; // Cost from start
        double h; // Heuristic to goal
        double f; // Total cost
        
        Node(BlockPos pos, Node parent, double g, double h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
    
    /**
     * Cache key for path storage
     */
    private static class PathCacheKey {
        final BlockPos start;
        final BlockPos goal;
        final int hash;
        
        PathCacheKey(BlockPos start, BlockPos goal) {
            this.start = start;
            this.goal = goal;
            this.hash = Objects.hash(start, goal);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathCacheKey)) return false;
            PathCacheKey that = (PathCacheKey) o;
            return start.equals(that.start) && goal.equals(that.goal);
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
