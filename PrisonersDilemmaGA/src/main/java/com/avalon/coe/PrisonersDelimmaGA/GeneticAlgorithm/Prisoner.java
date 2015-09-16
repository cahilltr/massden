package com.avalon.coe.PrisonersDelimmaGA.GeneticAlgorithm;

import com.avalon.coe.PrisonersDelimmaGA.prework.Chromosome;
import com.avalon.coe.PrisonersDelimmaGA.prework.ChromosomeMappingTable;

import java.util.BitSet;

/**
 * Prisoner Class maintains prisoner strategy
 * and returns moves based on previous history
 */
public class Prisoner {

  private final Chromosome myChromosome;
  private final ChromosomeMappingTable chromosomeMappingTable;

  public Prisoner (Chromosome chromosome, ChromosomeMappingTable chromosomeMappingTable) {
    this.myChromosome = chromosome;
    this.chromosomeMappingTable = chromosomeMappingTable;
  }

  /**
  * Gets the Prisoner's next game move
  * Iteration is the iteration of the game
  * History should represent previous moves as C=1 and D=0
  * This players move should be followed by the opponents move, one
  * such pair for each previous iteration of PD
  * Returns true for Cooperate or false for defect.
  *
  * This is taken from https://github.com/aerrity/prisoners-dilemma
  */
  public boolean play(int iteration, BitSet History) {

    if(iteration == 0) { // if first move return start move
      return this.myChromosome.get(0);
    } else if(iteration == 1) { // if second move
      if(History.get(1)) //opponent Cooperated
        return this.myChromosome.get(1);
      else //opponent Defected
        return this.myChromosome.get(2);
    } else if(iteration == 2) { // if third move
      if(History.get(1) && History.get(3)) //opponent CC
        return this.myChromosome.get(3);
      else if(History.get(1) && !History.get(3)) //opponent CD
        return this.myChromosome.get(4);
      else if(!History.get(1) && History.get(3)) //opponent DC
        return this.myChromosome.get(5);
      else if(!History.get(1) && !History.get(3)) ////opponent DD
        return this.myChromosome.get(6);
    } else { // if normal move, use normal strategy
      //Get last 3 sets of moves
      BitSet hist = History.get( (iteration*2) - 6, (iteration*2));
      int x = this.chromosomeMappingTable.get(hist);
      return this.myChromosome.get(x+7); //adjust index to skip setup info
    }
    return false;
  }
}
