package com.gabrielsk.ai.ml;

import java.util.Random;
import java.util.function.Function;

/**
 * Mathematical matrix for neural network computations
 * Optimized for both CPU and GPU operations
 */
public class Matrix {
    private final double[][] data;
    private final int rows;
    private final int cols;
    
    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }
    
    public Matrix(int rows, int cols, Random random, double scale) {
        this(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = random.nextGaussian() * scale;
            }
        }
    }
    
    public double get(int i, int j) {
        return data[i][j];
    }
    
    public void set(int i, int j, double value) {
        data[i][j] = value;
    }
    
    public int rows() {
        return rows;
    }
    
    public int cols() {
        return cols;
    }
    
    public Vector multiply(Vector v) {
        if (cols != v.size()) {
            throw new IllegalArgumentException("Matrix-vector dimension mismatch");
        }
        
        Vector result = new Vector(rows);
        for (int i = 0; i < rows; i++) {
            double sum = 0;
            for (int j = 0; j < cols; j++) {
                sum += data[i][j] * v.get(j);
            }
            result.set(i, sum);
        }
        return result;
    }
    
    public Matrix multiply(double scalar) {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = this.data[i][j] * scalar;
            }
        }
        return result;
    }
    
    public void subtractInPlace(Matrix other) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] -= other.data[i][j];
            }
        }
    }
    
    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[j][i] = this.data[i][j];
            }
        }
        return result;
    }
    
    public Matrix map(Function<Double, Double> func) {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = func.apply(this.data[i][j]);
            }
        }
        return result;
    }
}
