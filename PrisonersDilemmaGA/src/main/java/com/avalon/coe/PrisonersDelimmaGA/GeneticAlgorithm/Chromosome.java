package com.avalon.coe.PrisonersDelimmaGA.GeneticAlgorithm;

import java.util.BitSet;
import java.util.Random;

/**
 * Chromosome Class
 * A chromosome can also be referred to as a Strategy.
 * Size for a 3 game history is 71
 *
 * generateInitalChromosome, mutate, and  code is from https://github.com/aerrity/prisoners-dilemma
 */
public class Chromosome {

  private final int size;
  private BitSet chromosome;
  private int score;

  public Chromosome(BitSet chromosome) {
    this.chromosome = chromosome;
    this.size = chromosome.length();
  }

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

  public void updateScore(int s) {
    this.score += s;
  }

  public int getScore() {
    return this.score;
  }

  /**
   * Mate two parents using random, one point crossover
   * and returns an array containing the two children Chromosomes
   */
  public static Chromosome[] crossover(BitSet parenta,BitSet parentb)
  {
    Random rand = new Random();
    BitSet child1 = new BitSet(71);
    BitSet child2 = new BitSet(71);

    //One point splicing
    int slicePoint = rand.nextInt(71); //rnd num between 0-70
    BitSet a = (BitSet)parenta.clone();
    a.clear(slicePoint,71);
    BitSet b = (BitSet)parenta.clone();
    b.clear(0,slicePoint);
    BitSet c = (BitSet)parentb.clone();
    c.clear(slicePoint,71);
    BitSet d = (BitSet)parentb.clone();
    d.clear(0,slicePoint);

    //Combine start of p1 with end of p2
    child1.or(a);
    child1.or(d);
    //Combine start of p2 with end of p1
    child2.or(c);
    child2.or(b);

    //Return the new child Chromosomes
    return new Chromosome[]{new Chromosome(child1), new Chromosome(child2)};
  }

  /**
   * Mutate (Flip a bit in the bitset) with probability mProb
   * on the existing Chromosome
   * mProb the probability of a bit being mutated
   *
   */
  public void mutate(double mProb)
  {
    Random rand = new Random();
    for(int m = 0; m < 71; m++)
    {
      //Small possibility a bit copied from parent to child is mutated
      if(rand.nextDouble() <= mProb)
        this.chromosome.flip(m);
    }
  }

}
