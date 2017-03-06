# Hyperparameter Optimization

Based on the [blog](https://blog.acolyer.org/2017/03/01/optimisation-and-training-techniques-for-deep-learning/), this repo is meant to be a testing platform for Hyperparameter optimization.

The goal will be to make this as generic as possible and to provide a "testing harness" to allow anyone to use run hyperparameter optimization.

## TODO
1. ~~Param limits (min/max)~~


## Optimization Algorithms

### Random Search
The random search algorithm runs a random search over the mutable hyperparameters. Parameters are randomly generated between their min and max values.  The first iteration will be ran using the running value.  The step attribute will be ignored if given to the paramter.

## Grid Search

## Running


## Notes

Params are specified min,max,runningValue,step(optional)
- i.e. parameter.myparam=0,5,3,1

Final params are specified runningValue
final.parameter.myparamfinal=3