package com.avalon.coe.PrisonersDelimmaGA.GeneticAlgorithm;

/**
 * Main Class for the Gentic Algorithm
 */
public class GeneticAlgorithm {

  public static void main(String[] args) {

    /** Need params for:
     * Mutations probability
     * Population size
     * Number of generations
     * game iterations
     * probability of crossover
     */

    int generations = 100;
    int populationSize = 20;
    int gameIterations = 3;
    double mutateProb = .02;
    double crossoverProb = .97;

    ChromosomeMappingTable chromosomeMappingTable = new ChromosomeMappingTable();
    Chromosome[] chromosomes = new Chromosome[populationSize];
    RoundRobinTournament roundRobinTournament = new RoundRobinTournament(gameIterations, chromosomeMappingTable);
    Breeding breeding = new Breeding(populationSize, 71, mutateProb, crossoverProb);

    for (int i = 0; i < generations; i++) {
      chromosomes = breeding.breedChromosomes(chromosomes);
      roundRobinTournament.play(chromosomes);
    }
  }
}
