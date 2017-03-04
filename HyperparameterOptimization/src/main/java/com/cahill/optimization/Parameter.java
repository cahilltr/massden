package com.cahill.optimization;

public class Parameter {

  private String name;
  private double min;
  private double max;
  private double runningValue;
  private boolean isFinal;

  public Parameter(String name, double min, double max, double runningValue) {
    this.runningValue = runningValue;
    this.name = name;
    this.max = max;
    this.min = min;
  }

  public Parameter(String name, double min, double max) {
    this.name = name;
    this.max = max;
    this.min = min;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getMin() {
    return min;
  }

  public void setMin(double min) {
    this.min = min;
  }

  public double getMax() {
    return max;
  }

  public void setMax(double max) {
    this.max = max;
  }

  public double getRunningValue() {
    return runningValue;
  }

  public void setRunningValue(double runningValue) {
    this.runningValue = runningValue;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal(boolean aFinal) {
    isFinal = aFinal;
  }

  @Override
  public String toString() {
    String myString = "\tParameter " + this.name;
    myString += "=" + this.runningValue;
    myString += " - Min Val: " + this.min + " - Max Val: " + this.max;
    return myString;
  }

}
