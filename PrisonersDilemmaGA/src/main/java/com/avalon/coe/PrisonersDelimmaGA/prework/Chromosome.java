package com.avalon.coe.PrisonersDelimmaGA.prework;

import java.util.BitSet;
import java.util.Random;

/**
 * Chromosome Class
 * A chromosome can also be referred to as a Strategy.
 * Size for a 3 game history is 71
 *
 * generateInitalChromosome code is from https://github.com/aerrity/prisoners-dilemma
 */
public class Chromosome {

  private final int size;
  private BitSet chromosome;

  public Chromosome(int size) {
    this.size = size;
  }

  public void generateInitalChromosome() {
    BitSet ra;
    Random rand = new Random();

    //build random Chromosome
    ra = new BitSet(size);
    for(int i = 0; i < size;i++) //set strategy
    {
      if(rand.nextBoolean())
        ra.set(i);
      else
        ra.clear(i);
    }
    this.chromosome = ra;
  }

  public boolean get(int index) {
    return this.chromosome.get(index);
  }

}
