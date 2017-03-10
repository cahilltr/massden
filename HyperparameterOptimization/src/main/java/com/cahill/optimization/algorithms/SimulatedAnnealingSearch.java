package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Iteration;
import com.cahill.optimization.OptimizationAlgorithm;
import com.cahill.optimization.Parameter;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SimulatedAnnealingSearch extends OptimizationAlgorithm{

    private double coolingRate = 0.045;
    private static final String COOLING_RATE = "coolingRate";

    private double startingTemperature = 1000;
    private static final String STARTING_TEMP = "startingTemperature";

    public SimulatedAnnealingSearch(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, List<Parameter> hyperparams, List<Parameter> immutableHyperparams) {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
        String coolingRateParam = OPTIMIZATION_ALGORITHM_PARAMS + COOLING_RATE;
        coolingRate = optimizationParams.containsKey(coolingRateParam) ? optimizationParams.get(coolingRateParam) : coolingRate;

        String startingTempParam = OPTIMIZATION_ALGORITHM_PARAMS + STARTING_TEMP;
        startingTemperature = optimizationParams.containsKey(startingTempParam) ? optimizationParams.get(startingTempParam) : startingTemperature;
    }

    @Override
    public void run() {
        List<Parameter> candidate = new ArrayList<>(this.hyperparams);
        candidate.addAll(this.immutableHyperparams);
        Iteration currentCandidate = new Iteration(new CrossValidationResults(new int[]{0}), candidate, -100.00);
        this.bestIteration = currentCandidate;

        int iterationId = 1;
        double temperature = this.startingTemperature;

        while(temperature > 0.0) {
            CrossValidationResults candidateResult = mlAlgorithm.run(candidate);
            double candidateScore = costFunction(candidateResult);
            Iteration candidateIteration = new Iteration(candidateResult, candidate, candidateScore);
            iterationList.add(candidateIteration);

            temperature = getTemperature(iterationId, temperature); //get new temperature
            double deltaE = candidateScore - currentCandidate.getScore(); //get delta of scores (https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/search/local/SimulatedAnnealingSearch.java)
            currentCandidate = shouldAccept(temperature, deltaE) ? candidateIteration : currentCandidate;
            if (this.bestIteration.getScore() < currentCandidate.getScore())
                this.bestIteration = currentCandidate;

            iterationId++;
            candidate = generateCandidate(this.hyperparams);
        }
        writeOutResults();
    }

    //https://github.com/nsadawi/simulated-annealing/blob/master/SimulatedAnnealing.java
    public double getTemperature(int iterationId, double temp) {
        if (iterationId >= this.iterations) {
            return 0.0;
        }
        return temp * (1 - coolingRate);
    }

    //https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/search/local/SimulatedAnnealingSearch.java
    private double probabilityOfAcceptance(double temperature, double deltaE) {
        return Math.exp(deltaE / temperature);
    }

    //https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/search/local/SimulatedAnnealingSearch.java
    private boolean shouldAccept(double temperature, double deltaE) {
        return (deltaE > 0.0)
                || (new Random().nextDouble() <= probabilityOfAcceptance(temperature, deltaE));
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
