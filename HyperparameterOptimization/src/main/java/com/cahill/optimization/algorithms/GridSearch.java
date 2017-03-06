package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.Iteration;
import com.cahill.optimization.OptimizationAlgorithm;
import com.cahill.optimization.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GridSearch extends OptimizationAlgorithm {

    private List<List<Parameter>> grid;

    public GridSearch(MLAlgorithm mlAlgorithm, Map<String, Double> optimizationParams, List<Parameter> hyperparams,
                      List<Parameter> immutableHyperparams) throws Exception {
        super(mlAlgorithm, optimizationParams, hyperparams, immutableHyperparams);
        this.grid = generateParameterGrid();
        if (this.grid.isEmpty())
            throw new Exception("The Grid for grid search is empty. Be sure that the Step value of the parameters is set");
    }

    @Override
    public void run() {
        List<Parameter> candidate = grid.get(0);
        candidate.addAll(this.immutableHyperparams);
        Iteration bestCandidate = new Iteration(new CrossValidationResults(), candidate, -100.00);

        int gridSize = this.grid.size();
        for (int i = 1; i < gridSize; i++) {
            CrossValidationResults candidateResult = mlAlgorithm.run(candidate);
            double candidateScore = costFunction(candidateResult);
            Iteration candidateIteration = new Iteration(candidateResult, candidate, candidateScore);
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
    protected List<List<Parameter>> generateParameterGrid() {
        List<List<Parameter>> gridList = new ArrayList<>();
        for (Parameter p : this.hyperparams) {
            List<Parameter> candidateFrame = this.hyperparams.stream().filter(tmp -> tmp != p).collect(Collectors.toList()); //Remove current parameter
            if (p.getStep() > 0 && !p.isFinal()) { //make sure its not an immuatable parameter and that the step value won't cause an infinite loop
                for (double val = p.getMin(); val <= p.getMax(); val += p.getStep()) {
                    Parameter paramChanged = new Parameter(p.getName(), p.getMin(), p.getMax(), val, p.getStep());
                    List<Parameter> candidate = candidateFrame.stream().collect(Collectors.toList()); //Create a copy of the list
                    candidate.add(paramChanged);
                    candidate.addAll(this.immutableHyperparams);
                    gridList.add(candidate);
                }
            }
        }
        return gridList;
    }
}
