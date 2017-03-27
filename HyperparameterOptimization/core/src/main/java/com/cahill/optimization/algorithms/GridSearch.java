package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GridSearch extends OptimizationAlgorithm {

    private List<Map<String, Parameter>> grid;

    public GridSearch(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, List<Parameter> hyperparams,
                      List<Parameter> immutableHyperparams) throws Exception {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
        this.grid = generateParameterGrid();
        if (this.grid.isEmpty())
            throw new Exception("The Grid for grid search is empty. Be sure that the Step value of the parameters is set");
    }

    @Override
    public void run() {
        Map<String, Parameter> candidate = grid.get(0);
        candidate.putAll(this.immutableHyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        Iteration bestCandidate = new Iteration(new CrossValidationResults(new int[]{0}), new ArrayList<>(candidate.values()), -100.00);

        int gridSize = this.grid.size();
        for (int i = 1; i < gridSize; i++) {
            CrossValidationResults candidateResult = mlAlgorithm.run(candidate);
            double candidateScore = costFunction(candidateResult);
            Iteration candidateIteration = new Iteration(candidateResult, new ArrayList<>(candidate.values()), candidateScore);
            iterationList.add(candidateIteration);
            if (candidateScore > bestCandidate.getScore()) { //Highest Value wins
                bestCandidate = candidateIteration;
            }
            candidate = this.grid.get(i);
        }
        this.bestIteration = bestCandidate;
        writeOutResults();
    }

    //This will return a list of list<parameter> in running order.
    protected List<Map<String, Parameter>> generateParameterGrid() {
        List<Map<String, Parameter>> gridList = new ArrayList<>();
        for (Parameter p : this.hyperparams) {
            List<Parameter> candidateFrame = this.hyperparams.stream().filter(tmp -> tmp != p).collect(Collectors.toList()); //Remove current parameter
            if (p.isNumericParameter()) {
                NumericalParameter np = (NumericalParameter) p;
                if (np.getStep() > 0 && !p.isFinal()) { //make sure its not an immuatable parameter and that the step value won't cause an infinite loop
                    for (double val = np.getMin(); val <= np.getMax(); val += np.getStep()) {
                        NumericalParameter paramChanged = new NumericalParameter(p.getName(), np.getMin(), np.getMax(), val, np.getStep());
                        Map<String, Parameter> candidate = candidateFrame.stream()
                                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa))
                                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
                        candidate.put(paramChanged.getName(), paramChanged);
                        candidate.putAll(this.immutableHyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
                        gridList.add(candidate);
                    }
                }
            } else {
                CategoricalParameter cp = (CategoricalParameter) p;
                if (!p.isFinal()) { //make sure its not an immuatable parameter and that the step value won't cause an infinite loop
                    for (String val : cp.getAllowedValues()) {
                        CategoricalParameter paramChanged = new CategoricalParameter(p.getName(), cp.getAllowedValues(), val);
                        Map<String, Parameter> candidate = candidateFrame.stream()
                                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa))
                                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
                        candidate.put(paramChanged.getName(), paramChanged);
                        candidate.putAll(this.immutableHyperparams.stream().map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
                        gridList.add(candidate);
                    }
                }

            }
        }
        return gridList;
    }
}
