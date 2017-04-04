package com.cahill.optimization.algorithms;

import com.cahill.optimization.CategoricalParameter;
import com.cahill.optimization.NumericalParameter;
import com.cahill.optimization.Parameter;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.cahill.optimization.algorithms.GridSearch.getCategoricalParameterPermutation;
import static com.cahill.optimization.algorithms.GridSearch.getNumericParameterPermutations;
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
    public void recurseCartesianTest() {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new NumericalParameter("p1", 1.0, 2.0, 2.0, 1));
        paramsList.add(new NumericalParameter("p2", 100.0, 200.0, 200.0, 100));

        Map<String, List<Parameter>> builtMap = paramsList
                .stream()
                .map(p -> p.isNumericParameter() ? getNumericParameterPermutations((NumericalParameter)p) : getCategoricalParameterPermutation((CategoricalParameter)p))
                .map(l -> new AbstractMap.SimpleEntry<>(l.get(0).getName(), l))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


        Set<Map<String, Parameter>> testSet = GridSearch.recurseCartesian(new ArrayList<>(builtMap.values()));
        assertNotNull(testSet);
        testSet.forEach(m -> assertEquals(2, m.size()));
        assertEquals(4, testSet.size());

        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 1.0 && ((NumericalParameter)m.get("p2")).getRunningValue() == 100.0));
        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 1.0 && ((NumericalParameter)m.get("p2")).getRunningValue() == 200.0));
        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 2.0 && ((NumericalParameter)m.get("p2")).getRunningValue() == 100.0));
        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 2.0 && ((NumericalParameter)m.get("p2")).getRunningValue() == 100.0));
    }

    @Test
    public void recurseCartesianCategorialTest() {
        List<Parameter> paramsList = new ArrayList<>();
        paramsList.add(new NumericalParameter("p1", 1.0, 2.0, 2.0, 1));
        paramsList.add(new CategoricalParameter("p2", Arrays.asList("a", "b"), "a"));

        Map<String, List<Parameter>> builtMap = paramsList
                .stream()
                .map(p -> p.isNumericParameter() ? getNumericParameterPermutations((NumericalParameter)p) : getCategoricalParameterPermutation((CategoricalParameter)p))
                .map(l -> new AbstractMap.SimpleEntry<>(l.get(0).getName(), l))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


        Set<Map<String, Parameter>> testSet = GridSearch.recurseCartesian(new ArrayList<>(builtMap.values()));
        assertNotNull(testSet);
        testSet.forEach(m -> assertEquals(2, m.size()));
        assertEquals(4, testSet.size());

        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 1.0 && ((CategoricalParameter)m.get("p2")).getRunningValue().equalsIgnoreCase("a")));
        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 1.0 && ((CategoricalParameter)m.get("p2")).getRunningValue().equalsIgnoreCase("b")));
        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 2.0 && ((CategoricalParameter)m.get("p2")).getRunningValue().equalsIgnoreCase("a")));
        assertTrue(testSet.stream().anyMatch(m -> ((NumericalParameter)m.get("p1")).getRunningValue() == 2.0 && ((CategoricalParameter)m.get("p2")).getRunningValue().equalsIgnoreCase("b")));
    }

    @Test
    public void generateParameterGridStepWillBeLargerThanMax() throws Exception {
        List<Parameter> hyperParams = new ArrayList<>();
        hyperParams.add(new NumericalParameter("p1", 1, 5, 2, 3));
        List categorialValues = Arrays.asList((new String[]{"a", "b"}));
        hyperParams.add(new CategoricalParameter("p3", categorialValues, "b"));
        List<Parameter> immutableParams = new ArrayList<>();
        immutableParams.add(new NumericalParameter("p2", 1, 5, 2));

        GridSearch gridSearch = new GridSearch(new TestAlgorithm(),new HashMap<>(), hyperParams, immutableParams);
        List<Map<String, Parameter>> grid = gridSearch.generateParameterGrid();

        assertEquals(4, grid.size());
        for (Map<String, Parameter> candidate : grid) {
            assertEquals(3, candidate.size());
            for (Parameter p : candidate.values()) {
                if (p.isNumericParameter()) {
                    NumericalParameter np = (NumericalParameter) p;
                    assertEquals(5, np.getMax(), 0);
                    assertEquals(1, np.getMin(), 0);
                    double val = np.getRunningValue();
                    if (p.getName().equals("p2")) {
                        assertEquals(2, val, 0);
                    } else {
                        assertTrue(val == 1 || val == 4);
                    }
                } else {
                    CategoricalParameter cp = (CategoricalParameter) p;
                    assertTrue(categorialValues.contains(cp.getRunningValue()));
                }
            }
        }

    }
}