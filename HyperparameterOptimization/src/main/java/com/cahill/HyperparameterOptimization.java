package com.cahill;

import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class HyperparameterOptimization {
    private static final String PARAMETER_ID = "parameter.";
    private static final String FINAL_PARAMETER_ID = "final.parameter.";
    private static final String OPTIMIZATION_ALGORITHM = "optimization.algorithm.class";
    private static final String ML_ALGORITHM = "ml.algorithm";
    private static final String OPTIMIZATION_ALGORITHM_PARAMS = "optimization.algorithm.param.";

    public static void main(String[] args) {



    }

    public void run(String propertiesFile) throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(propertiesFile));

        //Set inital parameters
        Map<String, String> parameters = getParameters(props, PARAMETER_ID);
        //Define parameters that cannot be changed
        Map<String, String> immutableParams = getParameters(props, FINAL_PARAMETER_ID);
        //Select ML Algorithm
        String mlAlgorithm = props.getProperty(ML_ALGORITHM);

        //select optimization algorithm and get params
        String optimizationAlgorithmClass = props.getProperty(OPTIMIZATION_ALGORITHM);
        Map<String, String> optimizationParams = getParameters(props, OPTIMIZATION_ALGORITHM_PARAMS);


        //Start running of algorithm
            //run training and cross validation ML type
            //record, analyze, rerun

    }

    private Map<String, String> getParameters(Properties props, String type) {
        return props.stringPropertyNames()
                .stream()
                .filter(k -> k.startsWith(type))
                .map(k -> new AbstractMap.SimpleEntry<String, String>(k, props.getProperty(k)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
