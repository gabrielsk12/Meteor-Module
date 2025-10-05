package com.gabrielsk.ai.ml;

import java.util.*;
import java.util.function.Function;

/**
 * Mathematical vector for neural network computations
 * Optimized for both CPU and GPU operations
 */
public class Vector {
    private final double[] data;
    private final int size;
    
    public Vector(int size) {
        this.size = size;
        this.data = new double[size];
    }
    
    public Vector(int size, Random random, double scale) {
        this(size);
        for (int i = 0; i < size; i++) {
            data[i] = (random.nextGaussian() * scale);
        }
    }
    
    public Vector(double[] data) {
        this.size = data.length;
        this.data = Arrays.copyOf(data, size);
    }
    
    public double get(int i) {
        return data[i];
    }
    
    public void set(int i, double value) {
        data[i] = value;
    }
    
    public int size() {
        return size;
    }
    
    public Vector add(Vector other) {
        Vector result = new Vector(size);
        for (int i = 0; i < size; i++) {
            result.data[i] = this.data[i] + other.data[i];
        }
        return result;
    }
    
    public Vector subtract(Vector other) {
        Vector result = new Vector(size);
        for (int i = 0; i < size; i++) {
            result.data[i] = this.data[i] - other.data[i];
        }
        return result;
    }
    
    public Vector subtract(double scalar) {
        Vector result = new Vector(size);
        for (int i = 0; i < size; i++) {
            result.data[i] = this.data[i] - scalar;
        }
        return result;
    }
    
    public void subtractInPlace(Vector other) {
        for (int i = 0; i < size; i++) {
            this.data[i] -= other.data[i];
        }
    }
    
    public Vector multiply(double scalar) {
        Vector result = new Vector(size);
        for (int i = 0; i < size; i++) {
            result.data[i] = this.data[i] * scalar;
        }
        return result;
    }
    
    public Vector divide(double scalar) {
        return multiply(1.0 / scalar);
    }
    
    public Vector hadamard(Vector other) {
        Vector result = new Vector(size);
        for (int i = 0; i < size; i++) {
            result.data[i] = this.data[i] * other.data[i];
        }
        return result;
    }
    
    public double dot(Vector other) {
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += this.data[i] * other.data[i];
        }
        return sum;
    }
    
    public Matrix outerProduct(Vector other) {
        Matrix result = new Matrix(this.size, other.size);
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < other.size; j++) {
                result.set(i, j, this.data[i] * other.data[j]);
            }
        }
        return result;
    }
    
    public Vector map(Function<Double, Double> func) {
        Vector result = new Vector(size);
        for (int i = 0; i < size; i++) {
            result.data[i] = func.apply(this.data[i]);
        }
        return result;
    }
    
    public Vector exp() {
        return map(Math::exp);
    }
    
    public double max() {
        double max = data[0];
        for (int i = 1; i < size; i++) {
            if (data[i] > max) max = data[i];
        }
        return max;
    }
    
    public double sum() {
        double sum = 0;
        for (double v : data) {
            sum += v;
        }
        return sum;
    }
    
    public double[] toArray() {
        return Arrays.copyOf(data, size);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
