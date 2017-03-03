package com.cahill.optimization;

import com.cahill.ml.CrossValidationResults;

import java.util.Map;
import java.util.stream.Collectors;

public class Iteration {

    private double score;
    private CrossValidationResults results;
    private Map<String, Double> params;

    public Iteration(CrossValidationResults canidateResult, Map<String, Double> canidate, double canidateScore) {
        this.results = canidateResult;
        this.score = canidateScore;
        this.params = canidate;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public CrossValidationResults getResults() {
        return results;
    }

    public void setResults(CrossValidationResults results) {
        this.results = results;
    }

    public Map<String, Double> getParams() {
        return params;
    }

    public void setParams(Map<String, Double> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        String myString = "Score: " + score + System.lineSeparator();
        myString += "Results: " + System.lineSeparator() + results.toString(true);
        myString += "Parameters:" + System.lineSeparator();
        myString += params.entrySet()
                .stream()
                .map(e -> "\tParameter " + e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(System.lineSeparator()));

        return myString;
    }
}
