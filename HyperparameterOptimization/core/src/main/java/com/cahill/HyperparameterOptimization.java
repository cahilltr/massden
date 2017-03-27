package com.cahill;

import com.cahill.ml.MLAlgorithm;
import com.cahill.optimization.CategoricalParameter;
import com.cahill.optimization.OptimizationAlgorithm;
import com.cahill.optimization.NumericalParameter;
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
            IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        HyperparameterOptimization optimization = new HyperparameterOptimization();
        optimization.run(args[0]);
    }

    public void run(String propertiesFile) throws IOException, NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, InvocationTargetException, InterruptedException {
        Properties props = new Properties();
        props.load(new FileReader(propertiesFile));

        Thread.sleep(10000);

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

        OptimizationAlgorithm optimizationAlgorithm = getOptimizationClass(optimizationAlgorithmClass, optimizationParams, mlAlgorithm, parameters, immutableParams);

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
                    String value = props.getProperty(k);
                    String[] values = value.split(",");
                    int valuesLength = values.length;
                    Parameter p;
                    if (value.startsWith("(")) {
                        if (valuesLength == 1) {
                            String paramValue = values[0].replace("(", "").replace(")", "");
                            p = new CategoricalParameter(k, Collections.singletonList(paramValue), paramValue);
                        } else if (valuesLength == 2) {
                            String paramValue = values[0].replace("(", "").replace(")", "");
                            String[] valuesArray = paramValue.split(";");
                            String runningValue = values[1];
                            p = new CategoricalParameter(k, Arrays.asList(valuesArray), runningValue);
                        } else {
                            p = null;
                        }
                    } else {
                        if (valuesLength == 1) {
                            Double paramValue = Double.parseDouble(values[0]);
                            p = new NumericalParameter(k, paramValue, paramValue, paramValue);
                            p.setFinal(true);
                        } else if (valuesLength == 3) { //min,max,runningvalue
                            p = new NumericalParameter(k, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
                            p.setFinal(false);
                        } else if (valuesLength == 4) { //Includes step value
                            p = new NumericalParameter(k, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
                            p.setFinal(false);
                        } else {
                            p = null;
                        }
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

    private OptimizationAlgorithm getOptimizationClass(String optimizationAlgorithmClass, Map<String, Double> optimizationParams,
                                                       MLAlgorithm mlAlgorithm, List<Parameter> hyperparams,
                                                       List<Parameter> immutableHyperParams) throws NoSuchMethodException,
            ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = Class.forName(optimizationAlgorithmClass);
        Constructor<?> ctor = clazz.getConstructor(MLAlgorithm.class, Map.class, List.class, List.class);
        return (OptimizationAlgorithm) ctor.newInstance(mlAlgorithm, optimizationParams, hyperparams, immutableHyperParams);
    }
}
