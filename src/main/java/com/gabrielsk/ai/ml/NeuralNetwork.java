package com.gabrielsk.ai.ml;

import java.util.*;
import java.util.concurrent.*;

/**
 * Neural Network for learning player behavior patterns
 * Supports GPU acceleration via CUDA/OpenCL when available
 * Falls back to optimized CPU multi-threading
 */
public class NeuralNetwork {
    private final int[] layerSizes;
    private final List<Matrix> weights;
    private final List<Vector> biases;
    private final double learningRate;
    private final ActivationFunction activationFunction;
    private final ExecutorService executor;
    private final boolean useGPU;
    
    // GPU acceleration backend
    private GPUCompute gpuBackend;
    
    public NeuralNetwork(int[] layerSizes, double learningRate, boolean useGPU) {
        this.layerSizes = layerSizes;
        this.learningRate = learningRate;
        this.useGPU = useGPU;
        this.activationFunction = new ReLUActivation();
        
        // Initialize weights and biases
        weights = new ArrayList<>();
        biases = new ArrayList<>();
        
        Random random = new Random();
        for (int i = 0; i < layerSizes.length - 1; i++) {
            // Xavier initialization for better convergence
            double scale = Math.sqrt(2.0 / layerSizes[i]);
            weights.add(new Matrix(layerSizes[i + 1], layerSizes[i], random, scale));
            biases.add(new Vector(layerSizes[i + 1], random, scale));
        }
        
        // Initialize thread pool for CPU parallelization
        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        
        // Try to initialize GPU backend
        if (useGPU) {
            try {
                gpuBackend = GPUCompute.initialize();
                System.out.println("[AI-ML] GPU acceleration enabled: " + gpuBackend.getDeviceName());
            } catch (Exception e) {
                System.out.println("[AI-ML] GPU not available, using optimized CPU");
                this.gpuBackend = null;
            }
        }
    }
    
    /**
     * Forward pass through the network
     */
    public Vector forward(Vector input) {
        Vector current = input;
        
        if (useGPU && gpuBackend != null) {
            // GPU-accelerated forward pass
            return gpuBackend.forward(current, weights, biases, activationFunction);
        }
        
        // CPU forward pass with parallelization
        for (int i = 0; i < weights.size(); i++) {
            current = weights.get(i).multiply(current);
            current = current.add(biases.get(i));
            
            if (i < weights.size() - 1) {
                current = activationFunction.activate(current);
            } else {
                // Output layer uses softmax for classification
                current = softmax(current);
            }
        }
        
        return current;
    }
    
    /**
     * Backward pass for training
     */
    public void backward(Vector input, Vector target, Vector output) {
        List<Vector> activations = new ArrayList<>();
        List<Vector> zValues = new ArrayList<>();
        
        // Forward pass storing intermediate values
        Vector current = input;
        activations.add(current);
        
        for (int i = 0; i < weights.size(); i++) {
            Vector z = weights.get(i).multiply(current).add(biases.get(i));
            zValues.add(z);
            
            if (i < weights.size() - 1) {
                current = activationFunction.activate(z);
            } else {
                current = softmax(z);
            }
            activations.add(current);
        }
        
        // Backward pass
        List<Matrix> weightGradients = new ArrayList<>();
        List<Vector> biasGradients = new ArrayList<>();
        
        // Output layer error
        Vector delta = output.subtract(target);
        
        // Backpropagate through layers
        for (int i = weights.size() - 1; i >= 0; i--) {
            Matrix weightGrad = delta.outerProduct(activations.get(i));
            weightGradients.add(0, weightGrad);
            biasGradients.add(0, delta);
            
            if (i > 0) {
                delta = weights.get(i).transpose().multiply(delta);
                delta = delta.hadamard(activationFunction.derivative(zValues.get(i - 1)));
            }
        }
        
        // Update weights and biases
        if (useGPU && gpuBackend != null) {
            gpuBackend.updateWeights(weights, biases, weightGradients, biasGradients, learningRate);
        } else {
            updateWeightsCPU(weightGradients, biasGradients);
        }
    }
    
    private void updateWeightsCPU(List<Matrix> weightGradients, List<Vector> biasGradients) {
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < weights.size(); i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                weights.get(index).subtractInPlace(weightGradients.get(index).multiply(learningRate));
                biases.get(index).subtractInPlace(biasGradients.get(index).multiply(learningRate));
            }));
        }
        
        // Wait for all updates to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Train on a batch of data
     */
    public void trainBatch(List<TrainingExample> batch) {
        if (useGPU && gpuBackend != null) {
            gpuBackend.trainBatch(this, batch);
        } else {
            trainBatchCPU(batch);
        }
    }
    
    private void trainBatchCPU(List<TrainingExample> batch) {
        List<Future<?>> futures = new ArrayList<>();
        
        for (TrainingExample example : batch) {
            futures.add(executor.submit(() -> {
                Vector output = forward(example.input);
                backward(example.input, example.target, output);
            }));
        }
        
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Softmax activation for output layer
     */
    private Vector softmax(Vector v) {
        double max = v.max();
        Vector exp = v.subtract(max).exp();
        double sum = exp.sum();
        return exp.divide(sum);
    }
    
    /**
     * Save network to disk
     */
    public void save(String path) {
        // Implementation for saving weights and biases
    }
    
    /**
     * Load network from disk
     */
    public static NeuralNetwork load(String path) {
        // Implementation for loading weights and biases
        return null;
    }
    
    public void shutdown() {
        executor.shutdown();
        if (gpuBackend != null) {
            gpuBackend.cleanup();
        }
    }
    
    // Helper classes
    public static class TrainingExample {
        public Vector input;
        public Vector target;
        
        public TrainingExample(Vector input, Vector target) {
            this.input = input;
            this.target = target;
        }
    }
    
    interface ActivationFunction {
        Vector activate(Vector v);
        Vector derivative(Vector v);
    }
    
    static class ReLUActivation implements ActivationFunction {
        @Override
        public Vector activate(Vector v) {
            return v.map(x -> Math.max(0, x));
        }
        
        @Override
        public Vector derivative(Vector v) {
            return v.map(x -> x > 0 ? 1.0 : 0.0);
        }
    }
    
    static class TanhActivation implements ActivationFunction {
        @Override
        public Vector activate(Vector v) {
            return v.map(Math::tanh);
        }
        
        @Override
        public Vector derivative(Vector v) {
            return v.map(x -> {
                double tanh = Math.tanh(x);
                return 1 - tanh * tanh;
            });
        }
    }
}
