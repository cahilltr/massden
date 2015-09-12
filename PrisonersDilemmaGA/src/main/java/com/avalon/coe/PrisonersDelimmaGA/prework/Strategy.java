package com.avalon.coe.PrisonersDelimmaGA.prework;

/**
 * Created by cahillt on 9/3/15.
 */
public interface Strategy {

  int nextMove();

  void saveMove(int move);

  void saveOpponentMove(int move);

}
