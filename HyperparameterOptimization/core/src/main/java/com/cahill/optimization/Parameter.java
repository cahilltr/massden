package com.cahill.optimization;

public class Parameter {

  private String name;
  private double min;
  private double max;
  private double runningValue = 0;
  private double step = 0;
  private boolean isFinal = false;

  public Parameter(String name, double min, double max, double runningValue, double step) {
    this.runningValue = runningValue;
    this.name = name;
    this.max = max;
    this.min = min;
    this.step = step;
  }

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

  void validateMaxAndMin() throws Exception {
    if (this.min > this.max)
      throw new Exception("Min value: " + this.min + "  cannot be greater than the max value: " + this.max);

    if (this.max < this.min)
      throw new Exception("Max value: " + this.max + "  cannot be less than the min value: " + this.min);
  }

  void validateRunningValue() {
    if (this.runningValue < this.min)
      this.runningValue = this.min;

    if (this.runningValue > this.max)
      this.runningValue = this.max;
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

  public double getStep() {
    return step;
  }

  public void setStep(double step) {
    this.step = step;
  }

  @Override
  public String toString() {
    String myString = "\tParameter " + this.name;
    myString += "=" + this.runningValue;
    myString += " - Min Val: " + this.min + " - Max Val: " + this.max + " - Step Val: " + (this.step <= 0 ? "No Step Value Specified" : this.step);
    return myString;
  }

}
