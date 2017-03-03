package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;


public class RandomSearchTest {


    @Test
    public void run() throws Exception {
        Map<String, Double> paramsMap = new HashMap<>();
        paramsMap.put("p1", 0.0);
        paramsMap.put("p2", 0.0);
        RandomSearch randomSearch = new RandomSearch(new myAlg(), new HashMap<>(), paramsMap, new HashMap<>());

        randomSearch.run();
    }


    private class myAlg implements MLAlgorithm {

        @Override
        public CrossValidationResults run(Map<String, Double> params) {
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
}