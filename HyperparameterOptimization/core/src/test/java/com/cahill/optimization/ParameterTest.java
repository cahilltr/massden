package com.cahill.optimization;

import org.junit.Test;

import static org.junit.Assert.*;

public class ParameterTest {

    @Test(expected = Exception.class)
    public void validateMaxAndMinMinGreaterThanMax() throws Exception {
        NumericalParameter p = new NumericalParameter("test1", 10, 1);
        p.validateMaxAndMin();
    }

    @Test
    public void validateRunningValueLessThanMin() throws Exception {
        NumericalParameter p = new NumericalParameter("test1", 10, 100, 9);
        p.validateRunningValue();
        assertEquals(10, p.getRunningValue(), 0);
    }

    @Test
    public void validateRunningValueGreaterThanMax() throws Exception {
        NumericalParameter p = new NumericalParameter("test1", 10, 100, 200);
        p.validateRunningValue();
        assertEquals(100, p.getRunningValue(), 0);
    }
}