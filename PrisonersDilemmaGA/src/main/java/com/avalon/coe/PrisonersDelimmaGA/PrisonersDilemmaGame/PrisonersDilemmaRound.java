package com.avalon.coe.PrisonersDelimmaGA.PrisonersDilemmaGame;

import com.avalon.coe.PrisonersDelimmaGA.Strategy.Strategy;

/**
 * Play a round of Prisoners Dilemma
 */
public class PrisonersDilemmaRound {

  int player1Payoff;
  int player2Payoff;

  int player1Move;
  int player2Move;

  public void playRound(Strategy p1, Strategy p2) {
    int T = 7;
    int R = 5;
    int P = 3;
    int S = 1;

    // 0 = defect, 1 = cooperate
    player1Move = p1.nextMove();
    player2Move = p2.nextMove();

    p1.saveMove(player1Move);
    p2.saveMove(player2Move);
    p1.saveOpponentMove(player2Move);
    p1.saveOpponentMove(player1Move);

    if (player1Move == 0 && player2Move == 0)
    {
      player1Payoff = P;
      player2Payoff = P;
    }
    else if (player1Move == 0 && player2Move == 1)
    {
      player1Payoff = T;
      player2Payoff = S;
    }
    else if (player1Move == 1 && player2Move == 0)
    {
      player1Payoff = S;
      player2Payoff = T;
    }
    else if (player1Move == 1 && player2Move == 1)
    {
      player1Payoff = R;
      player2Payoff = R;
    }
  }

  public int getPlayer1Move()  { return player1Move; }
  public int getPlayer2Move()  { return player2Move; }
  public int getPlayer1Payoff()  { return player1Payoff; }
  public int getPlayer2Payoff()  { return player2Payoff; }
}
