package com.avalon.coe.PrisonersDelimmaGA.GeneticAlgorithm;

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RoundRobinTournament for the entire population.
 */
public class RoundRobinTournament {

  private final int iterations;
  private final ChromosomeMappingTable chromosomeMappingTable;

  public RoundRobinTournament(int iterations, ChromosomeMappingTable chromosomeMappingTable) {
    this.iterations = iterations;
    this.chromosomeMappingTable = chromosomeMappingTable;
  }

  /**
   * Play a generations worth of PD. Each player plays
   * each other in a RoundRobin style
   */
  public Map<String, Integer> play(Chromosome[] chromosomes) {
    Map<String, Integer> scoreMap = new HashedMap();
    for (int i = 0; i < chromosomes.length; i++) {
      Prisoner iPrisoner = new Prisoner(chromosomes[i], chromosomeMappingTable);
      for (int j = i + 1; j < chromosomes.length; j++) {
        Prisoner jPrisoner = new Prisoner(chromosomes[j], chromosomeMappingTable);
        Game g = new Game(iPrisoner, jPrisoner, iterations);
        g.Play();
        int[] scores = g.getScores();
        if (scoreMap.containsKey(iPrisoner.getChromosomeString())) {
          scoreMap.put(iPrisoner.getChromosomeString(), scoreMap.get(iPrisoner.getChromosomeString()) + scores[0]);
        } else {
          scoreMap.put(iPrisoner.getChromosomeString(), scores[0]);
        }
        if (scoreMap.containsKey(jPrisoner.getChromosomeString())) {
          scoreMap.put(jPrisoner.getChromosomeString(), scoreMap.get(jPrisoner.getChromosomeString()) + scores[1]);
        } else {
          scoreMap.put(jPrisoner.getChromosomeString(), scores[1]);
        }
      }
    }
    return scoreMap;
  }

}
