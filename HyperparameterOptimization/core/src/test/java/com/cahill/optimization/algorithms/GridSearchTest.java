package com.cahill.optimization.algorithms;

import com.cahill.optimization.CategoricalParameter;
import com.cahill.optimization.NumericalParameter;
import com.cahill.optimization.Parameter;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class GridSearchTest {

    @Test(expected=Exception.class)
    public void runException() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new NumericalParameter("p1", 1.0, 5.0, 3.0));
        paramsList.add(new NumericalParameter("p2", 100.0, 500.0, 300.0));
        List<Parameter> immutableParamsList = new ArrayList<>();
        immutableParamsList.add(new NumericalParameter("immutableP1", 0, 0, 0));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(), new HashMap<>(), paramsList, immutableParamsList);

        gridSearch.run();
    }

    @Test
    public void run() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new NumericalParameter("p1", 1.0, 5.0, 3.0, 1));
        paramsList.add(new NumericalParameter("p2", 100.0, 500.0, 300.0, 10));
        List<Parameter> immutableParamsList = new ArrayList<>();
        immutableParamsList.add(new NumericalParameter("immutableP1", 0, 0, 0));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(), new HashMap<>(), paramsList, immutableParamsList);

        gridSearch.run();
    }

    @Test
    public void generateParameterGrid() throws Exception {
        List<Parameter> hyperParams = new ArrayList<>();
        hyperParams.add(new NumericalParameter("p1", 1, 5, 2, 2));
        List<Parameter> immutableParams = new ArrayList<>();
        immutableParams.add(new NumericalParameter("p2", 1, 5, 2));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(),new HashMap<>(), hyperParams, immutableParams);
        List<Map<String, Parameter>> grid = gridSearch.generateParameterGrid();

        assertEquals(3, grid.size());
        for (Map<String, Parameter> candidate : grid) {
            assertEquals(2, candidate.size());
            for (Parameter p : candidate.values()) {
                NumericalParameter np = (NumericalParameter) p;
                assertEquals(5, np.getMax(), 0);
                assertEquals(1, np.getMin(), 0);
                double val = np.getRunningValue();
                if (p.getName().equals("p2")) {
                    assertEquals(2, val, 0);
                } else {
                    assertTrue(val == 1 || val == 3 || val == 5);
                }
            }
        }

    }

    @Test
    public void generateParameterGridStepWillBeLargerThanMax() throws Exception {
        List<Parameter> hyperParams = new ArrayList<>();
        hyperParams.add(new NumericalParameter("p1", 1, 5, 2, 3));
        hyperParams.add(new CategoricalParameter("p3", Arrays.asList(new String[]{"a", "b"}), "b"));
        List<Parameter> immutableParams = new ArrayList<>();
        immutableParams.add(new NumericalParameter("p2", 1, 5, 2));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(),new HashMap<>(), hyperParams, immutableParams);
        List<Map<String, Parameter>> grid = gridSearch.generateParameterGrid();

        assertEquals(2, grid.size());
        for (Map<String, Parameter> candidate : grid) {
            assertEquals(2, candidate.size());
            for (Parameter p : candidate.values()) {
                NumericalParameter np = (NumericalParameter) p;
                assertEquals(5, np.getMax(), 0);
                assertEquals(1, np.getMin(), 0);
                double val = np.getRunningValue();
                if (p.getName().equals("p2")) {
                    assertEquals(2, val, 0);
                } else {
                    assertTrue(val == 1 || val == 4);
                }
            }
        }

    }
}