package com.cahill.optimization.algorithms;

import com.cahill.optimization.NumericalParameter;
import com.cahill.optimization.Parameter;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class RandomSearchTest {


    @Test
    public void testRun() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new NumericalParameter("p1", 1.0, 5.0, 3.0));
        paramsList.add(new NumericalParameter("p2", 100.0, 500.0, 300.0));
        List<Parameter> immutableParamsList = new ArrayList<>();
        immutableParamsList.add(new NumericalParameter("immutableP1", 0, 0, 0));
        RandomSearch randomSearch = new RandomSearch(new TestAlgorithm(), new HashMap<>(), paramsList, immutableParamsList);

        randomSearch.run();
    }

    @Test
    public void testGenerateNewParameter() throws Exception {
        double minValue = 1.0;
        double maxValue = 100.0;
        NumericalParameter p = new NumericalParameter("test", minValue, maxValue, 10);

        RandomSearch randomSearch = new RandomSearch(new TestAlgorithm(), new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        NumericalParameter generatedParameter = (NumericalParameter)randomSearch.generateNewParameter(p);

        assertTrue(maxValue >= generatedParameter.getRunningValue() && generatedParameter.getRunningValue() >= minValue);
        assertEquals(minValue, generatedParameter.getMin(), 0.0);
        assertEquals(maxValue, generatedParameter.getMax(), 0.0);
    }
}