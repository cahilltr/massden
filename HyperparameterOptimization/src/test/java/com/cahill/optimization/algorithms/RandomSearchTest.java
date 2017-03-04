package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Parameter;
import org.junit.Test;

import java.util.*;


public class RandomSearchTest {


    @Test
    public void run() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new Parameter("p1", 1.0, 5.0, 3.0));
        paramsList.add(new Parameter("p2", 100.0, 500.0, 300.0));
        RandomSearch randomSearch = new RandomSearch(new myAlg(), new HashMap<>(), paramsList, new ArrayList<Parameter>());

        randomSearch.run();
    }


    private class myAlg implements MLAlgorithm {

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
}