package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestAlgorithm implements MLAlgorithm {

    @Override
    public CrossValidationResults run(List<Parameter> params) {
        CrossValidationResults crossValidationResults = new CrossValidationResults(generateRandomArray());
        return crossValidationResults;
    }

    private int[] generateRandomArray() {
        Random rand = new Random();
        return IntStream.iterate(0, i -> i + 1)
                .limit(100)
                .map(i -> (rand.nextDouble() > .6) ? 1 : 0)
                .toArray();
    }
}
