# Hyperparameter Optimization

Based on the [The Morning Paper blog from 3/1/2017](https://blog.acolyer.org/2017/03/01/optimisation-and-training-techniques-for-deep-learning/), this repo is meant to be a testing platform for Hyperparameter optimization.

The goal will be to make this as generic as possible and to provide a "testing harness" to allow anyone to use run hyperparameter optimization.


## Implemented Optimization Algorithms

### Random Search
The random search algorithm runs a random search over the mutable hyperparameters. Parameters are randomly generated between their min and max values.  The first iteration will be ran using the running value.  The step attribute will be ignored if given to the paramter.

### Grid Search
The grid search algorithm runs a grid search over the mutable hyperparameters and utilities the step feature of parameters.  New candidates are generated as per their step between their min and max allowable values.

### Simulated Annealing Search
The simulated annealing search runs the simulated annealing algorithm over the mutable hyperparameters and generates new candidates as per probabiliy that the current iteration will be better than remaining iterations.


## Running

## Basic Parameters
- optimization.algorithm.class: Class of optimization algorithm to run i.e. com.cahill.optimization.algorithms.GridSearch
- optimization.algorithm.param. : This is the start of parameters that the Optimization Algorithm will use.  i.e. optimization.algorithm.param.iterations would set the number of iterations
- ml.algorithm: Class of ML Algorithm to use
- final.parameter. : This is the start of a parameter that the ML algorithm will use, but the optimization algorithm will not change i.e. final.parameter.impurity=variance
- parameter. : This is the start of a parameter that the ML algorithm will use and be altered by the optmization algorithm

### Specifiying Parameters
Numeric Params are specified: min,max,runningValue,step(optional)
- i.e. parameter.myparam=0,5,3,1

Categorical Params are specified: (semicolon separated values), runningValue
- i.e. parameter.myCategoricalParam=("sadf"; "word"; "otherword"),"otherword"
- The running value of a Cataegorical param should be present in the allowed values (parenthesis semicolo separated values).  If not, it will be added on parameter creation.

Numeric Final params are specified runningValue
- i.e. final.parameter.myparamfinal=3

Categorical Final params are specified runningValue
- i.e. final.parameter.myCategoricalParam=(3)

## Limitations
- ~Currently, only numeric datatypes can be optimized by the Optimiation Algorithms.~

## SparkRandomForestData
This data is gotten from [Polish Companies Bankruptcy data](http://archive.ics.uci.edu/ml/datasets/Polish+companies+bankruptcy+data).

### Sample Properties
optimization.algorithm.class=com.cahill.optimization.algorithms.GridSearch
ml.algorithm=com.cahill.ml.examples.SparkRandomForestOptmizationExample
final.parameter.numClasses=2
parameter.numTrees=3,10,3,1
parameter.maxDepth=4,10,4,1
parameter.maxBins=10,32,10,2

## Running
Run HyperparameterOptimization with a single argument being a properties file with at least ml.algorithm and optimization.algorithm.class specified.

### Example
java -cp examples/target/examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.cahill.HyperparameterOptimization ~/optimization.properties


## Notes
The user is responsible for saving results as they feel appropriate.