package com.gabrielsk.ai;

import java.util.*;

/**
 * GOAP (Goal-Oriented Action Planning) System
 * Allows AI to dynamically plan sequences of actions to achieve goals
 * Based on world state and action preconditions/effects
 */
public class GOAPPlanner {
    
    /**
     * Plan a sequence of actions to achieve the goal
     * Uses A* search through action space
     */
    public static List<GOAPAction> plan(
        WorldState currentState,
        WorldState goalState,
        List<GOAPAction> availableActions
    ) {
        PriorityQueue<PlanNode> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.f)
        );
        Set<WorldState> closedSet = new HashSet<>();
        
        PlanNode startNode = new PlanNode(currentState, null, null, 0);
        openSet.add(startNode);
        
        int maxIterations = 1000;
        int iterations = 0;
        
        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            PlanNode current = openSet.poll();
            
            // Goal reached?
            if (goalState.isSatisfiedBy(current.state)) {
                return reconstructPlan(current);
            }
            
            closedSet.add(current.state);
            
            // Try each available action
            for (GOAPAction action : availableActions) {
                if (!action.checkPreconditions(current.state)) {
                    continue; // Can't execute this action yet
                }
                
                // Apply action to get new state
                WorldState newState = action.applyEffects(current.state.copy());
                
                if (closedSet.contains(newState)) {
                    continue;
                }
                
                double newCost = current.g + action.getCost();
                double heuristic = calculateHeuristic(newState, goalState);
                
                PlanNode neighbor = new PlanNode(newState, current, action, newCost + heuristic);
                neighbor.g = newCost;
                
                openSet.add(neighbor);
            }
        }
        
        // No plan found
        return Collections.emptyList();
    }
    
    /**
     * Calculate heuristic (estimated cost to goal)
     */
    private static double calculateHeuristic(WorldState current, WorldState goal) {
        int unsatisfiedGoals = 0;
        
        for (Map.Entry<String, Object> entry : goal.getState().entrySet()) {
            Object currentValue = current.get(entry.getKey());
            Object goalValue = entry.getValue();
            
            if (!Objects.equals(currentValue, goalValue)) {
                unsatisfiedGoals++;
            }
        }
        
        return unsatisfiedGoals;
    }
    
    /**
     * Reconstruct plan from goal node
     */
    private static List<GOAPAction> reconstructPlan(PlanNode goalNode) {
        List<GOAPAction> plan = new ArrayList<>();
        PlanNode current = goalNode;
        
        while (current.parent != null) {
            plan.add(current.action);
            current = current.parent;
        }
        
        Collections.reverse(plan);
        return plan;
    }
    
    /**
     * Node in the planning search space
     */
    private static class PlanNode {
        WorldState state;
        PlanNode parent;
        GOAPAction action;
        double g; // Cost from start
        double f; // Total cost (g + h)
        
        PlanNode(WorldState state, PlanNode parent, GOAPAction action, double f) {
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.f = f;
        }
    }
}
