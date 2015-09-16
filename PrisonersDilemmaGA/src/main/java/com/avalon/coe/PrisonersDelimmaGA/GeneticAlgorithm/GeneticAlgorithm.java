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

    ChromosomeMappingTable chromosomeMappingTable = new ChromosomeMappingTable();
    Chromosome[] chromosomes = new Chromosome[populationSize];
    RoundRobinTournament roundRobinTournament = new RoundRobinTournament(gameIterations, chromosomeMappingTable);

    for (int i = 0; i < generations; i++) {
      //TODO initate/alter/offspring chromosomes
      //TODO create Chromosome offspring class

      //TODO create RoundRobinTournament Class
    }

  }
}
