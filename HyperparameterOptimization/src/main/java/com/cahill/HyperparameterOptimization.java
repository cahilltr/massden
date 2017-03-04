package com.cahill;

import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.OptimizationAlgorithm;
import com.cahill.optimization.Parameter;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.cahill.ml.MLAlgorithm.*;
import static com.cahill.optimization.OptimizationAlgorithm.OPTIMIZATION_ALGORITHM;
import static com.cahill.optimization.OptimizationAlgorithm.OPTIMIZATION_ALGORITHM_PARAMS;

public class HyperparameterOptimization {


    public static void main(String[] args) throws IOException, NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        HyperparameterOptimization optimization = new HyperparameterOptimization();
        optimization.run(args[0]);
    }

    public void run(String propertiesFile) throws IOException, NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        Properties props = new Properties();
        props.load(new FileReader(propertiesFile));

        //Set inital parameters
        List<Parameter> parameters = getMLParameters(props, PARAMETER_ID);
        //Define parameters that cannot be changed
        List<Parameter> immutableParams = getMLParameters(props, FINAL_PARAMETER_ID);
        //Select ML Algorithm
        String mlAlgorithmName = props.getProperty(ML_ALGORITHM);
        MLAlgorithm mlAlgorithm = getMLClass(mlAlgorithmName);

        //select optimization algorithm and get optimizationParams
        String optimizationAlgorithmClass = props.getProperty(OPTIMIZATION_ALGORITHM);
        Map<String, Double> optimizationParams = getOptimizeParameters(props, OPTIMIZATION_ALGORITHM_PARAMS);

        OptimizationAlgorithm optimizationAlgorithm = getOptimizationClass(optimizationAlgorithmClass, optimizationParams, mlAlgorithm);

        //Use input optimizationParams as first pass
//        String output = mlAlgorithm.run(); //counts as iteration

        //Start running of algorithm
            //run training and cross validation ML type
            //record, analyze, rerun
        optimizationAlgorithm.run();
    }


    private Map<String, Double> getOptimizeParameters(Properties props, String type) {
        return props.stringPropertyNames()
            .stream()
            .filter(k -> k.startsWith(type))
            .map(k -> new AbstractMap.SimpleEntry<String, Double>(k, Double.parseDouble(props.getProperty(k))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<Parameter> getMLParameters(Properties props, String type) {
        return props.stringPropertyNames()
                .stream()
                .filter(k -> k.startsWith(type))
                .map(k -> {
                    Parameter p;
                    String value = props.getProperty(k);
                    String[] values = value.split(",");
                    if (values.length == 1) {
                        Double paramValue = Double.parseDouble(values[0]);
                        p = new Parameter(k, paramValue, paramValue, paramValue);
                        p.setFinal(true);
                    } else if (value.length() == 3) {
                        p = new Parameter(k, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
                        p.setFinal(false);
                    } else {
                        p = null;
                    }
                    return p;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    private MLAlgorithm getMLClass(String className) throws NoSuchMethodException,
        ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> ctor = clazz.getConstructor();
        return (MLAlgorithm) ctor.newInstance();
    }

    private OptimizationAlgorithm getOptimizationClass(String optimizationAlgorithmClass, Map<String, Double> optimizationParams, MLAlgorithm mlAlgorithm) throws NoSuchMethodException,
            ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = Class.forName(optimizationAlgorithmClass);
        Constructor<?> ctor = clazz.getConstructor(Map.class, MLAlgorithm.class);
        return (OptimizationAlgorithm) ctor.newInstance(optimizationParams, mlAlgorithm);
    }
}
