package com.cahill.optimization.algorithms;


import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.OptimizationAlgorithm;
import com.cahill.optimization.NumericalParameter;
import com.cahill.optimization.Parameter;

import java.util.List;
import java.util.Map;

//http://www.cleveralgorithms.com/nature-inspired/physical/extremal_optimization.html
public class ExtremalOptimization extends OptimizationAlgorithm {

    public ExtremalOptimization(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams,
                                List<Parameter> hyperparams, List<Parameter> immutableHyperparams) {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
    }

    @Override
    public void run() {

    }
}
