package com.cahill.optimization;

import java.util.List;

public class CategoricalParameter extends Parameter<String> {

    private List<String> allowedValues;
    private String runningValue;

    public CategoricalParameter(String name, List<String> values, String runningValue) {
        super.setName(name);
        this.allowedValues = values;
        this.runningValue = runningValue;
        if (!this.allowedValues.contains(this.runningValue))
            this.allowedValues.add(this.runningValue);
        super.setNumericParameter(false);
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    @Override
    public String getRunningValue() {
        return runningValue;
    }

    public void setRunningValue(String runningValue) {
        this.runningValue = runningValue;
    }

    @Override
    public String toString() {
        String myString = "\tParameter " + super.getName();
        myString += "=" + this.runningValue;
        myString += " - Allowed Vals: " + String.join(",", this.allowedValues);
        return myString;
    }

}
