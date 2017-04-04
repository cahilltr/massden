package com.cahill.optimization.algorithms;

import com.cahill.ml.CrossValidationResults;
import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.*;

import java.util.*;
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

    List<Map<String, Parameter>> generateParameterGrid() {
        List<Map<String, Parameter>> gridList;

        //Generate a list of all possible mutable param values
        //So, for Parameter "p1" of possible parameter values (a,b), you'll get a map of <"p1", [a,b]>
        Map<String, List<Parameter>> builtMap = this.hyperparams
                .stream()
                .map(p -> p.isNumericParameter() ? getNumericParameterPermutations((NumericalParameter)p) : getCategoricalParameterPermutation((CategoricalParameter)p))
                .map(l -> new AbstractMap.SimpleEntry<>(l.get(0).getName(), l))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


        gridList = new ArrayList<>(
                recurseCartesian(
                        new ArrayList<>(builtMap.values())
                ));

        //Add immutable Hyperparams to each map
        gridList.forEach(m -> m.putAll(this.immutableHyperparams.stream()
                .map(pa -> new AbstractMap.SimpleEntry<>(pa.getName(), pa)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))));
        return gridList;
    }

    //Returns a Set to prevent duplicates
    static Set<Map<String, Parameter>> recurseCartesian(List<List<Parameter>> values) {
        Set<Map<String, Parameter>> product = new HashSet<>();

        if (values.size() == 1) { //base case
            values.get(0).forEach(p ->
                {Map<String, Parameter> map = new HashMap<>();
                map.put(p.getName(), p);
                product.add(map);
                });
        } else {
            Set<Map<String, Parameter>> tmpProduct = recurseCartesian(values.subList(1, values.size())); //Remove first value of the list
            List<Parameter> parameterList = values.get(0); //Take the first value
            //for every map from the tmpProduct (coming back up the stack), create a new map and add a value from the parameter list
            //Example input: ([1,2],[3,4],[5,6]) -> Each bracket is a list, which we'll use in this explaination rather than a map
            //Base Stack: ([5],[6])
            //Previous Stack -> Input: ([5],[6]), Output: ([5,3], [6,3], [5,4], [6,4]) (adding a value from the parameter list)
            //Current Stack: For each input map ([5,3], [6,3], [5,4], [6,4]), add a value from the parameter list to create an output map -> ([5,3,1], [5,3,2], [6,3,1], [6,3,2], [5,4,1], [5,4,2], [6,4,1], [6,4,2])
            product.addAll(tmpProduct.stream().flatMap(m -> parameterList.stream().map(p -> {
                Map<String, Parameter> tmpMap = new HashMap<>(m);
                tmpMap.put(p.getName(), p);
                return tmpMap;
            })).collect(Collectors.toSet()));
        }

        return product;
    }

    static List<Parameter> getNumericParameterPermutations(NumericalParameter np) {
        List<Parameter> parameters = new ArrayList<>();
        if (np.getStep() > 0 && !np.isFinal()) { //make sure its not an immuatable parameter and that the step value won't cause an infinite loop
            for (double val = np.getMin(); val <= np.getMax(); val += np.getStep()) {
                parameters.add(new NumericalParameter(np.getName(), np.getMin(), np.getMax(), val, np.getStep()));
            }
        }
        return parameters;
    }

    static List<Parameter> getCategoricalParameterPermutation(CategoricalParameter cp) {
        List<Parameter> parameters = new ArrayList<>();
        for (String val : cp.getAllowedValues()) {
            parameters.add(new CategoricalParameter(cp.getName(), cp.getAllowedValues(), val));
        }
        return parameters;
    }
}
