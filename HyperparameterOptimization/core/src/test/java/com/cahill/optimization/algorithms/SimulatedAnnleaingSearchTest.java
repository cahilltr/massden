package com.cahill.optimization.algorithms;

import com.cahill.optimization.NumericalParameter;
import com.cahill.optimization.Parameter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cahill.optimization.OptimizationAlgorithm.OPTIMIZATION_ALGORITHM_PARAMS;
import static org.junit.Assert.*;

public class SimulatedAnnleaingSearchTest {
    @Test
    public void run() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new NumericalParameter("p1", 1.0, 5.0, 3.0));
        paramsList.add(new NumericalParameter("p2", 100.0, 500.0, 300.0));
        List<Parameter> immutableParamsList = new ArrayList<>();
        immutableParamsList.add(new NumericalParameter("immutableP1", 0, 0, 0));

        HashMap<String, Double> optMap = new HashMap<>();
        optMap.put(OPTIMIZATION_ALGORITHM_PARAMS + "iterations", 1000.0);

        SimulatedAnnealingSearch simulatedAnnleaingSearch = new SimulatedAnnealingSearch(new TestAlgorithm(), optMap,
                paramsList, immutableParamsList);

        simulatedAnnleaingSearch.run();
    }

    @Test
    public void generateNewParameter() throws Exception {
        double minValue = 1.0;
        double maxValue = 100.0;
        NumericalParameter p = new NumericalParameter("test", minValue, maxValue, 10);

        SimulatedAnnealingSearch simulatedAnnleaingSearch = new SimulatedAnnealingSearch(new TestAlgorithm(), new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        NumericalParameter generatedParameter = (NumericalParameter) simulatedAnnleaingSearch.generateNewParameter(p);

        assertTrue(maxValue >= generatedParameter.getRunningValue() && generatedParameter.getRunningValue() >= minValue);
        assertEquals(minValue, generatedParameter.getMin(), 0.0);
        assertEquals(maxValue, generatedParameter.getMax(), 0.0);
    }

}