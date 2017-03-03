package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Iteration;
import com.cahill.optimization.OptimizationAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

public class RandomSearch extends OptimizationAlgorithm {

    //TODO post processing

    RandomSearch(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, Map<String, Double> hyperparams,
                 Map<String, Double> immutableHyperparams) {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
    }

    @Override
    public void run() {
        //Use inital parameters as first solution
        Set<String> params = this.originalHyperparams.keySet();
        Iteration bestCanidate = new Iteration(new CrossValidationResults(), this.originalHyperparams, -100.00);
        Map<String, Double> canidate = this.originalHyperparams;
        for (int i = 0; i < this.iterations; i++) {
            CrossValidationResults canidateResult = mlAlgorithm.run(canidate);
            double canidateScore = costFunction(canidateResult);
            Iteration candidateIteration = new Iteration(canidateResult, canidate, canidateScore);
            iterationList.add(candidateIteration);
            if (canidateScore > bestCanidate.getScore()) { //Highest Value wins
                bestCanidate = candidateIteration;
            }
            canidate = generateCanidate(params);
        }
        this.bestIteration = bestCanidate;
        writeOutResults();
    }

    private double costFunction(CrossValidationResults results) {
        double falseResults = results.getFalseNegatives() + results.getFalsePositives();
        double positiveResults = results.getTrueNegatives() + results.getTruePositives();
        return (positiveResults * .7) - (falseResults * .3);
    }

    private Map<String, Double> generateCanidate(Set<String> params) {
        Random rand = new Random();
        return params.stream()
                .map(s -> new AbstractMap.SimpleEntry<String, Double>(s, rand.nextDouble()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private void writeOutResults() {
        String output = "Best iteration was: " + this.bestIteration.toString() + System.lineSeparator();
        output += this.iterationList.stream()
                .map(i -> "Iteration " + this.iterationList.indexOf(i) + System.lineSeparator() + i.toString())
                .collect(Collectors.joining(System.lineSeparator()));
        System.out.println(output);
    }
}
