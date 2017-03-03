package com.cahill.ml;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MLAlgorithm {

    String PARAMETER_ID = "parameter.";
    String FINAL_PARAMETER_ID = "final.parameter.";
    String ML_ALGORITHM = "ml.algorithm";

    //This should handle loading and splitting of data, training, and Cross Validation
    public abstract CrossValidationResults run(Map<String, Double> params);
}
