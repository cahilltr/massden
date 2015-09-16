package com.avalon.coe.PrisonersDelimmaGA.prework;

import org.apache.commons.lang3.StringUtils;

import java.util.BitSet;
import java.util.Hashtable;

/**
 * This class is a renamed, slightly updated Version of
 * https://github.com/aerrity/prisoners-dilemma.
 *
 * The Hashtable moves maps a 3 game history to a particular
 * history in the Chromosome.  This tables is the same across
 * the board for all chromosomes.
 */
public class ChromosomeMappingTable {
  private Hashtable moves;

  /**
   * Creates a table of the 64 possible histories of a 3 game sequence,
   * indexed by numbers from 0-63.
   * Table is the spot in the chromosome that responds to a particular history
   */

  public ChromosomeMappingTable() {
    moves = new Hashtable(64);
    String s;

    //Build table of all possible moves (3-game history)
    for(int n = 0; n <64; n++)
    {
      s = Integer.toString(n,2);
      StringUtils.leftPad(s, 6, '0');

      BitSet temp = new BitSet(6);
      for(int i = 0; i < 6; i++)
        if(s.charAt(i) == '0')
          temp.set(i);
      System.out.println(s + " " + temp);
      moves.put(temp,new Integer(n));
    }
  }

  /**
   * Decodes a 3 game history to an index number
   * to map to a gene in the chromosome
   */
  public int get(BitSet h)
  {
    return (int)moves.get(h);
  }
}
