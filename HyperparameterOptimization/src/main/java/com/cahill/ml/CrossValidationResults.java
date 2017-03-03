package com.cahill.ml;


import java.util.Map;
import java.util.stream.Collectors;

public class CrossValidationResults {

    private Map<String, String> results;
    private double falseNegatives;
    private double falsePositives;
    private double trueNegatives;
    private double truePositives;

    public double getFalseNegatives() {
        return falseNegatives;
    }

    public void setFalseNegatives(double falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(double falsePositives) {
        this.falsePositives = falsePositives;
    }

    public double getTrueNegatives() {
        return trueNegatives;
    }

    public void setTrueNegatives(double trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(double truePositives) {
        this.truePositives = truePositives;
    }

    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean useTab) {
        String tabString = useTab ? "\t" : "";

        String myString = tabString + "False Negative: " + falseNegatives + System.lineSeparator();
        myString += tabString + "False Positive: " + falsePositives + System.lineSeparator();
        myString += tabString + "True Positive: " + truePositives + System.lineSeparator();
        myString += tabString + "True Negative: " + trueNegatives + System.lineSeparator();

        if (!results.isEmpty()) {
            myString += results.entrySet()
                    .stream()
                    .map(e -> tabString + e.getKey()  + ": " + e.getValue())
                    .collect(Collectors.joining(System.lineSeparator()));
        }

        return myString;
    }
}
