package com.cahill.optimization.algorithms;

import com.cahill.optimization.Parameter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;


public class GridSearchTest {

    @Test(expected=Exception.class)
    public void runException() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new Parameter("p1", 1.0, 5.0, 3.0));
        paramsList.add(new Parameter("p2", 100.0, 500.0, 300.0));
        List<Parameter> immutableParamsList = new ArrayList<>();
        immutableParamsList.add(new Parameter("immutableP1", 0, 0, 0));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(), new HashMap<>(), paramsList, immutableParamsList);

        gridSearch.run();
    }

    @Test
    public void run() throws Exception {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new Parameter("p1", 1.0, 5.0, 3.0, 1));
        paramsList.add(new Parameter("p2", 100.0, 500.0, 300.0, 10));
        List<Parameter> immutableParamsList = new ArrayList<>();
        immutableParamsList.add(new Parameter("immutableP1", 0, 0, 0));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(), new HashMap<>(), paramsList, immutableParamsList);

        gridSearch.run();
    }

    @Test
    public void generateParameterGrid() throws Exception {
        List<Parameter> hyperParams = new ArrayList<>();
        hyperParams.add(new Parameter("p1", 1, 5, 2, 2));
        List<Parameter> immutableParams = new ArrayList<>();
        immutableParams.add(new Parameter("p2", 1, 5, 2));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(),new HashMap<>(), hyperParams, immutableParams);
        List<List<Parameter>> grid = gridSearch.generateParameterGrid();

        assertEquals(3, grid.size());
        for (List<Parameter> candidate : grid) {
            assertEquals(2, candidate.size());
            for (Parameter p : candidate) {
                assertEquals(5, p.getMax(), 0);
                assertEquals(1, p.getMin(), 0);
                double val = p.getRunningValue();
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
        hyperParams.add(new Parameter("p1", 1, 5, 2, 3));
        List<Parameter> immutableParams = new ArrayList<>();
        immutableParams.add(new Parameter("p2", 1, 5, 2));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(),new HashMap<>(), hyperParams, immutableParams);
        List<List<Parameter>> grid = gridSearch.generateParameterGrid();

        assertEquals(2, grid.size());
        for (List<Parameter> candidate : grid) {
            assertEquals(2, candidate.size());
            for (Parameter p : candidate) {
                assertEquals(5, p.getMax(), 0);
                assertEquals(1, p.getMin(), 0);
                double val = p.getRunningValue();
                if (p.getName().equals("p2")) {
                    assertEquals(2, val, 0);
                } else {
                    assertTrue(val == 1 || val == 4);
                }
            }
        }

    }
}