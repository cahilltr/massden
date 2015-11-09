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
  private int score = -1;
  private int gameScore = -1;

  public Chromosome(BitSet chromo) {
    this.chromosome = chromo;
    this.size = chromo.length();
  }

  public Chromosome(int size, boolean generateChromosome) {
    this.size = size;
    if (generateChromosome) {
      generateInitalChromosome();
    }
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

  public BitSet getChromosome() {
    return this.chromosome;
  }

  public void updateScore(int s) {
    if (this.gameScore < 0) {
      this.gameScore += 1;
      this.score += 1;
    }
    this.gameScore += s;
    this.score += s;
  }

  public int getScore() {
    return this.score;
  }

  public int getGameScore() { return this.gameScore; }

  /**
   * Mate two parents using random, one point crossover
   * and returns an array containing the two children Chromosomes
   */
  public Chromosome[] crossover(Chromosome parentb)
  {
    Random rand = new Random();
    BitSet child1 = new BitSet(71);
    BitSet child2 = new BitSet(71);

    //One point splicing
    int slicePoint = rand.nextInt(71); //rnd num between 0-70
    BitSet a = (BitSet)this.chromosome.clone();
    a.clear(slicePoint, 71);
    BitSet b = (BitSet)this.chromosome.clone();
    b.clear(0, slicePoint);
    BitSet c = (BitSet)parentb.chromosome.clone();
    c.clear(slicePoint, 71);
    BitSet d = (BitSet)parentb.chromosome.clone();
    d.clear(0, slicePoint);

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

  public String toString() {
    String p = "";
    for(int i = 0; i < this.chromosome.length(); i++)
    {
      if(this.chromosome.get(i))
        p += 'C';
      else
        p += 'D';
    }
    return p;
  }

}
