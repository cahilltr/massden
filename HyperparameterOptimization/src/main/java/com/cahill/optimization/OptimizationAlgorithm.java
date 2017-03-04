package com.cahill.optimization;

import com.cahill.ml.MLAlgorithm;

import java.util.*;

public abstract class OptimizationAlgorithm {

    public static final String OPTIMIZATION_ALGORITHM_PARAMS = "optimization.algorithm.param.";
    public static final String OPTIMIZATION_ALGORITHM = "optimization.algorithm.class";

    protected Map<String, Double> optimizationParams;
    protected List<Parameter> immutableHyperparams;
    protected List<Parameter> hyperparams;
    protected MLAlgorithm mlAlgorithm;
    protected List<Iteration> iterationList = new ArrayList<>();
    protected Iteration bestIteration;

    protected int iterations = 10;

    public OptimizationAlgorithm(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, List<Parameter> hyperparams,
                                 List<Parameter> immutableHyperparams) {
        this.immutableHyperparams = immutableHyperparams;
        this.optimizationParams = optimizationParams;
        this.hyperparams = hyperparams;
        if (optimizationParams.containsKey(OPTIMIZATION_ALGORITHM_PARAMS + "iterations"))
            iterations = optimizationParams.get(OPTIMIZATION_ALGORITHM_PARAMS + "iterations").intValue();
        this.mlAlgorithm = mlAlgorithm;
    }

    public abstract void run();

    public List<Iteration> getIterationList() {
        return iterationList;
    }

    public Iteration getBestIteration() {
        return bestIteration;
    }
}
