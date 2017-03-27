package com.cahill.optimization;


abstract public class Parameter <T>{

    private String name;
    private boolean isFinal = false;
    private boolean isNumericParameter = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean isNumericParameter() {
        return isNumericParameter;
    }

    public void setNumericParameter(boolean numericParameter) {
        isNumericParameter = numericParameter;
    }

    public abstract T getRunningValue();
}
