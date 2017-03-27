package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.*;
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
        Map<String, Parameter> candidate = this.hyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        candidate.putAll(this.immutableHyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        Iteration bestCandidate = new Iteration(new CrossValidationResults(new int[]{0}), new ArrayList<>(candidate.values()), -100.00);

        for (int i = 0; i < this.iterations; i++) {
            CrossValidationResults candidateResult = mlAlgorithm.run(candidate);
            double candidateScore = costFunction(candidateResult);
            Iteration candidateIteration = new Iteration(candidateResult, new ArrayList<>(candidate.values()), candidateScore);
            iterationList.add(candidateIteration);
            if (candidateScore > bestCandidate.getScore()) { //Highest Value wins
                bestCandidate = candidateIteration;
            }
            candidate = generateCandidate(this.hyperparams);
        }
        this.bestIteration = bestCandidate;
        writeOutResults();
    }

    private Map<String, Parameter> generateCandidate(List<Parameter> params) {
        Map<String, Parameter> paramsMap = params.stream()
//                .map(p -> new NumericalParameter(p.getName(), p.getMin(), p.getMax(), RandomUtils.nextDouble(p.getMin(), p.getMax())))
                .map(p -> generateNewParameter(p))
                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        paramsMap.putAll(this.immutableHyperparams.stream()
                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        return paramsMap;
    }

    Parameter generateNewParameter(Parameter p) {
        if (p.isNumericParameter()) {
            NumericalParameter np = (NumericalParameter)p;
            return new NumericalParameter(p.getName(), np.getMin(), np.getMax(), RandomUtils.nextDouble(np.getMin(), np.getMax()));
        } else {
            CategoricalParameter cp = (CategoricalParameter)p;
            List<String> valuesList = cp.getAllowedValues();
            return new CategoricalParameter(p.getName(), valuesList, valuesList.get(RandomUtils.nextInt(0,  valuesList.size())));
        }
    }
}
