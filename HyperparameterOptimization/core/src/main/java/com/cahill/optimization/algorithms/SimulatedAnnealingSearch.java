package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.*;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedAnnealingSearch extends OptimizationAlgorithm{

    private double coolingRate = 0.045;
    private static final String COOLING_RATE = "coolingRate";

    private double startingTemperature = 1000;
    private static final String STARTING_TEMP = "startingTemperature";

    public SimulatedAnnealingSearch(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, List<Parameter> hyperparams,
                                    List<Parameter> immutableHyperparams) {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
        String coolingRateParam = OPTIMIZATION_ALGORITHM_PARAMS + COOLING_RATE;
        coolingRate = optimizationParams.containsKey(coolingRateParam) ? optimizationParams.get(coolingRateParam) : coolingRate;

        String startingTempParam = OPTIMIZATION_ALGORITHM_PARAMS + STARTING_TEMP;
        startingTemperature = optimizationParams.containsKey(startingTempParam) ? optimizationParams.get(startingTempParam) : startingTemperature;
    }

    @Override
    public void run() {
        Map<String, Parameter> candidate = this.hyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        candidate.putAll(this.immutableHyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        Iteration currentCandidate = new Iteration(new CrossValidationResults(new int[]{0}), new ArrayList<>(candidate.values()), -100.00);
        this.bestIteration = currentCandidate;

        int iterationId = 1;
        double temperature = this.startingTemperature;

        while(temperature > 0.0) {
            CrossValidationResults candidateResult = mlAlgorithm.run(candidate);
            double candidateScore = costFunction(candidateResult);
            Iteration candidateIteration = new Iteration(candidateResult, new ArrayList<>(candidate.values()), candidateScore);
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

    private Map<String, Parameter> generateCandidate(List<Parameter> params) {
        Map<String, Parameter> parameterMap = params.stream()
                .map(p -> generateNewParameter(p))
                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        parameterMap.putAll(this.immutableHyperparams.stream()
                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        return parameterMap;
    }

    protected Parameter generateNewParameter(Parameter p) {
        if (p.isNumericParameter()) {
            NumericalParameter np = (NumericalParameter) p;
            return new NumericalParameter(p.getName(), np.getMin(), np.getMax(), RandomUtils.nextDouble(np.getMin(), np.getMax()));
        } else {
            CategoricalParameter cp = (CategoricalParameter)p;
            List<String> valuesList = cp.getAllowedValues();
            return new CategoricalParameter(p.getName(), valuesList, valuesList.get(RandomUtils.nextInt(0,  valuesList.size())));
        }
    }
}
