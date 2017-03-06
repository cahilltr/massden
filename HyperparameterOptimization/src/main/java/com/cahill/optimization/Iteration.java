package com.cahill.optimization;

import com.cahill.ml.CrossValidationResults;

import java.util.List;
import java.util.stream.Collectors;

public class Iteration {

    private double score;
    private CrossValidationResults results;
    private List<Parameter> params;

    public Iteration(CrossValidationResults canidateResult, List<Parameter> candidate, double canidateScore) {
        this.results = canidateResult;
        this.score = canidateScore;
        this.params = candidate;
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

    public List<Parameter> getParams() {
        return params;
    }

    public void setParams(List<Parameter> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        String myString = "Score: " + score + System.lineSeparator();
        myString += "Results: " + System.lineSeparator() + results.toString(true);
        myString += "Parameters:" + System.lineSeparator();
        myString += params.stream()
                .map(Parameter::toString)
                .collect(Collectors.joining(System.lineSeparator()));

        return myString;
    }
}
