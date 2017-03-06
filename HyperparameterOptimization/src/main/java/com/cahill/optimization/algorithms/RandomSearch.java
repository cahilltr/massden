package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Iteration;
import com.cahill.optimization.OptimizationAlgorithm;
import com.cahill.optimization.Parameter;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;

public class RandomSearch extends OptimizationAlgorithm {

    RandomSearch(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, List<Parameter> hyperparams,
                 List<Parameter> immutableHyperparams) {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
    }

    @Override
    public void run() {
        //Use inital parameters as first solution
        List<Parameter> candidate = new ArrayList<>(this.hyperparams);
        candidate.addAll(this.immutableHyperparams);
        Iteration bestCandidate = new Iteration(new CrossValidationResults(), candidate, -100.00);

        for (int i = 0; i < this.iterations; i++) {
            CrossValidationResults candidateResult = mlAlgorithm.run(candidate);
            double candidateScore = costFunction(candidateResult);
            Iteration candidateIteration = new Iteration(candidateResult, candidate, candidateScore);
            iterationList.add(candidateIteration);
            if (candidateScore > bestCandidate.getScore()) { //Highest Value wins
                bestCandidate = candidateIteration;
            }
            candidate = generateCandidate(this.hyperparams);
        }
        this.bestIteration = bestCandidate;
        writeOutResults();
    }

    private List<Parameter> generateCandidate(List<Parameter> params) {
        List<Parameter> paramsList = params.stream()
                .map(p -> new Parameter(p.getName(), p.getMin(), p.getMax(), RandomUtils.nextDouble(p.getMin(), p.getMax())))
                .collect(Collectors.toList());
        paramsList.addAll(this.immutableHyperparams);
        return paramsList;
    }

    protected Parameter generateNewParameter(Parameter p) {
        return new Parameter(p.getName(), p.getMin(), p.getMax(), RandomUtils.nextDouble(p.getMin(), p.getMax()));
    }
}
