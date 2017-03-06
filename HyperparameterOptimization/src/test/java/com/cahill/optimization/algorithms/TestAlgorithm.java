package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TestAlgorithm implements MLAlgorithm {

    @Override
    public CrossValidationResults run(List<Parameter> params) {
        Random rand = new Random();
        CrossValidationResults results = new CrossValidationResults();
        results.setFalseNegatives(rand.nextDouble());
        results.setFalsePositives(rand.nextDouble());
        results.setTrueNegatives(rand.nextDouble());
        results.setTruePositives(rand.nextDouble());
        results.setResults(new HashMap<>());
        return results;
    }
}
