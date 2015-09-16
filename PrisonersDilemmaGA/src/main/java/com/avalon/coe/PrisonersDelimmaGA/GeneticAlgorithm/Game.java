package com.avalon.coe.PrisonersDelimmaGA.GeneticAlgorithm;

import java.util.BitSet;

/**
 * Class there a game between 2 Chromosomes is done for n iteration.
 * Class is nearly copied from https://github.com/aerrity/prisoners-dilemma
 */
public class Game {

  //Two Players
  private Prisoner P1;
  private Prisoner P2;

  //Payoffs recieved
  private int P1Score;
  private int P2Score;

  //Rules for the IPD
  private int iterations;

  private int T, S, R, P;

  /**
   *Create a new game of the Iterated Prisoner's Dilemma between two players
   */
  public Game(Prisoner p1, Prisoner p2, int iterations) {
    P1 = p1;
    P2 = p2;
    this.iterations = iterations;
    T = 5; //Temptation to defect
    S = 0; //Sucker's Payoff
    R = 3; //Reward for mutual cooperation
    P = 1; //Punishment for mutual defection
  }


  /**Play a game of IPD according to the rules*/
  public void Play() {
    //Init
    P1Score = 0;
    P2Score = 0;
    BitSet P1History = new BitSet();
    BitSet P2History = new BitSet();
    boolean P1move, P2move;

    //Play the specified number of PD games
    for(int iteration = 0; iteration < this.iterations; iteration++) {
      //Get each players move
      P1move = P1.play(iteration,P1History);
      P2move = P2.play(iteration,P2History);

      //Update scores according to payoffs
      if(P1move && P2move) { //CC
        P1Score += R;
        P2Score += R;
      }
      else if(P1move && !P2move) { //CD
        P1Score += S;
        P2Score += T;

      }
      else if(!P1move && P2move) {//DC
        P1Score += T;
        P2Score += S;

      }
      else if(!P1move && !P2move) {//DD
        P1Score += P;
        P2Score += P;
      }

      //Update player histories
      if(P1move)
      {
        P1History.set(iteration*2);
        P2History.set((iteration*2)+1);
      }
      else
      {
        P1History.clear(iteration*2);
        P2History.clear((iteration*2)+1);
      }
      if(P2move)
      {
        P1History.set((iteration*2)+1);
        P2History.set((iteration*2));
      }
      else
      {
        P1History.clear((iteration*2)+1);
        P2History.clear((iteration*2));
      }
    }
    //Update each players score
    P1.updateScore(P1Score);
    P2.updateScore(P2Score);
  }

  /**
   * Get game results
   * returns array containing player 1 and player 2's score - [p1,p2]
   */
  public int[] getScores() {
    int	[] scores = {P1Score, P2Score};
    return scores;
  }


}
